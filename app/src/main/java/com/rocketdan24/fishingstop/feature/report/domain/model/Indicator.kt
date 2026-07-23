package com.rocketdan24.fishingstop.feature.report.domain.model

/**
 * 신고 대상 "지표(indicator)" — 가해자를 특정할 수 있는 값.
 *
 * 검사 방법(문자/링크/QR/이미지)마다 확보 가능한 값이 다르므로, 고정 필드가 아니라
 * 지표 목록으로 담아 신고 종류별 차이를 유연하게 흡수한다.
 *
 * @property type   지표 종류(전화번호/링크/계좌/SNS/앱)
 * @property value  실제 값(예: "010-1234-5678", "http://bit.ly/xxx")
 * @property source 어떻게 얻었는지(자동추출 / 사용자 수동입력)
 */
data class Indicator(
    val type: IndicatorType,
    val value: String,
    val source: IndicatorSource
)

/** 지표 종류. label은 화면 표시에 사용한다. */
enum class IndicatorType(val label: String) {
    PHONE("전화번호"),
    URL("링크"),
    ACCOUNT("계좌번호"),
    SNS("SNS 계정"),
    APP("앱")
}

/** 지표 출처. */
enum class IndicatorSource {
    /** 원문에서 정규식으로 자동 추출 */
    AUTO,

    /** 사용자가 직접 입력 */
    MANUAL
}
