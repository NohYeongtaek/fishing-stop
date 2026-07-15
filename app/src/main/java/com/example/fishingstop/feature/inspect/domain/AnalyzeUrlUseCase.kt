package com.example.fishingstop.feature.inspect.domain

import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.core.util.RiskLevel
import com.example.fishingstop.feature.inspect.domain.model.RiskAnalysis
import javax.inject.Inject

/**
 * URL(링크/QR) 검사 유스케이스 — "휴리스틱 + Gemini 분석" 병합(기획 확정).
 *
 * 1) 로컬 휴리스틱([UrlRiskAnalyzer])으로 즉시 검사하고,
 * 2) Gemini 분석을 추가로 시도한 뒤 두 결과를 병합한다.
 *    (점수는 더 높은 쪽, 근거는 합집합 — 안전 판정 실수를 줄이는 보수적 병합)
 * 3) AI가 실패해도(네트워크 오류 등) 휴리스틱 결과만으로 진행한다 → 오프라인에서도 동작.
 *
 * @return 성공 시 저장된 검사 기록 id
 */
class AnalyzeUrlUseCase @Inject constructor(
    private val repository: InspectionRepository,
    private val urlRiskAnalyzer: UrlRiskAnalyzer,
    private val whitelistRepository: WhitelistRepository
) {
    suspend operator fun invoke(
        text: String,
        method: InspectMethod = InspectMethod.LINK
    ): Result<Long> = runCatching {
        val input = text.trim()

        // 안심 도메인(화이트리스트)을 반영해 휴리스틱 검사
        val safeDomains = whitelistRepository.getSafeDomains()
        val heuristic = urlRiskAnalyzer.analyze(input, safeDomains)
        // AI 분석은 보조 신호: 실패해도 검사 자체는 계속된다(오프라인 지원).
        val ai = runCatching { repository.analyze(input) }.getOrNull()

        val merged = merge(heuristic, ai)
        repository.save(text = input, method = method, analysis = merged)
    }

    /** 휴리스틱과 AI 결과를 보수적으로(더 위험한 쪽 기준) 병합한다. */
    private fun merge(heuristic: RiskAnalysis, ai: RiskAnalysis?): RiskAnalysis {
        if (ai == null) return heuristic

        val score = maxOf(heuristic.riskScore, ai.riskScore)
        val riskier = if (ai.riskScore > heuristic.riskScore) ai else heuristic
        return RiskAnalysis(
            riskScore = score,
            riskLevel = RiskLevel.fromScore(score),
            // 근거는 합집합(중복 제거, 최대 5개) — 휴리스틱 근거를 앞에 둔다.
            signals = (heuristic.signals + ai.signals).distinct().take(5),
            advice = riskier.advice.ifBlank { heuristic.advice }
        )
    }
}
