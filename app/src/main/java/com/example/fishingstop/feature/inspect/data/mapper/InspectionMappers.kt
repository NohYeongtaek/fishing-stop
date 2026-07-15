package com.example.fishingstop.feature.inspect.data.mapper

import com.example.fishingstop.core.database.entity.InspectionEntity
import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.core.util.RiskLevel
import com.example.fishingstop.feature.inspect.data.dto.AnalysisResponseDto
import com.example.fishingstop.feature.inspect.domain.model.InspectionResult
import com.example.fishingstop.feature.inspect.domain.model.RiskAnalysis

/**
 * DTO/Entity ↔ 도메인 모델 변환 모음.
 * 계층 간 타입을 분리해 UI/도메인이 문자열이 아닌 enum 등 안전한 타입을 다루게 한다.
 */

/**
 * AI 응답(DTO) → 도메인 분석 결과.
 * 등급(riskLevel)은 모델의 문자열을 신뢰하기보다 점수(riskScore) 기준으로 재계산해
 * "점수와 등급 불일치"를 방지한다.
 */
fun AnalysisResponseDto.toRiskAnalysis(): RiskAnalysis {
    val score = riskScore.coerceIn(0, 100)
    return RiskAnalysis(
        riskScore = score,
        riskLevel = RiskLevel.fromScore(score),
        signals = signals,
        advice = advice
    )
}

/** Room 엔티티 → 도메인 모델. enum 파싱 실패 시 안전하게 보정한다. */
fun InspectionEntity.toDomain(): InspectionResult = InspectionResult(
    id = id,
    createdAt = createdAt,
    method = runCatching { InspectMethod.valueOf(method) }.getOrDefault(InspectMethod.TEXT),
    inputText = inputText,
    riskScore = riskScore,
    riskLevel = runCatching { RiskLevel.valueOf(riskLevel) }.getOrElse { RiskLevel.fromScore(riskScore) },
    advice = advice,
    signals = signals,
    isFavorite = isFavorite,
    isReported = isReported
)
