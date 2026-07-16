package com.example.fishingstop.feature.inspect.di

import com.example.fishingstop.feature.inspect.data.InspectionRepositoryImpl
import com.example.fishingstop.feature.inspect.data.OcrRepositoryImpl
import com.example.fishingstop.feature.inspect.data.WhitelistRepositoryImpl
import com.example.fishingstop.feature.inspect.domain.repository.InspectionRepository
import com.example.fishingstop.feature.inspect.domain.repository.OcrRepository
import com.example.fishingstop.feature.inspect.domain.repository.WhitelistRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 검사 기능의 의존성 구성.
 *
 * - @Binds 로 InspectionRepository ← 구현체 연결
 * - @Provides 로 Gemini GenerativeModel 을 구조화 출력(JSON) 설정과 함께 제공
 *
 * @Binds(추상)와 @Provides(구체)는 한 모듈에 섞기 어려우므로 provides 는 companion object 로 분리한다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class InspectModule {

    @Binds
    @Singleton
    abstract fun bindInspectionRepository(impl: InspectionRepositoryImpl): InspectionRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(impl: OcrRepositoryImpl): OcrRepository

    @Binds
    @Singleton
    abstract fun bindWhitelistRepository(impl: WhitelistRepositoryImpl): WhitelistRepository

    companion object {

        /**
         * 한국어 텍스트 인식기.
         * 한글 인식기는 한글뿐 아니라 영문·숫자도 함께 인식하므로 스미싱 문자에 적합하다.
         */
        @Provides
        @Singleton
        fun provideTextRecognizer(): TextRecognizer =
            TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

        @Provides
        @Singleton
        fun provideGenerativeModel(): GenerativeModel {
            // 응답을 항상 { riskScore, riskLevel, signals[], advice } JSON으로 받도록 스키마를 강제
            val schema = Schema.obj(
                mapOf(
                    "riskScore" to Schema.integer(),
                    "riskLevel" to Schema.enumeration(listOf("SAFE", "WARNING", "DANGER")),
                    "signals" to Schema.array(Schema.string()),
                    "advice" to Schema.string()
                )
            )
            return Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(
                    // 모델 선정 이력(2026-07): 2.0/2.5 계열은 셧다운·신규 차단,
                    // gemini-flash-latest 별칭은 응답 지연(hang) 이슈가 있어
                    // 고정 버전 lite 모델을 사용한다 — 분류 작업에 충분하고 빠르다.
                    modelName = "gemini-3.1-flash-lite",
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                        responseSchema = schema
                    }
                )
        }
    }
}
