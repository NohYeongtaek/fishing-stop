package com.rocketdan24.fishingstop.feature.consent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocketdan24.fishingstop.feature.consent.domain.SetConsentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 최초 동의 화면의 ViewModel.
 * "동의합니다"를 누르면 동의 상태를 저장한 뒤, 저장이 끝나면 화면 전환 콜백을 호출한다.
 */
@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val setConsentUseCase: SetConsentUseCase,
    private val consentRepository: com.rocketdan24.fishingstop.feature.consent.domain.ConsentRepository
) : ViewModel() {

    init {
        // 화면이 뜬 순간 "노출됨"을 기록해, 둘러보기(미동의) 사용자가
        // 앱 재실행 시마다 동의 화면을 강제로 보지 않게 한다(최초 1회 노출).
        viewModelScope.launch { consentRepository.markSeen() }
    }

    /**
     * 동의 저장 → 완료 후 콜백 실행.
     * 저장이 끝난 뒤 이동해야 스플래시 재진입 시에도 동의 상태가 확실히 반영된다.
     */
    fun agree(onCompleted: () -> Unit) {
        viewModelScope.launch {
            setConsentUseCase(agreed = true)
            onCompleted()
        }
    }
}
