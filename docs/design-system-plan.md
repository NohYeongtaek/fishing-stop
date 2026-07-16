# 디자인 시스템 적용 계획 (실행용)

> 「피싱멈춰! 디자인 스펙 문서」를 Compose에 적용하기 위한 실행 계획.
> 확정 결정: **다크 모드 유지(다크 토큰 추가 정의) / 어르신 모드=명시적 토큰 세트로 전환 / 범위 Phase 0~3.**
> 이 문서를 기준으로 착수한다. 아직 미구현.

## 확정 결정 사항

1. **다크 모드 유지** — 설정의 시스템/라이트/다크 3종 유지. 스펙엔 다크 토큰이 없으므로 아래 4-1에 다크값을 새로 정의(온디바이스 튜닝 대상, "제안값" 표기).
2. **어르신 모드 = 명시적 토큰 세트** — 현재 `FishingStopApp`의 `LocalDensity fontScale × 1.4` 해킹을 **제거**하고, 스펙의 어르신 값(H1 29, 버튼 70 등)을 토큰으로 명시. 색·테두리·레이아웃까지 토큰/분기로 제어.
3. **단위 매핑** — 스펙의 px를 **크기·간격·반경은 dp, 폰트는 sp**로 그대로 사용. sp는 시스템 글꼴 배율을 존중(접근성). 어르신 모드는 그 위에 얹는 앱 자체 토글.
4. **폰트** — 스펙은 Noto Sans KR. 현재 `FontFamily.Default`. → res/font에 Noto Sans KR(또는 Downloadable Fonts) 추가 필요(Phase 0 과제).
5. **기존 인터랙션 보존** — 스펙(정적 시안)에 없더라도 **현재 앱의 동작 방식은 유지**한다. 색·크기·모양 토큰만 스펙값으로 교체하고, 애니메이션·선택 표시 로직은 그대로 둔다. 대상은 아래 「인터랙션 보존 원칙」 참고.

## 토큰 아키텍처 (CompositionLocal 기반 커스텀 디자인 시스템)

Material3는 베이스 컴포넌트(Button/TextField/Switch 등)로만 쓰고, 스펙 커스텀 토큰은 별도 레이어로 얹는다.

색상은 `(dark, elder)` 조합으로, 나머지(타이포·크기·간격·반경)는 `(elder)`로 선택된다.

```
ui/theme/
├─ AppColors.kt     # data class AppColors(pageBg, cardBg, greenPrimary, dangerPrimary, warnBg, textPrimary, ...)
│                   # NormalLightColors / ElderLightColors / NormalDarkColors / ElderDarkColors
├─ AppType.kt       # data class AppType(h1, subtitle, body, cardLabel, caption, button, scoreNum, hotlineNum, tabLabel, chevron)
│                   # NormalType / ElderType  (TextStyle, fontFamily=NotoSansKR)
├─ AppShapes.kt     # data class AppShapes(card, button, iconBox, pill, input)  → Normal / Elder
├─ AppSizes.kt      # data class AppSizes(btnPrimaryH, btnSecondaryH, iconBox, tabIcon, homeCircle, scoreCircle, switch, statusCircle, borderCard, borderInput)
├─ AppSpacing.kt    # data class AppSpacing(screenX, cardPad, stackGap, listGap, navbar…)
├─ AppElevation.kt  # 그림자 근사(아래 Compose 노트 참고)
├─ AppTheme.kt      # LocalAppColors/Type/Shapes/Sizes/Spacing + object AppTheme { colors/type/... @Composable get }
└─ FishingstopTheme.kt  # FishingstopTheme(darkTheme, elder){ pick sets → CompositionLocalProvider + MaterialTheme 매핑 }
```

접근 예시: `AppTheme.colors.greenPrimary`, `AppTheme.type.h1`, `AppTheme.sizes.btnPrimaryH`, `AppTheme.spacing.screenX`.

`FishingStopApp`는 `FishingstopTheme(darkTheme = …, elder = elderMode)` 호출로 바꾸고 **density ×1.4 제거**.

---

## 인터랙션 보존 원칙 (Phase 2에서 반드시 지킬 것)

스펙 문서는 **정적 시안**이라 애니메이션·상태 표시 방식이 빠져 있다. 마이그레이션은 **"보이는 값(색·크기·모양·타이포)만 토큰으로 교체"**하는 작업이며, 아래 동작들은 **로직 그대로 유지**한다. 정적 시안대로 갈아엎지 말 것.

