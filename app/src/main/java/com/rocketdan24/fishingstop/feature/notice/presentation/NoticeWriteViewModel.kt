package com.rocketdan24.fishingstop.feature.notice.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.rocketdan24.fishingstop.core.navigation.Routes
import com.rocketdan24.fishingstop.feature.notice.domain.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 공지 작성/수정 폼 상태.
 *
 * @param loading 수정 모드에서 기존 공지를 불러오는 중
 * @param isEdit  수정 모드 여부(제목/버튼 문구 분기)
 * @param done    저장 성공(화면이 감지해 목록으로 복귀)
 */
data class NoticeWriteState(
    val loading: Boolean = false,
    val isEdit: Boolean = false,
    val title: String = "",
    val body: String = "",
    val submitting: Boolean = false,
    val done: Boolean = false,
    val error: String? = null
)

/**
 * 공지 작성/수정 ViewModel.
 * 라우트의 noticeId 가 있으면 수정 모드(기존 내용 프리필 → update), 없으면 작성 모드(add).
 */
@HiltViewModel
class NoticeWriteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NoticeRepository
) : ViewModel() {

    private val noticeId: String? = savedStateHandle.toRoute<Routes.NoticeWrite>().noticeId

    private val _state = MutableStateFlow(
        NoticeWriteState(loading = noticeId != null, isEdit = noticeId != null)
    )
    val state: StateFlow<NoticeWriteState> = _state.asStateFlow()

    init {
        if (noticeId != null) load(noticeId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            runCatching { repository.getNotice(id) }
                .onSuccess { notice ->
                    _state.update {
                        it.copy(
                            loading = false,
                            title = notice?.title.orEmpty(),
                            body = notice?.body.orEmpty()
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "공지를 불러오지 못했어요.") }
                }
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value) }
    fun onBodyChange(value: String) = _state.update { it.copy(body = value) }

    fun submit() {
        val current = _state.value
        if (current.submitting) return
        if (current.title.isBlank() || current.body.isBlank()) {
            _state.update { it.copy(error = "제목과 내용을 입력해 주세요.") }
            return
        }
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            val title = current.title.trim()
            val body = current.body.trim()
            runCatching {
                if (noticeId != null) repository.updateNotice(noticeId, title, body)
                else repository.addNotice(title, body)
            }
                .onSuccess { _state.update { it.copy(submitting = false, done = true) } }
                .onFailure { e -> _state.update { it.copy(submitting = false, error = e.message ?: "저장에 실패했어요.") } }
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }
}
