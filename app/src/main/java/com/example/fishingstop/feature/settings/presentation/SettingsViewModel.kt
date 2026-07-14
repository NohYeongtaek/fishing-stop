package com.example.fishingstop.feature.settings.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.core.util.ThemeMode
import com.example.fishingstop.feature.consent.domain.ConsentRepository
import com.example.fishingstop.feature.settings.domain.SettingsRepository
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
    private val settingsRepository: SettingsRepository,
    private val consentRepository: ConsentRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val elderMode: StateFlow<Boolean> = settingsRepository.elderMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** AI 분석(외부 전송) 동의 상태 — 설정에서 확인·철회할 수 있다. */
    val consentAgreed: StateFlow<Boolean> = consentRepository.hasAgreed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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

    /**
     * 동의 상태 변경.
     * 철회(false)하면 이후 검사 시 동의 게이트가 다시 뜬다(검사만 제한, 나머지 기능은 그대로).
     */
    fun setConsent(agreed: Boolean) {
        viewModelScope.launch { consentRepository.setAgreed(agreed) }
    }
}
