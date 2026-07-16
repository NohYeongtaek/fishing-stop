# 공지사항 Firebase 연동 + 관리자 작성(숨은 PIN 게이트) 구현 프롬프트

> 이 문서를 읽고 그대로 구현한다. 목표: **하드코딩된 공지사항을 Firestore 연동으로 바꾸고, 앱 안에서 관리자만 공지를 작성**할 수 있게 한다.
> 관리자 진입은 설정 화면의 "app version" 텍스트를 **10번 연속 탭 → PIN 4자리** 로 여는 숨은 게이트다.
> 협업 규칙: 커밋은 **kmj 로컬 브랜치에만**(절대 dev 금지), 환경 파일(`local.properties`·`.gradle/`·`.idea/`·`build/`) 제외.

---

## 0. 배경 · 현재 상태

- 지금 공지사항은 `feature/settings/presentation/NoticeScreen.kt` 안에 `private val NOTICES` 로 **하드코딩**되어 있고, 설정 → "공지사항"(`Routes.NoticeList`)으로 진입한다.
- Firestore 접근 패턴은 이미 있다: `feature/report/data/ReportRepositoryImpl.kt` 참고(주입된 `FirebaseFirestore`, `withTimeoutOrNull`로 오프라인 무한대기 방지, Task→코루틴 래핑).
- `core/di/FirebaseModule.kt` 가 `FirebaseFirestore`·`FirebaseAuth` 를 **이미 provide** 한다(익명 인증 추가 비용 없음).
- DataStore preferences 는 이미 사용 중(`androidx.datastore:datastore-preferences`), Material3(BOM 2024.09.00 → 1.3.0)이라 `PullToRefreshBox` 사용 가능.
- 디자인 토큰/공통 위젯(`AppTheme.*`, `AppScaffold`·`AppTopBar`·`AppCard`·`AppTextField`·`PrimaryButton`·`SecondaryButton`·`WarnBox`)이 전 화면에 적용돼 있으니 **신규 화면도 반드시 토큰/공통 위젯으로** 만든다.

---

## 1. 확정 결정 사항

1. **보안 방식 = PIN 게이트(클라이언트) — v1 기본**. 요청대로 계정/로그인 화면 없이 PIN만으로 작성 화면을 연다.
   - ⚠️ **명시적 한계**: PIN 검증은 앱 안에서만 이뤄지므로, APK 디컴파일·직접 API 호출로 우회 가능하다. Firestore 규칙은 이 PIN을 검증할 방법이 없다(요청에 "PIN 통과" 표식이 실리지 않음). 즉 **공지 데이터는 사실상 공개 쓰기** 상태가 된다.
   - ✅ **권장 옵션(2-2에 함께 명세, 기본은 끔)**: PIN 통과 시 `FirebaseAuth.signInAnonymously()` 1회 호출 + 규칙 `allow create: if request.auth != null`. 로그인 UI·계정 불필요(현 UX 그대로), firebase-auth·provideFirebaseAuth 이미 있음 → **코드 3줄**. 무작위 API 난사는 막힌다. 이 앱은 피싱 예방 앱이라 공지 오염(가짜 링크 삽입) 리스크가 있으니 **켜는 것을 권장**한다. 구현 시 `USE_ANON_AUTH` 한 곳 토글로 on/off 가능하게 둔다.
2. **관리자 여러 명 → 공용 PIN 1개**. PIN 값은 앱에 하드코딩하지 말고 **Firestore `config/adminGate` 문서의 `pinHash`(4자리 PIN의 SHA-256 hex)** 에서 읽어 비교한다. 팀원/PIN 변경 시 **앱 재배포 없이 콘솔에서 값만 교체**. (PIN 해시가 read 공개라 4자리는 오프라인 브루트포스로 뚫린다 — 이건 암호학적 보안이 아니라 "운영 편의 + 우발적 진입 차단" 목적임을 코드 주석에 명시.)
3. **v1 범위 = 작성(등록)만**. 수정/삭제는 Firebase 콘솔에서 직접. 앱엔 수정/삭제 UI 없음.
4. **목록 갱신 = 당겨서 새로고침(pull-to-refresh)만**. 실시간 리스너 미사용(읽기 비용·복잡도 절감). 화면 진입 시 1회 로드 + 사용자가 당기면 재로드.
5. **락아웃 = 기기별 로컬**. PIN 5회 틀리면 30분 잠금. DataStore에 실패 횟수·해제 시각 저장(앱 재시작해도 유지). 서버 검증 아님(로컬 방어).

