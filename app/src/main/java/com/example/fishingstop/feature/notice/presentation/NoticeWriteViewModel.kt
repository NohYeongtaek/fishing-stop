package com.example.fishingstop.feature.notice.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.feature.notice.domain.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 공지 작성 상태. */
sealed interface NoticeWriteUiState {
    data object Idle : NoticeWriteUiState
    data object Submitting : NoticeWriteUiState
    data object Done : NoticeWriteUiState
    data class Error(val message: String) : NoticeWriteUiState
}

@HiltViewModel
class NoticeWriteViewModel @Inject constructor(
    private val repository: NoticeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoticeWriteUiState>(NoticeWriteUiState.Idle)
    val uiState: StateFlow<NoticeWriteUiState> = _uiState.asStateFlow()

    fun submit(title: String, body: String) {
        if (_uiState.value is NoticeWriteUiState.Submitting) return
        _uiState.value = NoticeWriteUiState.Submitting
        viewModelScope.launch {
            runCatching { repository.addNotice(title.trim(), body.trim()) }
                .onSuccess { _uiState.value = NoticeWriteUiState.Done }
                .onFailure { _uiState.value = NoticeWriteUiState.Error(it.message ?: "등록에 실패했어요.") }
        }
    }

    fun consumeError() {
        if (_uiState.value is NoticeWriteUiState.Error) _uiState.value = NoticeWriteUiState.Idle
    }
}
