package com.example.fishingstop.feature.inspect.data.remote

import android.util.Log
import com.example.fishingstop.core.util.IoDispatcher
import com.example.fishingstop.core.utils.Constants
import com.example.fishingstop.feature.inspect.domain.repository.UrlRedirectResolver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import javax.inject.Inject

/**
 * 단축 URL 등 리다이렉트가 걸린 링크의 실제 목적지 호스트를 알아낸다.
 *
 * - HEAD 요청으로 응답 본문 없이 헤더(Location)만 확인한다 — 페이지를 렌더링/다운로드하지
 *   않으므로 악성 스크립트 실행이나 자동 다운로드 위험이 없다.
 * - 리다이렉트를 최대 [MAX_REDIRECTS]회로 제한하고 http/https 스킴만 허용해
 *   무한 루프나 이상한 스킴으로의 유도를 막는다.
 * - 실패하면 null을 반환한다 — 호출측(AnalyzeUrlUseCase)이 원래 휴리스틱 결과로 계속 진행한다.
 */
class UrlRedirectResolverImpl @Inject constructor(
    client: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UrlRedirectResolver {

    // 리다이렉트를 한 홉씩 직접 추적해야 하므로 OkHttp의 자동 추적은 끈다.
    private val noRedirectClient = client.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    override suspend fun resolveFinalHost(url: String): String? =
        withContext(ioDispatcher) {
            withTimeoutOrNull(TIMEOUT_MS) {
                runCatching { resolve(url) }.getOrNull()
            }
        }

    private fun resolve(startUrl: String): String? {
        var current = if (startUrl.contains("://")) startUrl else "http://$startUrl"
        var hops = 0

        while (hops < MAX_REDIRECTS) {
            val uri = runCatching { URI(current) }.getOrNull() ?: return null
            if (uri.scheme?.lowercase() !in ALLOWED_SCHEMES) return null

            val request = Request.Builder().url(current).head().build()
            val response = runCatching { noRedirectClient.newCall(request).execute() }.getOrNull()
                ?: return uri.host

            response.use {
                if (!it.isRedirect) return uri.host
                val location = it.header("Location") ?: return uri.host
                current = runCatching { URI(current).resolve(location).toString() }.getOrNull()
                    ?: return uri.host
            }
            hops++
        }

        return runCatching { URI(current).host }.getOrNull().also {
            Log.d(Constants.TAG, "리다이렉트 추적이 최대 횟수($MAX_REDIRECTS)에 도달했습니다.")
        }
    }

    companion object {
        private const val MAX_REDIRECTS = 5
        private const val TIMEOUT_MS = 8_000L
        private val ALLOWED_SCHEMES = setOf("http", "https")
    }
}
