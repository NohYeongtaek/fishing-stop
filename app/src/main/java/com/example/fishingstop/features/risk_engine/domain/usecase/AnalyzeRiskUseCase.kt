package com.example.fishingstop.features.risk_engine.domain.usecase

import com.example.fishingstop.features.risk_engine.domain.entities.RiskAnalysisResult
import com.example.fishingstop.features.risk_engine.domain.entities.RiskLevel
import com.example.fishingstop.features.risk_engine.domain.repositories.RiskAnalysisRepository
import javax.inject.Inject

/**
 * 위험도 판정 UseCase.
 *
 * 메시지 검사, 이미지(OCR) 검사, URL 검사 등 모든 검사 화면이 공통으로 주입받아 사용하는 진입점이다.
 * AI(Gemini) 응답을 그대로 신뢰하지 않고, 여기서 점수 구간(0~30 안전 / 30~75 경고 / 75~100 위험)을
 * 다시 한 번 강제로 맞춘다. 프롬프트가 바뀌어도 등급 기준선은 항상 이 파일 하나로 고정된다.
 *
 * 위험도 로직 리스크 분산 원칙에 따라 이 파일을 수정할 때는 반드시 페어 설계 또는 코드 리뷰를 거친다.
 */
class AnalyzeRiskUseCase @Inject constructor(
    private val repository: RiskAnalysisRepository,
) {
    suspend operator fun invoke(text: String, urls: List<String> = emptyList()): RiskAnalysisResult {
        require(text.isNotBlank()) { "분석할 텍스트가 비어 있습니다." }

        val result = repository.analyze(text = text, urls = urls)

        // AI가 반환한 level과 score가 어긋나더라도, 최종 등급은 항상 score 기준으로 재계산해 신뢰도를 보장한다.
        val correctedLevel = RiskLevel.fromScore(result.score)

        return if (correctedLevel == result.level) result else result.copy(level = correctedLevel)
    }
}
