package com.example.fishingstop.feature.inspect.domain.usecase

import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.feature.inspect.domain.UrlRiskAnalyzer
import com.example.fishingstop.feature.inspect.domain.mergeRiskAnalyses
import com.example.fishingstop.feature.inspect.domain.repository.InspectionRepository
import com.example.fishingstop.feature.inspect.domain.repository.WhitelistRepository
import javax.inject.Inject

/**
 * URL(링크/QR) 검사 유스케이스 — "휴리스틱(+리다이렉트 추적) + 2단계 평판 조회" 병합(기획 확정).
 *
 * 1) 로컬 휴리스틱([UrlRiskAnalyzer])으로 입력 전체를 즉시 검사하고,
 * 2) 대표 URL의 평판을 [CheckUrlReputationUseCase]로 조회한다(단축 URL 리다이렉트 추적 →
 *    Safe Browsing 1차 → 매치 없으면 Gemini 방문 분석 2차 — 문자 검사와 로직을 공유한다),
 * 3) 두 결과를 보수적으로(더 위험한 쪽 기준) 병합해 저장한다.
 * 4) 원격 조회는 실패해도(네트워크 오류 등) 나머지 결과만으로 진행한다 → 오프라인에서도 동작.
 *
 * @return 성공 시 저장된 검사 기록 id
 */
class AnalyzeUrlUseCase @Inject constructor(
    private val repository: InspectionRepository,
    private val urlRiskAnalyzer: UrlRiskAnalyzer,
    private val whitelistRepository: WhitelistRepository,
    private val checkUrlReputationUseCase: CheckUrlReputationUseCase
) {
    suspend operator fun invoke(
        text: String,
        method: InspectMethod = InspectMethod.LINK
    ): Result<Long> = runCatching {
        val input = text.trim()

        // 안심 도메인(화이트리스트)을 반영해 휴리스틱 검사
        val safeDomains = whitelistRepository.getSafeDomains()
        val heuristic = urlRiskAnalyzer.analyze(input, safeDomains)

        // 대표 URL의 평판(리다이렉트 추적 + Safe Browsing + AI 방문 분석)은 보조 신호:
        // 실패해도 검사 자체는 계속된다(오프라인 지원).
        val worstUrl = urlRiskAnalyzer.findWorstUrl(input, safeDomains)
        val reputation = worstUrl?.let { checkUrlReputationUseCase(it, safeDomains) }

        val merged = mergeRiskAnalyses(heuristic, reputation)
        repository.save(text = input, method = method, analysis = merged)
    }
}
