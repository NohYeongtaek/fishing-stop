package com.example.fishingstop.feature.inspect.presentation.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fishingstop.core.navigation.Routes
import com.example.fishingstop.feature.inspect.domain.usecase.ObserveInspectionResultUseCase
import com.example.fishingstop.feature.inspect.domain.model.InspectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** 결과 화면 상태. */
sealed interface InspectResultUiState {
    data object Loading : InspectResultUiState
    data class Success(val result: InspectionResult) : InspectResultUiState
    data object NotFound : InspectResultUiState
}

/**
 * 결과 화면 ViewModel.
 * 라우트의 inspectionId 를 Room Flow로 관찰해, 신고 완료/즐겨찾기 변경이 즉시 반영된다.
 */
@HiltViewModel
class InspectResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeInspectionResult: ObserveInspectionResultUseCase
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Routes.InspectResult>()

    val uiState: StateFlow<InspectResultUiState> =
        observeInspectionResult(args.inspectionId)
            .map { result ->
                result?.let { InspectResultUiState.Success(it) } ?: InspectResultUiState.NotFound
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InspectResultUiState.Loading
            )
}
