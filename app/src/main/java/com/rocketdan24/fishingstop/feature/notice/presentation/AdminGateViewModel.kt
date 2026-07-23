package com.rocketdan24.fishingstop.feature.notice.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocketdan24.fishingstop.feature.notice.domain.AdminGateRepository
import com.rocketdan24.fishingstop.feature.notice.domain.PinResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 관리자 PIN 게이트 다이얼로그 상태. */
sealed interface AdminGateUiState {
    data object Idle : AdminGateUiState
    data object Verifying : AdminGateUiState
    /** @param remaining 잠금까지 남은 시도 횟수 */
    data class Failed(val remaining: Int) : AdminGateUiState
    /** @param remainingMinutes 잠금 해제까지 남은 분 */
    data class Locked(val remainingMinutes: Long) : AdminGateUiState
    data class Error(val message: String) : AdminGateUiState
    /** 통과 — 화면은 이 상태를 감지해 작성 화면으로 이동한다. */
    data object Success : AdminGateUiState
}

@HiltViewModel
class AdminGateViewModel @Inject constructor(
    private val repository: AdminGateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminGateUiState>(AdminGateUiState.Idle)
    val uiState: StateFlow<AdminGateUiState> = _uiState.asStateFlow()

    fun verify(pin: String) {
        if (pin.length != PIN_LENGTH || _uiState.value is AdminGateUiState.Verifying) return
        _uiState.value = AdminGateUiState.Verifying
        viewModelScope.launch {
            _uiState.value = when (val r = repository.verifyPin(pin)) {
                is PinResult.Success -> AdminGateUiState.Success
                is PinResult.Failed -> AdminGateUiState.Failed(r.remaining)
                is PinResult.Locked -> AdminGateUiState.Locked(millisToMinutesCeil(r.remainingMillis))
                is PinResult.Error -> AdminGateUiState.Error(r.message)
            }
        }
    }

    /** 다이얼로그를 닫거나 다시 열 때 상태 초기화. */
    fun reset() { _uiState.value = AdminGateUiState.Idle }

    private fun millisToMinutesCeil(ms: Long): Long = (ms + 59_999L) / 60_000L

    companion object {
        const val PIN_LENGTH = 4
    }
}
