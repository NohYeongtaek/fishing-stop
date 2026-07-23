package com.rocketdan24.fishingstop.feature.inspect.data.dto

import kotlinx.serialization.Serializable

/**
 * Gemini가 반환하는 JSON을 파싱하기 위한 DTO.
 *
 * 계약(프롬프트/responseSchema에서 강제하는 JSON 형태):
 * { "riskScore": 0~100, "riskLevel": "SAFE|WARNING|DANGER",
 *   "signals": ["근거1", ...], "advice": "행동 권고" }
 *
 * ⚠️ riskScore 는 **기본값을 두지 않는다(필수)**. 안전 판정의 근거이므로, 응답에서 이 값이
 *    누락되면 조용히 0(=SAFE)으로 떨어뜨리지 않고 파싱을 실패시켜(fail-loud) 오류로 처리한다.
 *    "판별 실패를 안전으로 오인"하는 것이 이 앱에서 가장 위험하기 때문이다.
 *    나머지(riskLevel/signals/advice)는 표시용이라 누락돼도 기본값으로 견고하게 처리한다.
 *    (riskLevel 은 어차피 점수에서 재도출하므로 값 자체는 신뢰하지 않는다.)
 */
@Serializable
data class AnalysisResponseDto(
    val riskScore: Int,
    val riskLevel: String = "WARNING",
    val signals: List<String> = emptyList(),
    val advice: String = ""
)
