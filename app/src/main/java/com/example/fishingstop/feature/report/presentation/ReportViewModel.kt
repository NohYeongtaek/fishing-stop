package com.example.fishingstop.feature.report.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fishingstop.core.navigation.Routes
import com.example.fishingstop.core.util.Anonymizer
import com.example.fishingstop.core.util.RiskLevel
import com.example.fishingstop.core.util.toUserMessage
import com.example.fishingstop.feature.inspect.domain.GetInspectionResultUseCase
import com.example.fishingstop.feature.report.domain.SubmitReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 신고 화면 상태. */
sealed interface ReportUiState {
    data object Loading : ReportUiState

    /**
     * 신고 전 확인: "실제로 전송되는 정보"를 그대로 보여준다.
     * (원문은 전송되지 않으므로 미리보기에도 원문을 넣지 않는다.)
     */
    data class Ready(
        val riskLevel: RiskLevel,
        val riskScore: Int,
        val methodLabel: String,
        val signals: List<String>
    ) : ReportUiState

    data object Submitting : ReportUiState

    /** 접수 완료: 신고 번호 표시 */
    data class Success(val reportNumber: String) : ReportUiState
    data class Error(val message: String) : ReportUiState
}

/**
 * 신고 ViewModel.
 * 전송될 정보(등급·점수·근거)만 미리 보여주고, 확인 시 서버로 전송한다.
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInspectionResultUseCase: GetInspectionResultUseCase,
    private val submitReportUseCase: SubmitReportUseCase
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Routes.Report>()

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val inspection = getInspectionResultUseCase(args.inspectionId)
            _uiState.value = when {
                inspection == null -> ReportUiState.Error("신고할 검사 결과를 찾을 수 없습니다.")
                inspection.isReported -> ReportUiState.Error("이미 신고된 검사 결과입니다.")
                else -> ReportUiState.Ready(
                    riskLevel = inspection.riskLevel,
                    riskScore = inspection.riskScore,
                    methodLabel = inspection.method.label,
                    // 실제 전송본과 동일하게 마스킹된 근거를 보여준다.
                    signals = inspection.signals.map { Anonymizer.mask(it) }
                )
            }
        }
    }

    fun submit() {
        _uiState.value = ReportUiState.Submitting
        viewModelScope.launch {
            submitReportUseCase(args.inspectionId)
                .onSuccess { number -> _uiState.value = ReportUiState.Success(number) }
                .onFailure { e -> _uiState.value = ReportUiState.Error(e.toUserMessage()) }
        }
    }
}