### ① 홈 중앙 "검사 시작" 원형 버튼의 물결 애니메이션 — 유지
- 현재: `HomeScreen.kt`의 `RippleWaves`가 위상차를 둔 물결 3개(`rememberInfiniteTransition` + `animateFloat`, 3s 루프)를 원 뒤로 퍼뜨려 주목도를 높인다. 그 위에 원형 `Button`("검사 시작").
- 스펙 2-1은 이 원을 `radial green`의 **정적 원**으로만 그리므로, **물결 애니메이션을 제거하지 말 것.**
- 마이그레이션 시 교체하는 것은 **값뿐**:
  - 물결/버튼 색 `MaterialTheme.colorScheme.primary` → `AppTheme.colors.greenPrimary`(원 채움은 `greenGradient` radial 가능).
  - 버튼 지름 `200.dp` → `AppTheme.sizes.homeCircle`(일반 210 / 어르신 240), 바깥 Box는 그에 비례.
  - 어르신 모드: 스펙의 `+4dp 흰 테두리`를 원형 버튼에 추가(토큰 `borderCard`/전용 값).
  - "검사 시작" 텍스트 `fontSize=24.sp` → `AppTheme.type` 적절 역할(예: `button`/`h1`).
- `RippleWaves`의 물결 개수·주기·알파 감쇠 로직은 그대로. 다크에서 알파만 육안 확인.

### ② 하단 탭바 선택 표시 = 아이콘+글씨 색 변화 — 유지 (알약 인디케이터 없음)
- 현재: 선택 탭을 **아이콘·라벨 색을 브랜드 그린으로 바꿔** 표시하고, Material의 알약형 `indicator`는 `Color.Transparent`로 끈다. (`HomeScreen.kt`의 `NavigationBar`, 그리고 이미 만든 `AppBottomBar`가 동일 방식.)
- 스펙 1-7 탭바로 바꾸더라도 **이 "색 변화" 선택 표시 방식을 유지**하고 알약 배경을 다시 켜지 말 것.
- 교체 대상 값: 활성색 → `AppTheme.colors.greenPrimary`, 비활성 → `textTertiary`, 아이콘 크기 → `AppTheme.sizes.tabIcon`, 라벨 → `AppTheme.type.tabLabel`, 배경 → `navbarBg`. (전부 `AppBottomBar`에 이미 반영됨.)
- **후속 작업**: `HomeScreen.kt`의 인라인 `NavigationBar`를 `AppBottomBar`로 교체(동일 동작이라 시각만 토큰화). `HomeTab` enum의 라벨/아이콘은 그대로 넘긴다.

### 기타 보존 대상(발견 시 동일 원칙 적용)
- 탭 상태 `rememberSaveable`(뒤로가기 시 탭 유지) — 유지.
- 결과 화면 점수/등급 표현의 기존 인터랙션(있다면) — 값만 토큰화.
- 즉, **"애니메이션·상태 로직 = 유지, 색/크기/모양/폰트 = 토큰 교체"**가 Phase 2 전반의 기본 규칙.

---

## Phase 0 — 토큰 인프라 (화면 변경 없음)

### 0-1. 색상 토큰 (Light: 스펙 그대로, Dark: 제안값)

일반-라이트 / 어르신-라이트는 스펙 1-1을 그대로 코드화. 다크는 아래 제안값(튜닝 대상):