---

## 2. Firestore 스키마 · 보안 규칙 · 콘솔 작업(사용자가 직접)

### 2-1. 컬렉션 / 문서

- **`notices` 컬렉션** — 문서 ID 자동생성(`add`). 필드:
  | 필드 | 타입 | 설명 |
  |---|---|---|
  | `title` | string | 공지 제목 |
  | `body` | string | 공지 본문 |
  | `createdAt` | timestamp | `FieldValue.serverTimestamp()` (서버 기준 시각) |
  - 앱 최초 write 시 자동 생성되므로 미리 만들 필요 없음. 정렬은 `orderBy("createdAt", DESCENDING)`.
- **`config/adminGate` 문서** — 콘솔에서 **수동 생성**. 필드:
  | 필드 | 타입 | 설명 |
  |---|---|---|
  | `pinHash` | string | 4자리 PIN의 **SHA-256 hex 소문자**. 예: PIN `1234` → `03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4` |

### 2-2. 보안 규칙 (Firestore Rules) — 사용자가 콘솔에 반영

**기본(PIN-only, `USE_ANON_AUTH=false`):**
```
match /notices/{id} {
  allow read: if true;
  allow create: if request.resource.data.title is string
             && request.resource.data.body is string
             && request.resource.data.title.size() > 0
             && request.resource.data.title.size() <= 100
             && request.resource.data.body.size() <= 5000;
  allow update, delete: if false;   // 수정·삭제는 콘솔에서만
}
match /config/adminGate {
  allow read: if true;    // 앱이 pinHash 로드
  allow write: if false;  // 콘솔에서만 변경
}
```

**권장(익명 인증, `USE_ANON_AUTH=true`):** 위 `notices` create 조건 맨 앞에 `request.auth != null &&` 추가.

### 2-3. 사용자가 콘솔에서 할 일(구현과 별개, 안내만)
1. Firestore에 `config/adminGate` 문서 생성 후 `pinHash` 입력(원하는 PIN의 SHA-256).
2. 위 규칙 배포.
3. (권장 옵션 사용 시) Authentication → 로그인 방법 → **익명** 사용 설정.

---

## 3. 앱 아키텍처 — 신규 `feature/notice/` 모듈

기존 `feature/settings/presentation/NoticeScreen.kt`(하드코딩)는 **삭제**하고 공지 기능을 독립 모듈로 옮긴다(education/report 모듈과 동일한 클린아키텍처 결).

```
feature/notice/
├─ domain/
│  ├─ model/Notice.kt              # data class Notice(id, title, body, createdAtMillis: Long)
│  ├─ NoticeRepository.kt          # suspend getNotices(): List<Notice>;  suspend addNotice(title, body)
│  └─ AdminGateRepository.kt       # suspend verifyPin(pin): PinResult;  락아웃 상태 Flow
├─ data/
│  ├─ NoticeRepositoryImpl.kt      # Firestore notices read/write (withTimeoutOrNull, Task→코루틴)
│  └─ AdminGateRepositoryImpl.kt   # config/adminGate.pinHash 로드+SHA256 비교 + DataStore 락아웃
├─ di/NoticeModule.kt              # @Binds NoticeRepository, AdminGateRepository
└─ presentation/
   ├─ NoticeListScreen.kt + NoticeListViewModel   # Firestore 목록 + pull-to-refresh (기존 NoticeList 대체)
   ├─ NoticeWriteScreen.kt + NoticeWriteViewModel # 제목/본문 입력 → addNotice → 목록 복귀
   └─ AdminPinDialog.kt                            # 4자리 PIN 입력 다이얼로그(공통 위젯 사용)
```

**수정 대상(기존 파일):**
- `core/navigation/Routes.kt` — `NoticeWrite` route 추가(`data object`). `NoticeList` 는 유지(설명만 "Firestore 연동"으로).
- `core/navigation/FishingStopNavGraph.kt` — `NoticeList`를 새 `NoticeListScreen`으로 교체, `NoticeWrite` composable 추가. 설정에서 넘어오는 관리자 진입 콜백 배선.
- `feature/settings/presentation/SettingsScreen.kt` — "app version" 텍스트에 10탭 게이트(4절).
- `feature/settings/presentation/NoticeScreen.kt` — **삭제**(내용 이관).

