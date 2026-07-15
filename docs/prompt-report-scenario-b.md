# 프롬프트: 신고하기 — 시나리오 B(수동 대신신고) 구현

> 이 문서는 실행용 프롬프트입니다. 이 내용을 Claude Code에 그대로 붙여넣으면
> 아래 변경사항을 구현합니다. 아직 구현되지 않은 상태입니다(계획만 확정됨).

## 배경 / 팀 결정사항

기존 신고 기능은 "통계용 익명 메타데이터"(등급·점수·근거)만 Firestore로 보내고
원문은 전송하지 않았습니다. 팀 논의 결과 신고의 실제 목적은
**"우리가 데이터를 모아뒀다가 담당자가 수동으로 경찰(counterscam112/1394)에
대신 신고"**(시나리오 B)로 바뀌었고, 아래 6개 항목이 확정되었습니다.

| # | 결정 | 의미 |
|---|---|---|
| 1 | 발신번호: 자동추출 + 수동입력 | 원문에서 정규식으로 시도 + 사용자가 직접 입력 가능 |
| 2 | 이미지는 텍스트만 | OCR 결과 텍스트만 전송, 원본 이미지 파일은 전송 안 함(기존과 동일 유지) |
| 3 | 지표는 선택 확인 | 자동추출된 지표를 사용자가 체크박스로 확인해야 신고에 포함 |
| 4 | 익명 | 신고자 식별 정보(연락처 등) 수집 안 함 |
| 5 | 전달은 수동 | 자동 접수 API 없음. 담당자가 Firestore 콘솔에서 조회 → 수동으로 기관 접수 |
| 6 | 처리 완료 시 파기 | 접수 완료 후 담당자가 `status`를 갱신하고 문서를 삭제 |

**핵심 설계**: 검사 방법(문자/링크/QR/이미지)마다 확보 가능한 지표가 다르므로,
고정 필드가 아니라 `indicators: [{type, value, source}]` 배열로 유연하게 담습니다.

## 현재 코드 상태 (구현 시작 전 확인 필수)

- `feature/report/domain/model/Report.kt` — 현재 필드: `reportNumber, riskLevel,
  riskScore, method, signals, createdAt`. **원문(inputText) 없음.**
- `feature/report/domain/SubmitReportUseCase.kt` — 검사 기록에서 등급/점수/근거만
  뽑아 `Report`를 만들고 `Anonymizer.mask()`로 signals를 마스킹 후 저장.
  성공 시 `inspectionRepository.markReported(id)` 호출.
- `feature/report/data/ReportRepositoryImpl.kt` — Firestore `reports` 컬렉션에
  6개 필드만 write. `withTimeoutOrNull(10_000L)`로 감싸져 있음(무한대기 방지 — 유지할 것).
- `feature/report/presentation/ReportScreen.kt` — `ReadyContent`(전송정보 미리보기)
  → `익명으로 신고하기` 버튼 → `SuccessContent`(신고번호 + 1394 + counterscam112 링크).
- `feature/inspect/domain/UrlRiskAnalyzer.kt` — URL 추출 정규식(`URL_REGEX`)이
  이미 있음. 지표 자동추출 시 이 로직을 재사용/참고할 것(중복 구현 금지).
- `core/util/Anonymizer.kt` — 전화번호/주민번호/계좌/이메일 마스킹 정규식 보유.
  **주의**: 지표 자동추출용 전화번호 정규식은 Anonymizer의 마스킹용 정규식과
  다른 목적(추출 vs 은닉)이므로 별도 함수로 만들되 패턴은 참고할 것.
- `feature/inspect/domain/InspectionResult.kt` — `inputText` 필드 보유(로컬 Room
  에는 원문이 이미 저장되어 있음 — 신고 시 이걸 읽어서 지표를 뽑으면 됨).

## 구현 범위

### 1. domain 계층

**`Indicator` 모델 신설** (`feature/report/domain/model/Indicator.kt`)
```kotlin
data class Indicator(
    val type: IndicatorType,   // PHONE, URL, ACCOUNT, SNS, APP
    val value: String,
    val source: IndicatorSource // AUTO, MANUAL
)
enum class IndicatorType { PHONE, URL, ACCOUNT, SNS, APP }
enum class IndicatorSource { AUTO, MANUAL }
```

**`Report` 모델 확장**
```kotlin
data class Report(
    val reportNumber: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val method: InspectMethod,
    val signals: List<String>,
    val contentText: String,       // 신설: 원문(raw, 마스킹 없음 — 증거 목적)
    val indicators: List<Indicator>, // 신설: 사용자가 확인한 신고 대상
    val createdAt: Long,
    val status: String = "received", // 신설
    val handledAt: Long? = null       // 신설: 처리 완료 시각(파기 트리거)
)
```

**`ExtractIndicatorsUseCase` 신설** (`feature/report/domain/ExtractIndicatorsUseCase.kt`)
- 입력: 원문 텍스트
- 출력: `List<Indicator>` (source=AUTO)
- URL: `UrlRiskAnalyzer`의 `URL_REGEX` 참고해 URL 타입으로 추출
- 전화번호: 정규식 `01[0-9][- ]?\d{3,4}[- ]?\d{4}` (Anonymizer의 PHONE 패턴과 동일 소스 사용,
  단 마스킹이 아니라 원본 값을 그대로 반환)
