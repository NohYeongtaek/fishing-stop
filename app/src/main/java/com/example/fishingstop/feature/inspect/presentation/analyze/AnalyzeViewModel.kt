package com.example.fishingstop.feature.inspect.presentation.analyze

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fishingstop.core.navigation.Routes
import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.core.util.toUserMessage
import com.example.fishingstop.feature.consent.domain.ObserveConsentUseCase
import com.example.fishingstop.feature.inspect.domain.AnalyzeMessageUseCase
import com.example.fishingstop.feature.inspect.domain.AnalyzeUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 분석 화면의 상태. */
sealed interface AnalyzeUiState {
    data object Loading : AnalyzeUiState

    /** 미동의 상태: 검사 기능은 동의가 필요하다(동의 게이트). */
    data object NeedConsent : AnalyzeUiState

    /** 분석·저장 완료 → 결과 화면으로 이동할 검사 id */
    data class Success(val inspectionId: Long) : AnalyzeUiState
    data class Error(val message: String) : AnalyzeUiState
}

/**
 * 분석 화면 ViewModel.
 *
 * 모든 검사(공유/텍스트/이미지/링크/QR)의 합류점이므로, "AI 외부 전송 동의" 게이트도
 * 여기서 일괄 처리한다. 미동의면 분석을 시작하지 않고 [AnalyzeUiState.NeedConsent]를 노출하고,
 * 동의가 완료되면(동의 화면에서 복귀) 자동으로 분석을 시작한다.
 */
@HiltViewModel
class AnalyzeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val analyzeMessageUseCase: AnalyzeMessageUseCase,
    private val analyzeUrlUseCase: AnalyzeUrlUseCase,
    private val observeConsentUseCase: ObserveConsentUseCase
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Routes.Analyze>()

    private val _uiState = MutableStateFlow<AnalyzeUiState>(AnalyzeUiState.Loading)
    val uiState: StateFlow<AnalyzeUiState> = _uiState.asStateFlow()

    init {
        // 동의 상태를 구독: 최초 미동의면 게이트 노출, 이후 동의로 바뀌면 자동으로 분석 시작.
        viewModelScope.launch {
            observeConsentUseCase().collect { agreed ->
                when {
                    !agreed -> _uiState.value = AnalyzeUiState.NeedConsent
                    _uiState.value is AnalyzeUiState.NeedConsent ||
                        _uiState.value is AnalyzeUiState.Loading -> analyze()
                    else -> Unit // 이미 분석 중/완료/오류 상태면 그대로 둔다.
                }
            }
        }
    }

    /** 오류 후 재시도에도 사용한다. */
    fun analyze() {
        viewModelScope.launch {
            // 동의 없이는 절대 외부(AI)로 전송하지 않는다.
            if (!observeConsentUseCase().first()) {
                _uiState.value = AnalyzeUiState.NeedConsent
                return@launch
            }

            val method = runCatching { InspectMethod.valueOf(args.method) }
                .getOrDefault(InspectMethod.TEXT)

            _uiState.value = AnalyzeUiState.Loading
            // 링크/QR 검사는 휴리스틱+AI 병합, 그 외(공유/텍스트/이미지)는 Gemini 분석을 탄다.
            val result = if (method == InspectMethod.LINK || method == InspectMethod.QR) {
                analyzeUrlUseCase(text = args.text, method = method)
            } else {
                analyzeMessageUseCase(text = args.text, method = method)
            }
            result
                .onSuccess { id -> _uiState.value = AnalyzeUiState.Success(id) }
                .onFailure { e -> _uiState.value = AnalyzeUiState.Error(e.toUserMessage()) }
        }
    }
}
