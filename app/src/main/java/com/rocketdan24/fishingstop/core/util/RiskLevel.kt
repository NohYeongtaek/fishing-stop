package com.rocketdan24.fishingstop.core.util

/**
 * 위험도 3단계 등급.
 *
 * 스펙의 "단정 금지 원칙"에 따라, UI에서는 이 [RiskLevel] 등급과 판단 근거를 크게 보여주고
 * 0~100 숫자 점수는 "AI 참고 점수"라는 라벨과 함께 작게만 표시한다.
 *
 * @property minScore 이 등급에 해당하는 최소 점수(포함)
 * @property maxScore 이 등급에 해당하는 최대 점수(포함)
 */
enum class RiskLevel(val minScore: Int, val maxScore: Int) {
    /** 안전(0~30): 일상 대화, 인증번호, 단순 택배 안내 등 피싱 징후 없음 → 초록 */
    SAFE(0, 30),

    /** 주의(31~75): 출처 불명 URL, 금융/수사기관 언급, 유도성 문구, 판단 데이터 부족 등 → 노랑 */
    WARNING(31, 75),

    /** 위험(76~100): 신분증/계좌 요구, 대출 권유, 기관 사칭, 압박 문구 등 → 빨강 */
    DANGER(76, 100);

    companion object {
        /**
         * 0~100 점수를 등급으로 변환한다.
         * 범위를 벗어난 값은 안전하게 0~100으로 보정한다.
         */
        fun fromScore(score: Int): RiskLevel {
            val clamped = score.coerceIn(0, 100)
            return entries.first { clamped in it.minScore..it.maxScore }
        }
    }
}
