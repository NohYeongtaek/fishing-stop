package com.example.fishingstop.core.di

import com.newBie.new_bie.features.profile.data.repositories.GuestbooksRepositoryImpl
import com.newBie.new_bie.features.profile.domain.repositories.GuestbooksRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("UNUSED_PARAMETER")
abstract class GuestbooksModule {
    @Binds
    @Singleton
    @Suppress("UNUSED_PARAMETER")
    abstract fun bindGuestbooksRepository(
        guestbooksRepositoryImpl: GuestbooksRepositoryImpl
    ): GuestbooksRepository
}