| 토큰 | 일반-라이트 | 어르신-라이트 | 일반-다크(제안) | 어르신-다크(제안) |
|---|---|---|---|---|
| pageBg | #F7F4EC | #F7F4EC | #121410 | #0E100C |
| cardBg | #FFFFFF | #FFFFFF | #1E211B | #23271F |
| navbarBg | #FFFFFF | #FFFFFF | #1A1D17 | #1A1D17 |
| greenPrimary | #2F7A4A | #276B3F | #5FB07A | #6FC08A |
| greenTint/chipBg | #EAF3EC | #E4F0E6 | #1F3327 | #24402E |
| dangerPrimary | #C24E44 | #B23A31 | #E07A70 | #EC8C82 |
| dangerChipBg | #FBEAE7 | #FBEAE7 | #3A2320 | #45261F |
| warnBg/border/text | #FDF6E7 / #EBD9A8 / #9A6B12 | (동일) | #332C1A / #5C4E28 / #E8C87A | #3A3320 / #6B5A2E / #F0D289 |
| textPrimary | #2E3A2B | #1F2A1D | #ECEFE8 | #F5F7F0 |
| textSecondary | #5C564A | #3A362C | #C2C6BB | #D2D6CB |
| textTertiary | #8A8271 | #4A453B | #8F978A | #9AA290 |
| textPlaceholder | #A39B87 | #8A8271 | #6E756A | #7A8175 |
| borderInput | #D8D2C2 | #B7AE94 | #3A3E34 | #4A4E42 |
| borderNavbar | #EAE5D8 | #E0D9C6 | #2A2E24 | #33372E |
| borderDivider | #F0EDE2 | #E0D9C6 | #262A20 | #2E322800 |
| borderCard | 없음 | #E0D9C6(2px) | 없음 | #3A3E34(2px) |
| starActive | #E4B93C | #E4B93C | #E4B93C | #E4B93C |
| highlightBg | — | #F3EBCE | — | #3A3320 |

> gradient(green/danger)는 위 primary 기준 2색을 Brush로. 다크에선 대비 확보 위해 약간 밝게.

### 0-2. 타이포 토큰 (sp)

| 역할 | 일반 | 어르신 | weight | lineHeight |
|---|---|---|---|---|
| h1 | 24 | 29 | 900 | 1.3 |
| subtitle | 17 | 20 | 400 | 1.55 |
| body | 16 | 20 | 400 | 1.6 |
| cardLabel | 15 | 18 | 700/900 | 1.4 |
| caption | 14 | 16.5 | 400 | 1.55 |
| button | 19 | 23 | 700/900 | 1.2 |
| scoreNum | 34 | 40 | 900 | 1.0 |
| hotlineNum | 38 | 52 | 900 | 1.0 |
| tabLabel | 12 | 14 | 500/700 | 1.2 |
| chevron | 20 | 26 | 400 | 1.0 |

(FontWeight 900 = Black. Noto Sans KR Black 웨이트 포함 필요.)

### 0-3. 반경·크기·간격 토큰 (dp)

- 반경: card 20/22, button 18/20, iconBox 16/18, pill 999, input 18/20
- 크기: btnPrimaryH 58/70, btnSecondaryH 54/66, iconBox 52/62, tabIcon 21/26, homeCircle 210/240(+4dp 흰테두리 어르신), scoreCircle 74/(이모지 대체), switch 54×32 / 64×38, statusCircle 84/100, borderCard 0/2, borderInput 2/3
- 간격: screenX 24, cardPad 20/22, stackGap 12/16, listGap 12, navbar 10·8·18 / 12·6·20

### 0-4. 그림자 (Compose 노트)
- CSS 다중 그림자 → Compose는 `Modifier.shadow(elevation, shape, ambientColor, spotColor)`로 근사. 카드 8dp / 버튼 6dp / 히어로 12dp 정도 + spotColor를 브랜드색으로.
- 폰 목업 이중 그림자는 앱 화면엔 불필요(스펙의 마케팅 목업용).

### 0-5. 폰트 등록
- `app/src/main/res/font/`에 Noto Sans KR (Regular/Medium/Bold/Black) 추가 → `FontFamily` 정의. 또는 Google Downloadable Fonts.
- APK 용량 vs 오프라인: 번들 권장(어르신 대상, 네트워크 의존 최소화).

**Phase 0 산출물**: 위 파일들 + `FishingstopTheme(elder)` + `FishingStopApp` density 해킹 제거. 화면은 아직 기존 그대로(빌드만 통과).

---

## Phase 1 — 공통 컴포넌트 (스펙 1-7)

`core/ui/components/`에 토큰 기반 위젯 신설/교체:

- `PrimaryButton`(h58/70·r18/20·green, shadow) — 기존 것 대체
- `SecondaryButton`(흰 bg·border 2/3px)
- `AppCard`(r20/22·pad·shadow, 어르신 border) — Material Card 래핑
- `ListRow`(iconBox + 제목/설명 + chevron)
- `StatusBadge`(pill chip, 등급별 색)
- `FilterChip`(활성 green / 비활성 흰+border, nowrap)
- `AppTextField`(r18/20·border 2/3px)
- `AppScaffold`(pageBg 크림) / `AppTopBar`(← + 제목) / `AppBottomBar`(5탭, tabIcon·label 토큰, navbarBg·border)
- `WarnBox`(warn 색), `Chevron`

