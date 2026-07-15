package com.example.fishingstop.features.risk_engine.data.di

import com.example.fishingstop.features.risk_engine.data.repositories.RiskAnalysisRepositoryImpl
import com.example.fishingstop.features.risk_engine.domain.repositories.RiskAnalysisRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RiskModule {

    @Binds
    abstract fun bindRiskAnalysisRepository(
        impl: RiskAnalysisRepositoryImpl,
    ): RiskAnalysisRepository
}
