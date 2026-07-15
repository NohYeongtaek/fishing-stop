package com.example.fishingstop.features.inspect.domain.usecase

import com.example.fishingstop.features.risk_engine.domain.entities.RiskAnalysisResult
import com.example.fishingstop.features.risk_engine.domain.usecase.AnalyzeRiskUseCase
import javax.inject.Inject

/**
 * 검사 화면들의 공통 진입점.
 *
 * 메시지 검사(공유로 받은 텍스트), 이미지 검사(OCR로 뽑은 텍스트), URL 검사(붙여넣은 링크),
 * 직접검사 탭의 텍스트 붙여넣기·QR 촬영 결과까지 — 입력 경로가 무엇이든 최종적으로
 * "텍스트 + URL 목록" 형태로 정리된 뒤에는 반드시 이 UseCase 하나를 거쳐 risk_engine으로 위임한다.
 *
 * 각 화면의 ViewModel은 risk_engine 패키지(RiskAnalysisRepository, GeminiRiskDataSource 등)를
 * 절대 직접 import하지 않는다. risk_engine의 내부 구현(프롬프트, Gemini 연동)이 바뀌어도
 * 이 파일의 시그니처만 유지되면 검사 화면 쪽 코드는 전혀 영향받지 않는다.
 */
class InspectTextUseCase @Inject constructor(
    private val analyzeRiskUseCase: AnalyzeRiskUseCase,
) {
    suspend operator fun invoke(text: String, urls: List<String> = emptyList()): RiskAnalysisResult {
        return analyzeRiskUseCase(text = text, urls = urls)
    }
}
