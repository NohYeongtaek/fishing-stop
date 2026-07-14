package com.example.fishingstop.feature.inspect.data.dto

import kotlinx.serialization.Serializable

/**
 * Gemini가 반환하는 JSON을 파싱하기 위한 DTO.
 * 모델이 일부 필드를 빠뜨려도 앱이 죽지 않도록 전부 기본값을 둔다.
 *
 * 계약(프롬프트에서 강제하는 JSON 형태):
 * { "riskScore": 0~100, "riskLevel": "SAFE|WARNING|DANGER",
 *   "signals": ["근거1", ...], "advice": "행동 권고" }
 */
@Serializable
data class AnalysisResponseDto(
    val riskScore: Int = 0,
    val riskLevel: String = "WARNING",
    val signals: List<String> = emptyList(),
    val advice: String = ""
)
