package com.example.fishingstop.feature.home.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.fishingstop.feature.home.domain.CoachMarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** 홈 탭 코치마크 열람 여부를 DataStore(Preferences)에 저장하는 구현체. */
class CoachMarkRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CoachMarkRepository {

    override val hasSeenHomeTabsCoachMark: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_SEEN] ?: false }

    override suspend fun markHomeTabsCoachMarkSeen() {
        dataStore.edit { prefs -> prefs[KEY_SEEN] = true }
    }

    companion object {
        private val KEY_SEEN = booleanPreferencesKey("home_tabs_coach_mark_seen")
    }
}
