package com.example.fishingstop.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.feature.onboarding.domain.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 온보딩 화면 ViewModel. 완료 시 열람 여부를 저장한다. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    /** 이미 본 적 있으면(재진입 방어) 화면을 즉시 건너뛰기 위한 상태. null=확인 중. */
    val alreadySeen: StateFlow<Boolean?> = onboardingRepository.hasSeenOnboarding
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** "시작하기" → 열람 기록 후 다음 화면으로 이동. */
    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            onboardingRepository.markOnboardingSeen()
            onDone()
        }
    }
}
