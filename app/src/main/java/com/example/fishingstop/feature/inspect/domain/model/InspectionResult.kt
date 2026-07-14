package com.example.fishingstop.feature.inspect.domain.model

import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.core.util.RiskLevel

/**
 * 저장된 검사 1건(도메인 모델). Room 엔티티와 분리해, UI/도메인은 enum 등 타입 안전한 형태로 다룬다.
 */
data class InspectionResult(
    val id: Long,
    val createdAt: Long,
    val method: InspectMethod,
    val inputText: String,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val advice: String,
    val signals: List<String>,
    val isFavorite: Boolean,
    val isReported: Boolean
)
