package com.rocketdan24.fishingstop.feature.report.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.rocketdan24.fishingstop.core.navigation.Routes
import com.rocketdan24.fishingstop.core.util.Anonymizer
import com.rocketdan24.fishingstop.core.util.RiskLevel
import com.rocketdan24.fishingstop.core.util.toUserMessage
import com.rocketdan24.fishingstop.feature.inspect.domain.usecase.GetInspectionResultUseCase
import com.rocketdan24.fishingstop.feature.report.domain.ExtractIndicatorsUseCase
import com.rocketdan24.fishingstop.feature.report.domain.SubmitReportUseCase
import com.rocketdan24.fishingstop.feature.report.domain.model.Indicator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 신고 화면 상태. */
sealed interface ReportUiState {
    data object Loading : ReportUiState

    /**
     * 신고 전 확인 화면.
     * @param autoIndicators      원문에서 자동 추출된 신고 대상 후보
     * @param selectedIndicators  사용자가 신고에 포함하기로 체크한 인덱스
     * @param manualPhone         직접 입력한 발신 전화번호
     */
    data class Ready(
        val riskLevel: RiskLevel,
        val riskScore: Int,
        val methodLabel: String,
        val signals: List<String>,
        val autoIndicators: List<Indicator>,
        val selectedIndicators: Set<Int> = emptySet(),
        val manualPhone: String = ""
    ) : ReportUiState

    data object Submitting : ReportUiState

    /** 접수 완료: 신고 번호 표시 */
    data class Success(val reportNumber: String) : ReportUiState
    data class Error(val message: String) : ReportUiState
}

/**
 * 신고 ViewModel.
 * 전송될 정보(등급·점수·근거·원문)와 신고 대상 지표를 보여주고, 확인 시 서버로 전송한다.
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInspectionResultUseCase: GetInspectionResultUseCase,
    private val extractIndicatorsUseCase: ExtractIndicatorsUseCase,
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
                    signals = inspection.signals.map { Anonymizer.mask(it) },
                    autoIndicators = extractIndicatorsUseCase(inspection.inputText)
                )
            }
        }
    }

    /** 자동추출 지표 체크 토글. */
    fun toggleIndicator(index: Int) {
        _uiState.update { state ->
            if (state !is ReportUiState.Ready) return@update state
            val next = state.selectedIndicators.toMutableSet().apply {
                if (!add(index)) remove(index)
            }
            state.copy(selectedIndicators = next)
        }
    }

    /** 발신번호 직접 입력. */
    fun setManualPhone(value: String) {
        _uiState.update { state ->
            if (state is ReportUiState.Ready) state.copy(manualPhone = value) else state
        }
    }

    fun submit() {
        val ready = _uiState.value as? ReportUiState.Ready ?: return
        val confirmed = ready.autoIndicators.filterIndexed { i, _ -> i in ready.selectedIndicators }

        _uiState.value = ReportUiState.Submitting
        viewModelScope.launch {
            submitReportUseCase(
                inspectionId = args.inspectionId,
                confirmedIndicators = confirmed,
                manualPhone = ready.manualPhone
            )
                .onSuccess { number -> _uiState.value = ReportUiState.Success(number) }
                .onFailure { e -> _uiState.value = ReportUiState.Error(e.toUserMessage()) }
        }
    }
}
