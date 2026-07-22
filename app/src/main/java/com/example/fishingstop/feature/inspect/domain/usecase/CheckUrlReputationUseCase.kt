package com.example.fishingstop.feature.inspect.domain.usecase

import com.example.fishingstop.core.util.RiskLevel
import com.example.fishingstop.feature.inspect.domain.UrlRiskAnalyzer
import com.example.fishingstop.feature.inspect.domain.mergeRiskAnalyses
import com.example.fishingstop.feature.inspect.domain.model.RiskAnalysis
import com.example.fishingstop.feature.inspect.domain.model.SafeBrowsingVerdict
import com.example.fishingstop.feature.inspect.domain.repository.SafeBrowsingRepository
import com.example.fishingstop.feature.inspect.domain.repository.UrlRedirectResolver
import com.example.fishingstop.feature.inspect.domain.repository.UrlVisitAnalysisRepository
import javax.inject.Inject

/**
 * 대표 URL 하나의 평판을 조회한다: 실제로 HEAD 요청해보고 리다이렉트가 발견되면(등록된
 * 단축 서비스 목록이 아닌 "행동" 기준 — 목록에 없는 QR 생성기·자체 리다이렉터도 잡아낸다)
 * 그 목적지로 재평가하고, Google Safe Browsing(1차, 알려진 위협이면 즉시 확정) → 매치가
 * 없을 때만 Gemini의 URL Context 도구로 실제 페이지를 방문해 분석(2차)까지 확인한다.
 *
 * 링크 직접검사([AnalyzeUrlUseCase])와 문자 종합검사 내 URL 평판 확인([AnalyzeMessageUseCase])이
 * 이 로직을 공유해, 문자에 포함된 링크도 링크 직접검사와 동일한 수준으로 검증되게 한다.
 * 모든 원격 조회는 실패해도(네트워크 오류 등) 예외를 던지지 않는다 — 호출측이 보조 신호로만 취급.
 */
class CheckUrlReputationUseCase @Inject constructor(
    private val urlRiskAnalyzer: UrlRiskAnalyzer,
    private val urlRedirectResolver: UrlRedirectResolver,
    private val safeBrowsingRepository: SafeBrowsingRepository,
    private val urlVisitAnalysisRepository: UrlVisitAnalysisRepository
) {
    /**
     * @param url 평판을 조회할 대표 URL
     * @param safeDomains 화이트리스트(리다이렉트로 밝혀진 목적지 재평가에 사용)
     * @return 위험 신호를 하나라도 얻었으면 병합된 결과, 전부 실패/무신호면 null
     */
    suspend operator fun invoke(url: String, safeDomains: Set<String>): RiskAnalysis? {
        var effectiveUrl = url
        var redirectAnalysis: RiskAnalysis? = null

        // 등록된 "알려진 단축 서비스" 목록 여부와 무관하게 항상 한 번 HEAD로 찔러보고,
        // 실제로 리다이렉트가 나오면(=행동 기준) 그 목적지를 기준으로 재평가한다.
        // 실패해도(네트워크 오류 등) 원본 URL 기준으로 계속 진행한다.
        val originalHost = urlRiskAnalyzer.hostOf(url)
        val resolvedHost = runCatching { urlRedirectResolver.resolveFinalHost(url) }.getOrNull()
        if (resolvedHost != null && resolvedHost != originalHost) {
            val resolved = urlRiskAnalyzer.analyzeResolvedHost(resolvedHost, safeDomains)
            redirectAnalysis = resolved.copy(
                signals = (listOf("실제 목적지: $resolvedHost") + resolved.signals).distinct().take(5)
            )
            effectiveUrl = resolvedHost
        }

        val remote = analyzeRemote(effectiveUrl)
        return when {
            redirectAnalysis == null -> remote
            else -> mergeRiskAnalyses(redirectAnalysis, remote)
        }
    }

    /**
     * 1차: Safe Browsing 평판 조회. 매치되면 그 결과로 즉시 확정(2차 생략).
     * 매치가 없거나(Clean) 조회에 실패하면(Unknown) 2차(AI 방문 분석)로 넘어간다.
     */
    private suspend fun analyzeRemote(url: String): RiskAnalysis? {
        val verdict = runCatching { safeBrowsingRepository.checkUrl(url) }
            .getOrDefault(SafeBrowsingVerdict.Unknown)

        if (verdict is SafeBrowsingVerdict.Threat) {
            return safeBrowsingThreatAnalysis(verdict)
        }

        return runCatching { urlVisitAnalysisRepository.analyzeVisit(url) }.getOrNull()
    }

    /** Safe Browsing 매치를 즉시 위험 확정 결과로 변환한다. */
    private fun safeBrowsingThreatAnalysis(threat: SafeBrowsingVerdict.Threat): RiskAnalysis =
        RiskAnalysis(
            riskScore = 100,
            riskLevel = RiskLevel.DANGER,
            signals = listOf(
                "Google Safe Browsing에 위험 사이트로 등록되어 있습니다" +
                    (threat.threatTypes.takeIf { it.isNotEmpty() }?.let { " (${it.joinToString()})" } ?: "") + "."
            ),
            advice = "이 링크는 이미 확인된 악성/피싱 사이트입니다. 절대 열지 말고 즉시 삭제하세요."
        )
}
