package com.rocketdan24.fishingstop.feature.report.di

import com.rocketdan24.fishingstop.feature.report.data.ReportRepositoryImpl
import com.rocketdan24.fishingstop.feature.report.domain.ReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 신고 기능의 의존성 바인딩. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ReportModule {

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository
}
