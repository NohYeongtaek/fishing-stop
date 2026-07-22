package com.example.fishingstop.feature.inspect.data

import android.util.Log
import com.example.fishingstop.BuildConfig
import com.example.fishingstop.core.util.IoDispatcher
import com.example.fishingstop.core.utils.Constants
import com.example.fishingstop.feature.inspect.data.remote.safebrowsing.SafeBrowsingApi
import com.example.fishingstop.feature.inspect.data.remote.safebrowsing.dto.ThreatMatchesRequestDto
import com.example.fishingstop.feature.inspect.domain.model.SafeBrowsingVerdict
import com.example.fishingstop.feature.inspect.domain.repository.SafeBrowsingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * URL 검사 1차 관문: Google Safe Browsing v4로 알려진 악성/피싱 URL인지 즉시 대조한다.
 *
 * - API 키가 비어 있으면(로컬 미설정) 네트워크를 타지 않고 바로 [SafeBrowsingVerdict.Unknown]을
 *   반환한다 — 호출측(AnalyzeUrlUseCase)이 곧바로 2차(AI 방문 분석)로 넘어간다.
 * - 이 조회 자체는 URL 전체가 아니라 URL 문자열을 Google에 대조 질의로 전송한다는 점에 유의
 *   (Safe Browsing Lookup API의 정상 사용 방식이며 방문/렌더링은 하지 않는다).
 */
class SafeBrowsingRepositoryImpl @Inject constructor(
    private val api: SafeBrowsingApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SafeBrowsingRepository {

    override suspend fun checkUrl(url: String): SafeBrowsingVerdict = withContext(ioDispatcher) {
        val apiKey = BuildConfig.SAFE_BROWSING_API_KEY
        if (apiKey.isBlank()) return@withContext SafeBrowsingVerdict.Unknown

        val normalized = if (url.contains("://")) url else "http://$url"
        val request = ThreatMatchesRequestDto.forUrl(
            url = normalized,
            clientId = Constants.SAFE_BROWSING_CLIENT_ID,
            clientVersion = BuildConfig.VERSION_NAME
        )

        val response = runCatching {
            withTimeoutOrNull(TIMEOUT_MS) { api.findThreatMatches(apiKey, request) }
        }.onFailure { e ->
            Log.w(Constants.TAG, "Safe Browsing 조회 실패: ${e.javaClass.simpleName}", e)
        }.getOrNull() ?: return@withContext SafeBrowsingVerdict.Unknown

        val matches = response.matches
        if (matches.isNullOrEmpty()) {
            SafeBrowsingVerdict.Clean
        } else {
            SafeBrowsingVerdict.Threat(matches.mapNotNull { it.threatType }.distinct())
        }
    }

    companion object {
        private const val TIMEOUT_MS = 8_000L
    }
}
