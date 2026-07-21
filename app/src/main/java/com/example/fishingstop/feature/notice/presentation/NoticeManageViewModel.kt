package com.example.fishingstop.feature.notice.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.core.utils.Constants
import com.example.fishingstop.feature.notice.domain.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 공지 관리(관리자) ViewModel.
 * 목록 로드/새로고침은 [NoticeListUiState]를 재사용하고, 여기에 삭제 기능을 더한다.
 * 삭제 성공 시 목록을 자동으로 다시 불러와 화면이 즉시 갱신된다.
 */
@HiltViewModel
class NoticeManageViewModel @Inject constructor(
    private val repository: NoticeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoticeListUiState>(NoticeListUiState.Loading)
    val uiState: StateFlow<NoticeListUiState> = _uiState.asStateFlow()

    init {
        load(initial = true)
    }

    /** 당겨서 새로고침 / 수정 복귀 후 재조회. */
    fun refresh() = load(initial = false)

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { repository.deleteNotice(id) }
                .onSuccess { load(initial = false) } // 삭제 후 자동 새로고침
                .onFailure { e ->
                    Log.w(Constants.TAG, "공지 삭제 실패", e)
                    // 삭제 실패 시 현재 목록은 유지하고 오류 상태로 알린다.
                    _uiState.value = NoticeListUiState.Error(e.message ?: "삭제에 실패했어요.")
                }
        }
    }

    private fun load(initial: Boolean) {
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
                    _uiState.value = if (current is NoticeListUiState.Success) {
                        current.copy(isRefreshing = false)
                    } else {
                        NoticeListUiState.Error(e.message ?: "공지사항을 불러오지 못했어요.")
                    }
                }
        }
    }
}
