package com.example.fishingstop.feature.notice.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.feature.notice.domain.NoticeRepository
import com.example.fishingstop.feature.notice.domain.model.Notice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 공지 목록 화면 상태. */
sealed interface NoticeListUiState {
    data object Loading : NoticeListUiState
    data class Success(val notices: List<Notice>, val isRefreshing: Boolean) : NoticeListUiState
    data object Empty : NoticeListUiState
    data class Error(val message: String) : NoticeListUiState
}

@HiltViewModel
class NoticeListViewModel @Inject constructor(
    private val repository: NoticeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoticeListUiState>(NoticeListUiState.Loading)
    val uiState: StateFlow<NoticeListUiState> = _uiState.asStateFlow()

    init {
        load(initial = true)
    }

    /** 당겨서 새로고침. */
    fun refresh() = load(initial = false)

    private fun load(initial: Boolean) {
        // 새로고침 중이면 기존 목록을 유지한 채 스피너만 표시
        val current = _uiState.value
        if (!initial && current is NoticeListUiState.Success) {
            _uiState.value = current.copy(isRefreshing = true)
        } else if (initial) {
            _uiState.value = NoticeListUiState.Loading
        }

        viewModelScope.launch {
            runCatching { repository.getNotices() }
                .onSuccess { list ->
                    _uiState.value = if (list.isEmpty()) NoticeListUiState.Empty
                    else NoticeListUiState.Success(list, isRefreshing = false)
                }
                .onFailure { e ->
                    // 새로고침 실패 시 기존 목록을 버리지 않고 유지(스피너만 해제)
                    _uiState.value = if (current is NoticeListUiState.Success) {
                        current.copy(isRefreshing = false)
                    } else {
                        NoticeListUiState.Error(e.message ?: "공지사항을 불러오지 못했어요.")
                    }
                }
        }
    }
}
