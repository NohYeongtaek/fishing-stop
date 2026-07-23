package com.rocketdan24.fishingstop.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * 앱 공용 kotlinx-serialization Json 인스턴스.
 * - ignoreUnknownKeys: 모델이 예상 밖 필드를 추가해도 무시(견고성)
 * - isLenient: 약간 어긋난 JSON도 최대한 파싱
 */
@Module
@InstallIn(SingletonComponent::class)
object SerializationModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}
