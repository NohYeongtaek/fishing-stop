package com.example.fishingstop.features.risk_engine.data.dto

/**
 * Gemini 응답 원본을 그대로 담는 DTO.
 * domain의 RiskAnalysisResult와 분리해두는 이유: AI 응답 형식이 바뀌어도
 * 이 파일과 매퍼(RiskAnalysisRepositoryImpl)만 고치면 domain·화면 쪽은 영향받지 않는다.
 */
data class RiskAnalysisDto(
    val score: Int,
    val level: String,
    val reasons: List<String>,
    val advice: String,
)