각 위젯은 `AppTheme`에서 토큰을 읽어 일반/어르신 자동 대응.

---

## Phase 2 — 화면별 적용 (스펙 2절)

우선순위 순서로 각 화면을 Phase 1 위젯 + 토큰으로 재구성:

1. 홈(2-1): Greeting + StartCircle + HintCard — **StartCircle은 기존 `RippleWaves` 물결 애니메이션 유지**, 색/크기만 토큰화(위 「인터랙션 보존 ①」). 인라인 `NavigationBar`는 `AppBottomBar`로 교체하되 **색 변화 선택 표시 유지**(「인터랙션 보존 ②」).
2. 직접검사(2-2): ScanMethodRow ×4 (ListRow)
3. 결과 안전/위험(2-6,2-7): ResultHero + ContentCard + ReasonCard + Caution/Action + Buttons
4. 검사기록(2-5): FilterChip + HistoryCard(StatusBadge+Star)
5. 신고/완료(2-11,2-12): InfoTable, PhoneInput / SuccessIcon+HotlineCard(1394)
6. 링크·문자·이미지검사(2-3,2-4): GuideCard + Input/DropZone
7. 교육 목록·상세(2-8): CategoryRow / Warn·Tip 카드
8. 설정(2-9) + 공지(2-10): ThemeRadioGroup, AccessibilityToggle / NoticeCard(NEW 뱃지)

---

## Phase 3 — 어르신 레이아웃 분기 (토큰만으론 불가)

`if (AppTheme.elder) {…}` 또는 화면 variant로 처리:

- **결과 화면**: 일반=점수원(74) 중심 / 어르신=상태 이모지(😊·⚠️ 64sp) + "① 링크 안누르기 ② 답장 안하기 ③ 송금 안하기" 3단계 리스트
- **교육 목록**: 어르신=상위 5개 + "더보기"
- **직접검사**: 어르신=문자 검사 우선 정렬
- **신고완료**: 어르신=신고번호 강조 생략, 1394만 크게
- **문투**: 어르신=①②③ 단계·"~하세요" 쉬운 말 (문자열 리소스 2벌 또는 elder 분기)

---

## 리스크 / 주의

- **작업량 큼**: Phase 2가 12개 화면 재작성이라 가장 큼. Phase 0→1 완료 시점에 한 번 리뷰 권장.
- **다크 토큰은 제안값** — 실기기에서 대비 확인·튜닝 필요(특히 warn/danger on dark).
- **어르신×다크 4조합** 전부 육안 확인 필요.
- **Noto Sans KR Black(900)** 웨이트 누락 시 굵기 안 나옴 → 폰트 파일 확인.
- **sp + 어르신 토큰 + 시스템 글꼴 배율** 삼중 확대 가능 → 어르신 토큰은 "기본 대비 절대값"으로, 시스템 배율은 sp가 자연 반영(정상). 과확대 시 상한 고려.
- 기존 `core/ui/components/PrimaryButton`·`DisclaimerText` 등과 충돌 → 교체 시 전 화면 참조 갱신.

## 완료 기준
- [x] Phase 0: 토큰 파일 + 테마 개편 + density 해킹 제거, 빌드 통과 (커밋 02f18a2). 폰트는 TODO(AppFontFamily=Default, Noto Sans KR 미등록)
- [x] Phase 1: 공통 위젯 세트, 일반/어르신 토큰 반영 (커밋 02f18a2)
- [x] Phase 2: 17개 화면 토큰 적용 (**인터랙션 보존**: 홈 물결 애니메이션·탭바 색 변화 선택 표시 유지) (커밋 22c94e9)
- [x] Phase 3: 어르신 레이아웃 분기 (결과 이모지+3단계 / 직접검사 문자우선 / 교육 상위5+더보기 / 신고완료 1394강조) (커밋 22c94e9)
- [x] 실기기: 일반/어르신 × 라이트/다크 4조합 육안 확인 (2026-07-16)

> **남은 TODO**: Noto Sans KR 폰트 등록(AppType.AppFontFamily 한 줄 교체), 다크 토큰 대비 미세 튜닝, 신고완료 화면 어르신 1394강조는 코드만 반영(Firestore 필요해 실기기 미확인).
