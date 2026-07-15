package com.example.fishingstop.feature.education.di

import com.example.fishingstop.feature.education.data.EducationRepositoryImpl
import com.example.fishingstop.feature.education.domain.EducationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 예방 교육 기능의 의존성 바인딩. */
@Module
@InstallIn(SingletonComponent::class)
abstract class EducationModule {

    @Binds
    @Singleton
    abstract fun bindEducationRepository(impl: EducationRepositoryImpl): EducationRepository
}
