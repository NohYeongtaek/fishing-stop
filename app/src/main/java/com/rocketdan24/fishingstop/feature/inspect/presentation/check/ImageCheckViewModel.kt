package com.rocketdan24.fishingstop.feature.inspect.presentation.check

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocketdan24.fishingstop.core.util.toUserMessage
import com.rocketdan24.fishingstop.feature.inspect.domain.usecase.ExtractTextFromImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 이미지 검사 화면 상태. */
sealed interface ImageCheckUiState {
    /** 이미지 선택 대기 */
    data object Idle : ImageCheckUiState

    /** OCR 진행 중 */
    data object Processing : ImageCheckUiState

    /**
     * OCR 완료 → 추출 텍스트 미리보기(기획 확정: 사용자가 확인·수정 후 검사).
     * OCR 오인식이 그대로 분석되는 것을 막는 단계다.
     */
    data class Preview(val text: String) : ImageCheckUiState

    /** 오류(1회성 — 소비 후 Idle 복귀) */
    data class Error(val message: String) : ImageCheckUiState
}

/**
 * 이미지 검사 ViewModel(FO_02_03).
 * 포토피커로 받은 이미지를 온디바이스 OCR로 텍스트화하고, 미리보기 상태로 노출한다.
 */
@HiltViewModel
class ImageCheckViewModel @Inject constructor(
    private val extractTextFromImageUseCase: ExtractTextFromImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImageCheckUiState>(ImageCheckUiState.Idle)
    val uiState: StateFlow<ImageCheckUiState> = _uiState.asStateFlow()

    /** 이미지 선택 완료 → OCR 실행. null 이면 사용자가 선택을 취소한 것. */
    fun onImagePicked(uri: Uri?) {
        if (uri == null) return
        _uiState.value = ImageCheckUiState.Processing
        viewModelScope.launch {
            extractTextFromImageUseCase(uri)
                .onSuccess { text -> _uiState.value = ImageCheckUiState.Preview(text) }
                .onFailure { e -> _uiState.value = ImageCheckUiState.Error(e.toUserMessage()) }
        }
    }

    /** 미리보기에서 사용자가 텍스트를 수정한 경우 반영. */
    fun updatePreviewText(text: String) {
        _uiState.value = ImageCheckUiState.Preview(text)
    }

    /** 오류 소비 후 초기 상태로. */
    fun consumeError() {
        _uiState.value = ImageCheckUiState.Idle
    }
}
