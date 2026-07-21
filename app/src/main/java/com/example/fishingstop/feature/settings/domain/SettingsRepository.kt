package com.example.fishingstop.feature.settings.domain

import com.example.fishingstop.core.util.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * 앱 설정 저장소.
 * 테마 모드와 어르신 모드(큰 글씨/고대비)를 DataStore에 저장한다.
 */
interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val elderMode: Flow<Boolean>
    val notificationEnabled: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setElderMode(enabled: Boolean)
    suspend fun setNotificationEnabled(enabled: Boolean)
}
