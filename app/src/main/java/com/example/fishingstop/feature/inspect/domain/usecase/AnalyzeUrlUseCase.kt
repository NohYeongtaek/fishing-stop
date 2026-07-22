package com.example.fishingstop.feature.inspect.domain.usecase

import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.core.util.RiskLevel
import com.example.fishingstop.feature.inspect.domain.UrlRiskAnalyzer
import com.example.fishingstop.feature.inspect.domain.model.RiskAnalysis
import com.example.fishingstop.feature.inspect.domain.model.SafeBrowsingVerdict
import com.example.fishingstop.feature.inspect.domain.repository.InspectionRepository
import com.example.fishingstop.feature.inspect.domain.repository.SafeBrowsingRepository
import com.example.fishingstop.feature.inspect.domain.repository.UrlRedirectResolver
import com.example.fishingstop.feature.inspect.domain.repository.UrlVisitAnalysisRepository
import com.example.fishingstop.feature.inspect.domain.repository.WhitelistRepository
import javax.inject.Inject

/**
 * URL(링크/QR) 검사 유스케이스 — "휴리스틱(+리다이렉트 추적) + 2단계 평판 조회" 병합(기획 확정).
 *
 * 1) 로컬 휴리스틱([UrlRiskAnalyzer])으로 즉시 검사하고,
 * 2) 대표 URL을 실제로 HEAD 요청해보고 리다이렉트가 발견되면(도메인 목록이 아닌 "행동" 기준)
 *    실제 목적지를 재채점한 뒤 병합하고,
 * 3) 대표(또는 리다이렉트로 밝혀진) URL을 Google Safe Browsing으로 조회한다.
 *    - 매치(이미 알려진 악성/피싱 URL)면 그 즉시 위험(DANGER)으로 확정하고 4)는 생략한다
 *      (비용·속도상 이점 + 이미 확인된 위협이므로 추가 판단이 불필요).
 *    - 매치가 없으면(신종 URL일 수 있음) 4)로 넘어간다.
 * 4) Safe Browsing이 매치를 못 찾았을 때만, Gemini의 URL Context 도구가 그 URL의 실제 웹페이지
 *    내용을 직접 가져와 분석한 결과([UrlVisitAnalysisRepository])를 보조로 더한다.
 * 5) 위 모든 원격 조회는 실패해도(네트워크 오류 등) 나머지 결과만으로 진행한다 → 오프라인에서도 동작.
 *    (점수는 더 높은 쪽, 근거는 합집합 — 안전 판정 실수를 줄이는 보수적 병합)
 *
 * @return 성공 시 저장된 검사 기록 id
 */
class AnalyzeUrlUseCase @Inject constructor(
    private val repository: InspectionRepository,
    private val urlRiskAnalyzer: UrlRiskAnalyzer,
    private val urlRedirectResolver: UrlRedirectResolver,
    private val whitelistRepository: WhitelistRepository,
    private val safeBrowsingRepository: SafeBrowsingRepository,
    private val urlVisitAnalysisRepository: UrlVisitAnalysisRepository
) {
    suspend operator fun invoke(
        text: String,
        method: InspectMethod = InspectMethod.LINK
    ): Result<Long> = runCatching {
        val input = text.trim()

        // 안심 도메인(화이트리스트)을 반영해 휴리스틱 검사
        val safeDomains = whitelistRepository.getSafeDomains()
        var heuristic = urlRiskAnalyzer.analyze(input, safeDomains)

        // 대표 URL은 목록에 등록된 "알려진 단축 서비스"인지와 무관하게 항상 한 번 HEAD로
        // 찔러보고, 실제로 리다이렉트가 나오면(=행동 기준) 그 목적지로 재채점 후 병합한다.
        // 등록된 단축 서비스 목록에 없는 QR 생성기·리다이렉터도 이렇게 잡아낸다.
        // (실패해도 검사 자체는 계속된다 — 오프라인/네트워크 오류 대응)
        var effectiveUrl = urlRiskAnalyzer.findWorstUrl(input, safeDomains)
        if (effectiveUrl != null) {
            val originalHost = urlRiskAnalyzer.hostOf(effectiveUrl)
            val resolvedHost = runCatching { urlRedirectResolver.resolveFinalHost(effectiveUrl) }
                .getOrNull()
            if (resolvedHost != null && resolvedHost != originalHost) {
                val resolved = urlRiskAnalyzer.analyzeResolvedHost(resolvedHost, safeDomains)
                heuristic = mergeResolved(heuristic, resolved, resolvedHost)
                effectiveUrl = resolvedHost
            }
        }

        // 원격 평판/방문 분석은 보조 신호: 실패해도 검사 자체는 계속된다(오프라인 지원).
        val remoteAnalysis = effectiveUrl?.let { url -> analyzeRemote(url) }

        val merged = merge(heuristic, remoteAnalysis)
        repository.save(text = input, method = method, analysis = merged)
    }

    /**
     * 1차: Safe Browsing 평판 조회. 매치되면 그 결과로 즉시 확정(2차 생략).
     * 매치가 없거나 조회에 실패하면(Unknown) 2차(AI 방문 분석)로 넘어간다.
     */
    private suspend fun analyzeRemote(url: String): RiskAnalysis? {
        val verdict = runCatching { safeBrowsingRepository.checkUrl(url) }
            .getOrDefault(SafeBrowsingVerdict.Unknown)

        if (verdict is SafeBrowsingVerdict.Threat) {
            return safeBrowsingThreatAnalysis(verdict)
        }

        // Clean(매치 없음) 또는 Unknown(API 키 미설정·오류) → 2차 AI 방문 분석 시도
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

    /** 원본 휴리스틱 결과에 "실제 목적지" 채점 결과를 보수적으로(더 위험한 쪽 기준) 합친다. */
    private fun mergeResolved(
        heuristic: RiskAnalysis,
        resolved: RiskAnalysis,
        resolvedHost: String
    ): RiskAnalysis {
        val score = maxOf(heuristic.riskScore, resolved.riskScore)
        val riskier = if (resolved.riskScore > heuristic.riskScore) resolved else heuristic
        return RiskAnalysis(
            riskScore = score,
            riskLevel = RiskLevel.fromScore(score),
            signals = (listOf("실제 목적지: $resolvedHost") + heuristic.signals + resolved.signals)
                .distinct().take(5),
            advice = riskier.advice.ifBlank { heuristic.advice }
        )
    }

    /** 휴리스틱과 원격(Safe Browsing/AI 방문) 결과를 보수적으로(더 위험한 쪽 기준) 병합한다. */
    private fun merge(heuristic: RiskAnalysis, remote: RiskAnalysis?): RiskAnalysis {
        if (remote == null) return heuristic

        val score = maxOf(heuristic.riskScore, remote.riskScore)
        val riskier = if (remote.riskScore > heuristic.riskScore) remote else heuristic
        return RiskAnalysis(
            riskScore = score,
            riskLevel = RiskLevel.fromScore(score),
            // 근거는 합집합(중복 제거, 최대 5개) — 휴리스틱 근거를 앞에 둔다.
            signals = (heuristic.signals + remote.signals).distinct().take(5),
            advice = riskier.advice.ifBlank { heuristic.advice }
        )
    }
}
