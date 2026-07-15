package com.example.fishingstop.features.risk_engine.domain.entities

/**
 * 위험도 등급.
 * 점수 구간: SAFE 0~30 / WARNING 30~75 / DANGER 75~100
 */
enum class RiskLevel {
    SAFE,
    WARNING,
    DANGER;

    companion object {
        fun fromScore(score: Int): RiskLevel = when {
            score >= 75 -> DANGER
            score >= 30 -> WARNING
            else -> SAFE
        }
    }
}
