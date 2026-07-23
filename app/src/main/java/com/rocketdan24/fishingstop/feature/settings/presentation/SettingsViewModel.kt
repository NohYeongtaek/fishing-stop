package com.rocketdan24.fishingstop.feature.settings.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocketdan24.fishingstop.core.fcm.FishingStopFcmService
import com.rocketdan24.fishingstop.core.util.ThemeMode
import com.rocketdan24.fishingstop.core.utils.Constants.TAG
import com.rocketdan24.fishingstop.feature.settings.domain.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 설정 화면 ViewModel. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val elderMode: StateFlow<Boolean> = settingsRepository.elderMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val notificationEnabled: StateFlow<Boolean> = settingsRepository.notificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /** 앱 버전명(예: 1.0). PackageManager로 조회해 gradle 설정과 자동 일치. */
    val versionName: String =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "-"

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setElderMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setElderMode(enabled) }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationEnabled(enabled) }
        if (enabled) {
            FishingStopFcmService.subscribeNotice()
            Log.d(TAG, "setNotificationEnabled: 알림 설정")
        } else {
            FishingStopFcmService.unsubscribeNotice()
            Log.d(TAG, "setNotificationEnabled: 알림 해제")
        }
    }
}
