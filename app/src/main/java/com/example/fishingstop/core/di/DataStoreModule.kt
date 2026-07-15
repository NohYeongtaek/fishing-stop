package com.example.fishingstop.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 앱 전역 설정 저장소(DataStore Preferences).
 * 최초 동의 여부, 다크/라이트 모드, 어르신 모드 등 가벼운 설정값을 저장한다.
 */

// 프로세스당 하나만 존재해야 하는 DataStore 인스턴스를 Context 확장으로 선언한다.
private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "fishingstop_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.appDataStore
}
