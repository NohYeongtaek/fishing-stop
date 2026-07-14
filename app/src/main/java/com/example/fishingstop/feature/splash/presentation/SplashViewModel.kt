package com.example.fishingstop.feature.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.feature.consent.domain.ConsentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** 스플래시 분기에 필요한 동의 상태 묶음. */
data class ConsentStatus(val seen: Boolean, val agreed: Boolean)

/**
 * 스플래시 ViewModel.
 * 동의 화면 노출 여부(seen)와 동의 여부(agreed)를 읽어 분기 재료로 노출한다. null = 로딩 중.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    consentRepository: ConsentRepository
) : ViewModel() {

    val status: StateFlow<ConsentStatus?> =
        combine(consentRepository.hasSeen, consentRepository.hasAgreed) { seen, agreed ->
            ConsentStatus(seen = seen, agreed = agreed)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null // 로딩 상태
        )
}
