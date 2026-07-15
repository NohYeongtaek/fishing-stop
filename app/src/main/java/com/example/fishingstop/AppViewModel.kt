package com.example.fishingstop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.core.util.ThemeMode
import com.example.fishingstop.feature.settings.domain.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 앱 최상위 ViewModel.
 * 테마/어르신 모드 설정을 구독해, 앱 전체에 즉시 반영되도록 한다.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val elderMode: StateFlow<Boolean> = settingsRepository.elderMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