- 계좌번호: Anonymizer의 LONG_DIGITS 패턴 참고 (오탐 많을 수 있어 낮은 우선순위)
- SNS/APP: 자동추출 안 함(정확도 낮음, 수동 전용)

**`SubmitReportUseCase` 수정**
- 시그니처 변경: `invoke(inspectionId: Long, confirmedIndicators: List<Indicator>, manualPhone: String?)`
- 검사 기록(`inspectionRepository.getResult`)에서 `inputText`를 그대로 `contentText`에 담음
  (⚠️ 여기서는 마스킹하지 않음 — 증거 목적. 기존처럼 signals만 참고용으로 마스킹 유지)
- `confirmedIndicators` + `manualPhone`(입력됐으면 PHONE/MANUAL 지표로 추가)을 합쳐 `indicators`에 저장
- 기존과 동일하게 성공 시 `markReported` 호출

### 2. data 계층

**`ReportRepositoryImpl.submit()` 수정**
- Firestore write 필드에 `contentText`, `indicators`(map 리스트로 직렬화),
  `status`, `handledAt` 추가
- `indicators`는 `listOf(mapOf("type" to ..., "value" to ..., "source" to ...))` 형태로 변환

### 3. presentation 계층

**`ReportViewModel` 수정**
- `ReportUiState.Ready`에 필드 추가:
  - `autoIndicators: List<Indicator>` (체크박스 초기 상태, 기본 전부 선택 해제 — 사용자가 명시적으로 선택)
  - `confirmedIndicatorIndices: Set<Int>` (체크된 항목)
  - `manualPhone: String` (직접입력 텍스트)
- `init` 블록에서 `ExtractIndicatorsUseCase`로 자동추출 실행 → `autoIndicators`에 반영
- `toggleIndicator(index)`, `setManualPhone(value)` 함수 추가
- `submit()` 호출 시 확정된 지표 목록을 `SubmitReportUseCase`에 전달

**`ReportScreen.ReadyContent` UI 추가**
- 기존 "전송되는 정보" 카드는 유지
- **신설 섹션 "신고 대상 확인"**:
  - 자동추출 지표를 체크박스 리스트로 표시 (`type` 라벨 + `value`, 예: "전화번호: 010-1234-5678")
  - 발신번호 직접 입력 `OutlinedTextField` (라벨: "발신 전화번호(선택)")
  - 안내 문구: "체크한 항목만 신고에 포함됩니다. 문자 원문도 함께 전송됩니다."
- 미리보기 카드에 "원문 포함됨" 표시 추가 (기존 "메시지 원문과 개인정보는 전송되지 않습니다" 문구는
  **반드시 삭제** — 이제 사실이 아님)

### 4. 동의/처리방침 (필수 — 빠뜨리면 안 됨)

**`ConsentScreen.kt`** 동의 문구에 추가:
> "신고 시 문자 원문·발신번호 등 신고에 필요한 정보를 수집·보관하며, 수사기관 신고
> 목적으로 담당자가 확인·전달할 수 있습니다. 처리 완료 후 파기됩니다."

**`PrivacyPolicyScreen.kt`** 처리방침 본문에 수집항목·목적·보관기간·파기 절차 추가.

### 5. Firestore 콘솔 (코드 아님 — 수동 작업 안내만 출력할 것)

규칙에서 `hasOnly([...])` 목록에 `contentText, indicators, status, handledAt` 추가 필요.
이 부분은 **콘솔 작업이므로 코드로 처리하지 말고**, 최종 규칙 텍스트를 사용자에게
안내만 하고 끝낼 것.

## 하지 말 것 (스코프 아님)

- 실제 경찰/기관 API 연동 (존재하지 않음, 수동 전달이 확정 정책)
- 담당자용 관리 콘솔/대시보드 화면 (Firebase 콘솔에서 직접 조회하는 것으로 충분, 별도 화면 요청 없었음)
- 이미지 원본 업로드 (결정 #2에서 텍스트만으로 확정)
- 처리 완료 자동 파기 배치(Cloud Functions) — 수동 파기로 확정(#6). 자동화가
  필요해지면 별도로 논의.

## 완료 기준

- [ ] `Indicator` 모델 + `ExtractIndicatorsUseCase` (URL/전화번호 자동추출, 단위 테스트 포함)
- [ ] `Report`에 `contentText`, `indicators`, `status`, `handledAt` 반영
- [ ] `SubmitReportUseCase`가 원문 + 확정 지표를 함께 전송
- [ ] `ReportScreen`에 지표 체크박스 + 발신번호 입력 UI, "원문 미전송" 문구 제거
- [ ] 동의 화면 + 처리방침에 신고 관련 수집 문구 추가
- [ ] 실기기에서 텍스트 검사 → 신고 → 지표 체크 → 제출 → Firestore에 `contentText`,
      `indicators` 포함 저장 확인
- [ ] Firestore 규칙 갱신 텍스트를 사용자에게 안내(콘솔 작업은 사용자가 직접)
