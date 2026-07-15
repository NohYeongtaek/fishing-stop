package com.example.fishingstop.feature.consent.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.fishingstop.feature.consent.domain.ConsentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 동의 상태를 DataStore(Preferences)에 저장하는 구현체.
 * 값이 없으면(최초 실행) 기본값 false → 동의 화면을 먼저 보여주게 된다.
 */
class ConsentRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ConsentRepository {

    override val hasAgreed: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_AGREED] ?: false }

    override val hasSeen: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_SEEN] ?: false }

    override suspend fun setAgreed(agreed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_AGREED] = agreed
            prefs[KEY_SEEN] = true // 동의/철회 어떤 경우든 화면은 본 것
        }
    }

    override suspend fun markSeen() {
        dataStore.edit { prefs -> prefs[KEY_SEEN] = true }
    }

    companion object {
        private val KEY_AGREED = booleanPreferencesKey("consent_agreed")
        private val KEY_SEEN = booleanPreferencesKey("consent_seen")
    }
}
