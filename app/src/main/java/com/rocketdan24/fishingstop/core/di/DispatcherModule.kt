package com.rocketdan24.fishingstop.core.di

import com.rocketdan24.fishingstop.core.util.DefaultDispatcher
import com.rocketdan24.fishingstop.core.util.IoDispatcher
import com.rocketdan24.fishingstop.core.util.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * 코루틴 디스패처를 한정자와 함께 제공한다.
 * (테스트에서 이 모듈을 교체하면 모든 디스패처를 TestDispatcher로 바꿀 수 있다.)
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
