package com.example.fishingstop.feature.settings.di

import com.example.fishingstop.feature.settings.data.SettingsRepositoryImpl
import com.example.fishingstop.feature.settings.domain.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 설정 기능의 의존성 바인딩. */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
