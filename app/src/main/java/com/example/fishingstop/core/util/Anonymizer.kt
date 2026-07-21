package com.example.fishingstop.core.util

/**
 * 신고 전 개인정보를 마스킹(익명화)한다.
 *
 * 스펙의 안전·개인정보 원칙("신고 시 개인정보를 제거·익명화해서 저장")을 위해,
 * 전화번호·주민번호·계좌/카드 등 숫자 열과 이메일을 라벨로 치환한다.
 * 순수 JVM(정규식)만 사용해 단위 테스트가 쉽다.
 */
object Anonymizer {

    private val EMAIL = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    // 주민등록번호: 6자리-7자리
    private val RRN = Regex("\\d{6}-\\d{7}")
    // 휴대폰 번호: 010-1234-5678 / 01012345678 등
    private val PHONE = Regex("01[0-9][- ]?\\d{3,4}[- ]?\\d{4}")
    // 계좌/카드 등 8자리 이상의 긴 숫자열(구분자 포함)
    private val LONG_DIGITS = Regex("\\d[\\d- ]{7,}\\d")

    /**
     * @param text 원문
     * @return 개인정보가 마스킹된 텍스트. 순서(주민번호·전화 먼저 → 남은 긴 숫자열)를 지켜
     *         과도한 마스킹을 줄인다.
     */
    fun mask(text: String): String {
        var result = text
        result = EMAIL.replace(result, "[이메일]")
        result = RRN.replace(result, "[주민번호]")
        result = PHONE.replace(result, "[전화번호]")
        result = LONG_DIGITS.replace(result, "[숫자]")
        return result
    }

    /**
     * AI 분석으로 **전송하기 전** 적용하는 선별 마스킹.
     *
     * 전면 마스킹(mask)은 전화번호·계좌 같은 값을 지워 피싱 탐지 신호까지 없애 정확도를 떨어뜨린다.
     * 반면 주민등록번호는 탐지에 전혀 필요 없으면서(있다는 사실만 신호가 됨) 가장 민감하므로,
     * 여기서는 **주민번호만** 라벨로 치환해 최소 전송 원칙과 탐지 정확도를 함께 지킨다.
     * (전화·계좌·이메일은 스미싱 판별의 근거가 될 수 있어 남긴다.)
     */
    fun maskForAi(text: String): String = RRN.replace(text, "[주민번호]")
}
