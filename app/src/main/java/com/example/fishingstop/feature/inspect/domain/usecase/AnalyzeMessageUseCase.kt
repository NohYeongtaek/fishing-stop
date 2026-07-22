package com.example.fishingstop.feature.inspect.domain.usecase

import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.feature.inspect.domain.UrlRiskAnalyzer
import com.example.fishingstop.feature.inspect.domain.mergeRiskAnalyses
import com.example.fishingstop.feature.inspect.domain.repository.InspectionRepository
import com.example.fishingstop.feature.inspect.domain.repository.WhitelistRepository
import javax.inject.Inject

/**
 * 메시지(텍스트)를 검사하는 유스케이스.
 *
 * 공유(ACTION_SEND), 텍스트 붙여넣기, OCR 결과 등 모든 텍스트 기반 입력이 최종적으로
 * 이 유스케이스를 통해 동일한 분석 로직을 탄다.
 *
 * 1) Gemini로 문자 전체를 분류(대화 맥락·유도 문구·압박 표현 등 텍스트 신호).
 * 2) 문자 안에 URL이 있으면, 링크 직접검사와 동일한 평판 조회([CheckUrlReputationUseCase] —
 *    단축 URL 리다이렉트 추적 → Safe Browsing 1차 → 매치 없으면 AI 방문 분석 2차)를 추가로 수행한다.
 *    텍스트 분류만으로는 URL이 실제로 알려진 악성 사이트인지, 실제 페이지 내용이 어떤지 알 수 없기
 *    때문이다(모델이 URL을 방문하지 않음).
 * 3) 두 결과를 보수적으로(더 위험한 쪽 기준) 병합해 저장한다.
 *
 * @return 성공 시 저장된 검사 기록 id. 실패(네트워크/파싱 등)는 Result.failure 로 전달.
 */
class AnalyzeMessageUseCase @Inject constructor(
    private val repository: InspectionRepository,
    private val urlRiskAnalyzer: UrlRiskAnalyzer,
    private val whitelistRepository: WhitelistRepository,
    private val checkUrlReputationUseCase: CheckUrlReputationUseCase
) {
    suspend operator fun invoke(text: String, method: InspectMethod): Result<Long> = runCatching {
        val input = text.trim()

        val textAnalysis = repository.analyze(input)

        // 문자 안에 링크가 있으면 그 URL의 평판도 함께 확인한다(실패해도 텍스트 분류 결과로 계속 진행).
        val safeDomains = whitelistRepository.getSafeDomains()
        val worstUrl = urlRiskAnalyzer.findWorstUrl(input, safeDomains)
        val urlReputation = worstUrl?.let { checkUrlReputationUseCase(it, safeDomains) }

        val merged = mergeRiskAnalyses(textAnalysis, urlReputation)
        repository.save(text = input, method = method, analysis = merged)
    }
}
