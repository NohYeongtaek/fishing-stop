package com.example.fishingstop.feature.notice.di

import com.example.fishingstop.feature.notice.data.AdminGateRepositoryImpl
import com.example.fishingstop.feature.notice.data.NoticeRepositoryImpl
import com.example.fishingstop.feature.notice.domain.AdminGateRepository
import com.example.fishingstop.feature.notice.domain.NoticeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 공지사항 기능의 의존성 바인딩. */
@Module
@InstallIn(SingletonComponent::class)
abstract class NoticeModule {

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(impl: NoticeRepositoryImpl): NoticeRepository

    @Binds
    @Singleton
    abstract fun bindAdminGateRepository(impl: AdminGateRepositoryImpl): AdminGateRepository
}
