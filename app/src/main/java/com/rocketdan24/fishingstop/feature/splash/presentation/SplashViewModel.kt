package com.rocketdan24.fishingstop.feature.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocketdan24.fishingstop.feature.consent.domain.ConsentRepository
import com.rocketdan24.fishingstop.feature.onboarding.domain.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** 스플래시 분기에 필요한 최초 실행 상태. */
data class StartupStatus(val agreed: Boolean, val onboardingSeen: Boolean)

/**
 * 스플래시 ViewModel.
 * 동의 여부와 온보딩 열람 여부를 읽어 분기 재료로 노출한다. null = 로딩 중.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    consentRepository: ConsentRepository,
    onboardingRepository: OnboardingRepository
) : ViewModel() {

    val status: StateFlow<StartupStatus?> =
        combine(
            consentRepository.hasAgreed,
            onboardingRepository.hasSeenOnboarding
        ) { agreed, onboardingSeen ->
            StartupStatus(agreed = agreed, onboardingSeen = onboardingSeen)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null // 로딩 상태
        )
}