---

## 4. 상세 동작 명세

### 4-1. 설정 화면 — 숨은 관리자 진입(10탭 → PIN)
- `SettingsScreen.kt` 하단 `"app version : x.x"` 텍스트를:
  - **리플/터치효과 제거**: `Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { ... }`.
  - **10탭 카운터**: 로컬 `var tapCount by remember { mutableIntStateOf(0) }`. 탭마다 +1. 10 도달 시 카운터 0으로 리셋하고 PIN 다이얼로그를 띄운다. (탭 사이 시간제한은 두지 않음 — 단순 카운트. 화면 이탈 시 자연 초기화.)
  - 진행 힌트는 노출하지 않는다(숨은 기능).
- PIN 다이얼로그(`AdminPinDialog`)를 `showPinDialog` 상태로 표시. 결과 처리는 ViewModel 경유(락아웃 상태 필요).
- **PIN 성공 시**: (옵션이면 익명 로그인 완료 후) `onOpenNoticeWrite()` 콜백으로 `Routes.NoticeWrite` 이동. 콜백은 NavGraph에서 주입.

### 4-2. PIN 다이얼로그(`AdminPinDialog`)
- 4자리 **숫자** 입력(`AppTextField` + `KeyboardType.NumberPassword`, `maxLength 4`). 또는 간단한 숫자 패드. `AlertDialog`(토큰 색: `containerColor = AppTheme.colors.cardBg`).
- 상태별 표시:
  - 잠김(락아웃 중): 입력 비활성 + `WarnBox` 로 "너무 많이 틀렸어요. N분 후 다시 시도해 주세요." (남은 분은 `lockUntil - now` 로 계산).
  - 실패: "비밀번호가 올바르지 않아요. (남은 시도 N회)" — 5회 중 남은 횟수.
  - 검증 중: 스피너(`config/adminGate` 로드 네트워크 필요).
- 취소/바깥 탭으로 닫기 허용(단, 카운터는 이미 리셋됨).

### 4-3. AdminGate 검증 로직(`AdminGateRepositoryImpl`)
- DataStore 키: `admin_failed_attempts: Int`, `admin_lock_until: Long`(epoch millis, 0이면 미잠금).
- `verifyPin(pin: String): PinResult`:
  1. 현재 `now < lockUntil` 이면 즉시 `PinResult.Locked(remainingMillis)`.
  2. `config/adminGate` 문서에서 `pinHash` 로드(`withTimeoutOrNull`, 실패 시 `PinResult.Error("네트워크 확인")`).
  3. `sha256(pin) == pinHash` →
     - 성공: `failedAttempts = 0`, `lockUntil = 0` 리셋 후 `PinResult.Success`.
     - 실패: `failedAttempts += 1`. `failedAttempts >= 5` 이면 `lockUntil = now + 30*60*1000` 설정하고 카운터 리셋 → `PinResult.Locked(30분)`. 아니면 `PinResult.Failed(remaining = 5 - failedAttempts)`.
- `sha256`은 `java.security.MessageDigest`(SHA-256) hex 소문자. 유틸로 분리(`core/util` 또는 data 내부 private).
- 봉인 클래스: `sealed interface PinResult { Success; data class Failed(remaining: Int); data class Locked(remainingMillis: Long); data class Error(msg: String) }`.

### 4-4. 공지 작성 화면(`NoticeWriteScreen`)
- `AppScaffold` + `AppTopBar(title="공지 작성", onBack)`.
- `AppTextField` 2개: 제목(singleLine, 힌트 "제목"), 본문(멀티라인, `heightIn(min=200.dp)`, 힌트 "내용").
- 하단 `PrimaryButton("등록하기")`:
  - 빈 값 검증(제목/본문 공백 → Toast).
  - `NoticeWriteViewModel.submit()` → `repository.addNotice(title, body)`(Firestore `add` + `serverTimestamp`, `withTimeoutOrNull`).
  - 성공: Toast "공지가 등록되었어요" + `popBackStack`(목록으로). 목록은 자동 새로고침 안 하므로, 돌아가면 pull-to-refresh 안내 or `NoticeList` 진입 시 재로드되게(초기 로드가 있으니 자연스러움).
  - 실패: 오류 메시지(`toUserMessage()` 재사용 가능).
