package com.example.fishingstop.feature.onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.fishingstop.feature.onboarding.domain.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** 온보딩 열람 여부를 DataStore(Preferences)에 저장하는 구현체. */
class OnboardingRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : OnboardingRepository {

    override val hasSeenOnboarding: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_SEEN] ?: false }

    override suspend fun markOnboardingSeen() {
        dataStore.edit { prefs -> prefs[KEY_SEEN] = true }
    }

    companion object {
        private val KEY_SEEN = booleanPreferencesKey("onboarding_seen")
    }
}
