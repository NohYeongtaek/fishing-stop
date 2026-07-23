package com.rocketdan24.fishingstop.feature.inspect.domain

import com.rocketdan24.fishingstop.core.util.RiskLevel
import com.rocketdan24.fishingstop.feature.inspect.domain.model.RiskAnalysis

/**
 * 두 분석 결과를 보수적으로(더 위험한 쪽 기준) 병합한다.
 * 점수는 더 높은 쪽, 근거는 합집합(중복 제거, 최대 5개), 조언은 더 위험한 쪽 것을 우선한다
 * (단, 비어 있으면 a의 조언으로 보정).
 *
 * URL 검사(휴리스틱+평판)와 문자 검사(텍스트 분류+URL 평판)처럼, 서로 다른 소스의 판정을
 * 하나의 결과로 합칠 때 공통으로 쓴다. b가 null이면(신호 없음/조회 실패) a를 그대로 반환한다.
 */
fun mergeRiskAnalyses(a: RiskAnalysis, b: RiskAnalysis?): RiskAnalysis {
    if (b == null) return a
    val score = maxOf(a.riskScore, b.riskScore)
    val riskier = if (b.riskScore > a.riskScore) b else a
    return RiskAnalysis(
        riskScore = score,
        riskLevel = RiskLevel.fromScore(score),
        signals = (a.signals + b.signals).distinct().take(5),
        advice = riskier.advice.ifBlank { a.advice }
    )
}
