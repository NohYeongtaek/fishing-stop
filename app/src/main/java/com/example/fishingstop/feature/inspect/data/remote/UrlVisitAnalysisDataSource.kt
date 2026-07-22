package com.example.fishingstop.feature.inspect.data.remote

import android.util.Log
import com.example.fishingstop.core.di.UrlContextModel
import com.example.fishingstop.core.utils.Constants
import com.example.fishingstop.feature.inspect.data.dto.AnalysisResponseDto
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.UrlRetrievalStatus
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * URL 검사 2차 관문: Safe Browsing에 매치가 없을 때, Gemini의 URL Context 도구로 해당 URL의
 * 실제 페이지 내용을 "직접 방문"해 분석한다.
 *
 * Cloud Functions/헤드리스 브라우저 같은 별도 유료 백엔드 없이, 이미 쓰고 있는 Firebase AI Logic
 * (Gemini) 호출만으로 구현한다 — Google 서버가 URL을 대신 가져오므로 결제(Blaze) 없이 무료
 * 티어에서 동작한다. 사용자 기기가 페이지를 직접 열지 않으므로 악성 스크립트 노출도 없다.
 */
class UrlVisitAnalysisDataSource @Inject constructor(
    @UrlContextModel private val model: GenerativeModel,
    private val json: Json
) {
    suspend fun analyzeVisit(url: String): AnalysisResponseDto {
        val response = try {
            withTimeoutOrNull(TIMEOUT_MS) { model.generateContent(buildPrompt(url)) }
                ?: error("URL 방문 분석 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요.")
        } catch (e: Exception) {
            Log.w(Constants.TAG, "URL 방문 분석 호출 실패: ${e.javaClass.simpleName}", e)
            throw e
        }

        // Google이 URL Context 도구로 페이지를 가져오는 과정에서 자체적으로 "안전하지 않음"으로
        // 표시한 경우(UrlRetrievalStatus.UNSAFE)는 그 자체가 강한 위험 신호이므로 최소 DANGER로 끌어올린다.
        val unsafeByGoogle = response.candidates
            .firstOrNull()
            ?.urlContextMetadata
            ?.urlMetadata
            ?.any { it.urlRetrievalStatus == UrlRetrievalStatus.UNSAFE }
            ?: false

        val raw = response.text?.trim().orEmpty()
        check(raw.isNotEmpty()) { "AI 응답이 비어 있습니다." }

        val dto = try {
            json.decodeFromString<AnalysisResponseDto>(extractJsonObject(raw) ?: raw)
        } catch (e: Exception) {
            Log.w(Constants.TAG, "URL 방문 분석 응답 파싱 실패. raw=${raw.take(300)}", e)
            throw e
        }

        return if (unsafeByGoogle && dto.riskScore < MIN_SCORE_WHEN_GOOGLE_FLAGS_UNSAFE) {
            dto.copy(
                riskScore = MIN_SCORE_WHEN_GOOGLE_FLAGS_UNSAFE,
                riskLevel = "DANGER",
                signals = (listOf("Google이 이 페이지를 안전하지 않은 사이트로 표시했습니다.") + dto.signals).take(5)
            )
        } else {
            dto
        }
    }

    /**
     * 판정 규칙은 systemInstruction(UrlContextModule)에 고정돼 있다.
     * tools(urlContext) 사용 시 응답 형식 강제(responseSchema)가 항상 지켜진다는 보장이 없어,
     * 프롬프트로 JSON만 응답하도록 지시하고 파싱은 [extractJsonObject]로 관대하게 처리한다.
     */
    private fun buildPrompt(url: String): String = """
        아래 <url> 태그의 웹페이지를 URL Context 도구로 직접 확인하고 피싱 위험도를 분석하라.
        <url>$url</url>
    """.trimIndent()

    /** 모델이 JSON 앞뒤에 다른 텍스트를 덧붙여도 견고하게 파싱하도록 첫 '{'~마지막 '}' 구간만 취한다. */
    private fun extractJsonObject(text: String): String? {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start == -1 || end == -1 || end < start) return null
        return text.substring(start, end + 1)
    }

    companion object {
        private const val TIMEOUT_MS = 45_000L
        private const val MIN_SCORE_WHEN_GOOGLE_FLAGS_UNSAFE = 90
    }
}
