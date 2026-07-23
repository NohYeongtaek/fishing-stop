package com.rocketdan24.fishingstop.feature.inspect.data

import com.rocketdan24.fishingstop.core.util.RiskLevel
import com.rocketdan24.fishingstop.feature.inspect.data.dto.AnalysisResponseDto
import com.rocketdan24.fishingstop.feature.inspect.data.mapper.toRiskAnalysis
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Gemini 응답(DTO) 파싱 + 도메인 매핑의 안전성 테스트.
 *
 * 핵심(fail-to-safe): riskScore 가 응답에서 누락되면 조용히 0(=SAFE)으로 떨어지지 않고
 * 파싱이 실패해야 한다. "판별 실패를 안전으로 오인"하는 것이 이 앱에서 가장 위험하기 때문.
 */
class AnalysisResponseParsingTest {

    // 앱(SerializationModule)과 동일한 관용 설정으로 대표성 확보.
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `정상 응답은 파싱되고 점수로 등급이 도출된다`() {
        val raw = """{"riskScore":85,"riskLevel":"DANGER","signals":["기관 사칭"],"advice":"링크 열지 마세요"}"""
        val dto = json.decodeFromString<AnalysisResponseDto>(raw)
        val analysis = dto.toRiskAnalysis()

        assertEquals(85, analysis.riskScore)
        assertEquals(RiskLevel.DANGER, analysis.riskLevel)
        assertEquals(listOf("기관 사칭"), analysis.signals)
    }

    @Test
    fun `riskScore가 빠진 응답은 SAFE로 떨어지지 않고 파싱에 실패한다`() {
        // riskScore 필드가 없는 응답(모델 이상/조작 등). 기본값 0으로 SAFE가 되면 안 된다.
        val raw = """{"riskLevel":"SAFE","signals":[],"advice":"안전합니다"}"""
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<AnalysisResponseDto>(raw)
        }
    }

    @Test
    fun `모델이 보낸 riskLevel 문자열은 무시하고 점수로 재도출한다`() {
        // 점수는 안전 구간(10)인데 riskLevel만 DANGER로 온 경우 → 점수 기준(SAFE)이 이긴다.
        val dto = AnalysisResponseDto(riskScore = 10, riskLevel = "DANGER")
        assertEquals(RiskLevel.SAFE, dto.toRiskAnalysis().riskLevel)
    }

    @Test
    fun `점수는 0에서 100으로 보정된다`() {
        assertEquals(RiskLevel.DANGER, AnalysisResponseDto(riskScore = 150).toRiskAnalysis().riskLevel)
        assertEquals(100, AnalysisResponseDto(riskScore = 150).toRiskAnalysis().riskScore)
        assertEquals(RiskLevel.SAFE, AnalysisResponseDto(riskScore = -5).toRiskAnalysis().riskLevel)
        assertEquals(0, AnalysisResponseDto(riskScore = -5).toRiskAnalysis().riskScore)
    }

    @Test
    fun `경계값 등급 매핑`() {
        assertEquals(RiskLevel.SAFE, AnalysisResponseDto(riskScore = 30).toRiskAnalysis().riskLevel)
        assertEquals(RiskLevel.WARNING, AnalysisResponseDto(riskScore = 31).toRiskAnalysis().riskLevel)
        assertEquals(RiskLevel.WARNING, AnalysisResponseDto(riskScore = 75).toRiskAnalysis().riskLevel)
        assertEquals(RiskLevel.DANGER, AnalysisResponseDto(riskScore = 76).toRiskAnalysis().riskLevel)
    }
}
