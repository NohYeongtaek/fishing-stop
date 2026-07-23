package com.rocketdan24.fishingstop.feature.consent.di

import com.rocketdan24.fishingstop.feature.consent.data.ConsentRepositoryImpl
import com.rocketdan24.fishingstop.feature.consent.domain.ConsentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 동의 기능의 의존성 바인딩.
 * 도메인 인터페이스(ConsentRepository) ← 구현체(ConsentRepositoryImpl) 를 연결한다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ConsentModule {

    @Binds
    @Singleton
    abstract fun bindConsentRepository(impl: ConsentRepositoryImpl): ConsentRepository
}
