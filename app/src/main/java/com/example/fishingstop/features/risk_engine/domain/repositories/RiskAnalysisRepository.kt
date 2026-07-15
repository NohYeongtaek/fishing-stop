package com.example.fishingstop.features.risk_engine.domain.repositories

import com.example.fishingstop.features.risk_engine.domain.entities.RiskAnalysisResult

/**
 * 위험도 판정 저장소 인터페이스.
 * data 계층(Gemini 연동)을 domain이 몰라도 되도록 감싼다.
 * 호출하는 쪽(메시지 검사/이미지 검사/URL 검사 등)은 이 인터페이스를 직접 쓰지 않고
 * AnalyzeRiskUseCase를 통해서만 접근한다.
 */
interface RiskAnalysisRepository {
    /**
     * @param text 분석할 원문 텍스트 (메시지 원문 또는 OCR 추출 결과)
     * @param urls 텍스트에서 추출된 URL 목록 (URL 휴리스틱 검사 결과를 함께 참고 자료로 넘긴다)
     */
    suspend fun analyze(text: String, urls: List<String>): RiskAnalysisResult
}
