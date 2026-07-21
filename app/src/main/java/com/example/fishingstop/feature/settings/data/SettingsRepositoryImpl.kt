package com.example.fishingstop.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fishingstop.core.util.ThemeMode
import com.example.fishingstop.feature.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** 설정을 DataStore(Preferences)에 저장하는 구현체. */
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        // 저장값이 없거나 잘못됐으면 시스템 설정을 따른다.
        runCatching { ThemeMode.valueOf(prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    override val elderMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ELDER] ?: false
    }

    override val notificationEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATION] ?: true
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME] = mode.name }
    }

    override suspend fun setElderMode(enabled: Boolean) {
        dataStore.edit { it[KEY_ELDER] = enabled }
    }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATION] = enabled }
    }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_ELDER = booleanPreferencesKey("elder_mode")
        private val KEY_NOTIFICATION = booleanPreferencesKey("notification_enabled")
    }
}
