package com.rocketdan24.fishingstop.feature.inspect.domain.repository

import com.rocketdan24.fishingstop.feature.inspect.domain.model.RiskAnalysis

/**
 * URL 검사 2차 관문: Safe Browsing에 매치가 없을 때, Gemini의 URL Context 도구로 해당 URL의
 * 실제 웹페이지 내용을 직접 가져와 근거로 삼아 판정한 결과를 가져온다(Google 서버가 대신
 * 방문하므로 별도 유료 백엔드 없이 무료로 동작하고, 사용자 기기가 악성 페이지에 노출되지 않는다).
 *
 * 신종(블랙리스트 미등재) 피싱 페이지의 가짜 로그인 폼, 브랜드 사칭 문구 등을 잡아내기 위한 보조 신호.
 */
interface UrlVisitAnalysisRepository {
    /**
     * @throws Exception 방문 실패(사이트 차단/타임아웃/네트워크 오류 등). 호출측이 보조 신호로만
     *   취급하므로 실패해도 검사 자체는 계속 진행되어야 한다(runCatching으로 감싸서 호출).
     */
    suspend fun analyzeVisit(url: String): RiskAnalysis
}
