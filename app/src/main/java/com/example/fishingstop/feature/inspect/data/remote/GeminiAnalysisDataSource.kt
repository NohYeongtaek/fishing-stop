package com.example.fishingstop.feature.inspect.data.remote

import android.util.Log
import com.example.fishingstop.core.utils.Constants
import com.example.fishingstop.feature.inspect.data.dto.AnalysisResponseDto
import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Gemini(Firebase AI)로 텍스트의 피싱 위험도를 분석하는 원격 데이터소스.
 *
 * 위험도 판정은 이 앱의 핵심이자 최고 난이도 영역이므로, 프롬프트에 판정 기준을 명시하고
 * responseSchema(구조화 출력)로 JSON 형태를 강제해 파싱 실패를 최소화한다.
 * (프롬프트/기준 변경은 반드시 코드 리뷰를 거친다 — 협업 규칙.)
 */
class GeminiAnalysisDataSource @Inject constructor(
    private val model: GenerativeModel,
    private val json: Json
) {
    /**
     * @param text 분석할 원문 텍스트(공유/OCR/붙여넣기 등)
     * @return 파싱된 분석 결과 DTO
     * @throws IllegalStateException 응답이 비어 있을 때
     */
    suspend fun analyze(text: String): AnalysisResponseDto {
        val response = try {
            // SDK 기본 타임아웃(3분)은 너무 길다 — 45초 안에 응답이 없으면 실패 처리해
            // 사용자가 무한 로딩에 갇히지 않게 한다.
            withTimeoutOrNull(TIMEOUT_MS) {
                model.generateContent(buildPrompt(text))
            } ?: error("분석 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요.")
        } catch (e: Exception) {
            // 원인 진단을 위해 SDK 예외를 로그로 남긴다(화면에는 가공된 문구만 노출).
            Log.w(Constants.TAG, "Gemini 호출 실패: ${e.javaClass.simpleName}", e)
            throw e
        }
        val raw = response.text?.trim().orEmpty()
        check(raw.isNotEmpty()) { "AI 응답이 비어 있습니다." }
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            // 파싱 실패 시 원문 일부를 로그로 남겨 스키마/응답 문제를 추적한다.
            Log.w(Constants.TAG, "Gemini 응답 파싱 실패. raw=${raw.take(300)}", e)
            throw e
        }
    }

    /**
     * 위험도 판정 프롬프트.
     *
     * 스펙의 3단계 기준(안전 0~30 / 주의 31~75 / 위험 76~100)과 "단정 금지 원칙"을 반영한다.
     * - 확정적 표현("100% 사기") 대신 근거 기반의 신중한 표현을 쓰도록 지시
     * - 개인정보를 결과에 그대로 되풀이하지 않도록 지시
     */
    private fun buildPrompt(text: String): String = """
        너는 한국의 보이스피싱·스미싱 문자를 판별하는 보안 분석 도우미다.
        아래 [메시지]를 분석해 피싱/스미싱 위험도를 평가하라.

        판정 기준:
        - 안전(riskScore 0~30, riskLevel "SAFE"): 피싱 징후 없는 일상 대화, 인증번호, 단순 택배 안내 등.
        - 주의(riskScore 31~75, riskLevel "WARNING"): 출처 불명 URL, 금융/수사기관 언급, 유도성 문구,
          또는 판단 근거가 부족한 경우.
        - 위험(riskScore 76~100, riskLevel "DANGER"): 신분증/계좌번호 요구, 대출 권유, 기관 사칭,
          "즉시"·"오늘 마감" 등 압박 문구, 악성 앱(APK) 설치 유도 등.

        지침:
        - 확정적 단정("반드시 사기다")을 피하고, 근거에 기반해 신중하게 서술하라.
        - signals에는 위험/안전이라고 본 근거를 한국어 짧은 문장으로 3개 이내로 담아라.
        - advice에는 사용자가 취해야 할 행동 권고를 1~2문장으로 담아라(예: 링크를 열지 말 것).
        - 응답에 사용자의 개인정보(계좌·주민번호 등)를 그대로 반복하지 마라.
        - riskLevel은 riskScore 구간과 일치시켜라.

        [메시지]
        $text
    """.trimIndent()

    companion object {
        private const val TIMEOUT_MS = 45_000L
    }
}
