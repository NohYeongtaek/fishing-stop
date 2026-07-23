package com.rocketdan24.fishingstop.feature.home.di

import com.rocketdan24.fishingstop.feature.home.data.CoachMarkRepositoryImpl
import com.rocketdan24.fishingstop.feature.home.domain.CoachMarkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 홈 탭 코치마크 기능의 의존성 바인딩. */
@Module
@InstallIn(SingletonComponent::class)
abstract class CoachMarkModule {

    @Binds
    @Singleton
    abstract fun bindCoachMarkRepository(impl: CoachMarkRepositoryImpl): CoachMarkRepository
}
