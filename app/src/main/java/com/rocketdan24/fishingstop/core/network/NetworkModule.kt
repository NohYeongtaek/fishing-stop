package com.rocketdan24.fishingstop.core.network

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 공통 네트워크 클라이언트(OkHttp + Retrofit)를 제공한다.
 *
 * - 모든 통신은 HTTPS 기반(비기능 요구사항)이며 BASE_URL도 https 로 둔다.
 * - 현재 BASE_URL은 자리표시자다. URL 검사/신고용 백엔드(예: Firebase Functions HTTPS 엔드포인트)가
 *   확정되면 교체한다.
 * - 컨버터는 우선 Gson으로 두었다. 프로젝트를 kotlinx-serialization로 통일하기로 하면
 *   converter-gson 의존성과 함께 kotlinx 컨버터로 교체한다(현재 두 컨버터가 중복 추가되어 있어 정리 필요).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // TODO: 실제 백엔드 엔드포인트로 교체
    private const val BASE_URL = "https://example.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
}
