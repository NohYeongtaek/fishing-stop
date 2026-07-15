package com.example.fishingstop.feature.inspect.domain.model

import com.example.fishingstop.core.util.RiskLevel

/**
 * AI 분석의 순수 결과(저장/식별자 이전의 값).
 *
 * @property riskScore AI 참고 점수(0~100). UI에선 작게만 표시.
 * @property riskLevel 점수로부터 도출한 등급(안전/주의/위험). 표시의 중심.
 * @property signals   위험/안전 판단 근거 목록(예: "수사기관 사칭 정황", "출처 불명 단축 URL").
 * @property advice    사용자 행동 권고 문구(예: "링크를 절대 열지 마세요").
 */
data class RiskAnalysis(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val signals: List<String>,
    val advice: String
)
