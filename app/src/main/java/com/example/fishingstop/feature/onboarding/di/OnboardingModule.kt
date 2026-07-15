package com.example.fishingstop.feature.onboarding.di

import com.example.fishingstop.feature.onboarding.data.OnboardingRepositoryImpl
import com.example.fishingstop.feature.onboarding.domain.OnboardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 온보딩 기능의 의존성 바인딩. */
@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingModule {

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository
}
