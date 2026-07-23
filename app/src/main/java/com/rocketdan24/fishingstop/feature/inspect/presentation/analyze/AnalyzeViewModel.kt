package com.rocketdan24.fishingstop.feature.inspect.presentation.analyze

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.rocketdan24.fishingstop.core.navigation.Routes
import com.rocketdan24.fishingstop.core.util.InspectMethod
import com.rocketdan24.fishingstop.core.util.toUserMessage
import com.rocketdan24.fishingstop.feature.inspect.domain.usecase.AnalyzeMessageUseCase
import com.rocketdan24.fishingstop.feature.inspect.domain.usecase.AnalyzeUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 분석 화면의 상태. */
sealed interface AnalyzeUiState {
    data object Loading : AnalyzeUiState
    /** 분석·저장 완료 → 결과 화면으로 이동할 검사 id */
    data class Success(val inspectionId: Long) : AnalyzeUiState
    data class Error(val message: String) : AnalyzeUiState
}

/**
 * 분석 화면 ViewModel.
 * 모든 검사(공유/텍스트/이미지/링크/QR)의 합류점으로, 네비게이션 인자의 텍스트를 즉시 분석한다.
 * (AI 외부 전송 동의는 앱 최초 실행 시 필수로 받으므로 여기서 별도 게이트를 두지 않는다.)
 */
@HiltViewModel
class AnalyzeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val analyzeMessageUseCase: AnalyzeMessageUseCase,
    private val analyzeUrlUseCase: AnalyzeUrlUseCase
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Routes.Analyze>()

    private val _uiState = MutableStateFlow<AnalyzeUiState>(AnalyzeUiState.Loading)
    val uiState: StateFlow<AnalyzeUiState> = _uiState.asStateFlow()

    init {
        analyze()
    }

    /** 오류 후 재시도에도 사용한다. */
    fun analyze() {
        val method = runCatching { InspectMethod.valueOf(args.method) }
            .getOrDefault(InspectMethod.TEXT)

        _uiState.value = AnalyzeUiState.Loading
        viewModelScope.launch {
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