- 로딩 상태 스피너.

### 4-5. 공지 목록 화면(`NoticeListScreen`) — pull-to-refresh
- 기존 아코디언 UX 유지 가능(카드 탭 시 본문 펼침) 또는 단순 목록. 데이터만 Firestore로 교체.
- `Material3 PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { vm.refresh() })` 로 감싼 `LazyColumn`.
- 진입 시 1회 자동 로드(`init`). 당기면 `getNotices()` 재호출.
- 상태: `Loading`(첫 로드 스피너) / `Success(list, isRefreshing)` / `Empty`("등록된 공지가 없어요") / `Error`(메시지 + 다시시도).
- 각 항목: `AppCard`, 제목(`AppTheme.type.cardLabel`), 날짜(`createdAtMillis` → "yyyy.MM.dd" 포맷, `AppTheme.type.caption`/`textTertiary`), 본문(펼침 시). 토큰 색 사용.
- 정렬: 최신순(`createdAt DESC`). `createdAt` 이 아직 서버 반영 전(null)일 수 있으니 null-safe 처리.

---

## 5. 구현 순서(권장)

1. `Routes.NoticeWrite` 추가.
2. `feature/notice/domain` (Notice, NoticeRepository, AdminGateRepository, PinResult).
3. `feature/notice/data` (NoticeRepositoryImpl, AdminGateRepositoryImpl + sha256) + `di/NoticeModule`.
4. DataStore에 admin 락아웃 키 추가(기존 DataStore 재사용 또는 전용 preferences).
5. `NoticeListScreen`(+VM): Firestore 목록 + pull-to-refresh. `FishingStopNavGraph`의 `NoticeList` 를 이걸로 교체. 기존 `settings/.../NoticeScreen.kt` 삭제.
6. `AdminPinDialog` + `NoticeWriteScreen`(+VM).
7. `SettingsScreen` 10탭 게이트 배선 + NavGraph에 `NoticeWrite` composable/콜백.
8. (권장 옵션) `USE_ANON_AUTH` 배선: PIN 성공 시 `FirebaseAuth.signInAnonymously()` await 후 이동.
9. 빌드(`:app:assembleDebug`) → 실기기 검증(6절).

---

## 6. 완료 기준 / 검증

- [ ] 공지 목록이 Firestore `notices` 에서 로드되고, 당겨서 새로고침이 동작한다(하드코딩 제거).
- [ ] 설정 "app version" 10탭 → PIN 다이얼로그(리플 없음). 9탭까진 아무 반응 없음.
- [ ] 올바른 PIN → 작성 화면 진입 → 등록 → Firestore에 문서 생성 → 목록 새로고침 시 노출.
- [ ] 틀린 PIN 5회 → 30분 잠금 메시지, 앱 재시작해도 잠금 유지. 30분 후 재시도 가능.
- [ ] (권장 옵션 켰다면) 익명 인증 후에만 write 성공, 규칙이 미인증 write 거부.
- [ ] 라이트/다크 · 어르신 4조합에서 신규 화면 토큰 정상.
- [ ] 빌드 통과, 실기기 육안 확인.

## 7. 범위 밖(v1 제외, 후속)
- 앱 내 공지 **수정/삭제**(콘솔에서 처리).
- 실시간 자동 갱신(리스너).
- 관리자별 개별 계정·권한 분리(현재 공용 PIN).
- 서버측 PIN 검증(Cloud Functions).
- PIN 변경 UI(콘솔에서 `pinHash` 교체).

---

## 참고: 보안 요약(한 줄)
PIN·10탭은 **우발적/일반 사용자 진입 차단** 용도이며 암호학적 방어가 아니다. 실질 방어가 필요하면 `USE_ANON_AUTH=true`(규칙 `request.auth != null`)를 켜서 "우리 앱을 거친 요청만" 쓰게 하는 것을 권장한다. 완전한 방어는 Cloud Functions 릴레이(7절)만 제공한다.
