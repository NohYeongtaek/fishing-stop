package com.example.fishingstop.features.risk_engine.data.repositories

import com.example.fishingstop.features.risk_engine.data.datasources.GeminiRiskDataSource
import com.example.fishingstop.features.risk_engine.domain.entities.RiskAnalysisResult
import com.example.fishingstop.features.risk_engine.domain.entities.RiskLevel
import com.example.fishingstop.features.risk_engine.domain.repositories.RiskAnalysisRepository
import javax.inject.Inject

class RiskAnalysisRepositoryImpl @Inject constructor(
    private val dataSource: GeminiRiskDataSource,
) : RiskAnalysisRepository {

    override suspend fun analyze(text: String, urls: List<String>): RiskAnalysisResult {
        val dto = dataSource.fetchRiskAnalysis(text = text, urls = urls)

        // level 문자열이 예상 밖의 값으로 오는 경우를 대비해 score 기반 계산으로 안전하게 대체한다.
        val level = runCatching { RiskLevel.valueOf(dto.level) }
            .getOrDefault(RiskLevel.fromScore(dto.score))

        return RiskAnalysisResult(
            level = level,
            score = dto.score.coerceIn(0, 100),
            reasons = dto.reasons,
            advice = dto.advice,
        )
    }
}
