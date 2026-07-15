package com.example.fishingstop.features.qr_scan.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.features.inspect.domain.usecase.InspectTextUseCase
import com.example.fishingstop.features.risk_engine.domain.entities.RiskAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** QR 검사 화면의 상태. */
sealed interface QrScanUiState {
    /** 카메라로 QR을 찾는 중. */
    data object Scanning : QrScanUiState

    /** 검사하기 버튼을 눌러 위험도 판정을 기다리는 중. */
    data object Analyzing : QrScanUiState

    /** 판정 완료. 결과 화면으로 이동할 때 사용한다. */
    data class Completed(val result: RiskAnalysisResult) : QrScanUiState

    /** QR 미인식, 네트워크 오류 등으로 검사를 진행하지 못한 경우. */
    data class Failed(val message: String) : QrScanUiState
}

/**
 * QR 코드 촬영 화면의 ViewModel.
 *
 * 카메라 Analyzer(QrCodeAnalyzer)가 프레임마다 넘겨주는 인식 값을 계속 보관하다가,
 * 사용자가 "검사하기" 버튼을 눌렀을 때의 값만 사용해 URL로 정리한 뒤
 * 공통 검사 파이프라인인 InspectTextUseCase(→ risk_engine)에 넘긴다.
 * risk_engine 내부 구현은 이 파일에서 직접 참조하지 않는다.
 */
@HiltViewModel
class QrScanViewModel @Inject constructor(
    private val inspectTextUseCase: InspectTextUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<QrScanUiState>(QrScanUiState.Scanning)
    val uiState: StateFlow<QrScanUiState> = _uiState.asStateFlow()

    private var lastDetectedValue: String? = null

    /** 카메라 프레임에서 QR이 인식될 때마다 호출된다. */
    fun onQrDetected(rawValue: String) {
        lastDetectedValue = rawValue
    }

    /** "검사하기" 버튼 클릭 시 호출. 마지막으로 인식된 QR 값을 URL로 만들어 검사를 시작한다. */
    fun onInspectClick() {
        val rawValue = lastDetectedValue

        if (rawValue.isNullOrBlank()) {
            _uiState.value = QrScanUiState.Failed("아직 QR 코드가 인식되지 않았습니다. 카메라를 QR 코드에 맞춰주세요.")
            return
        }

        val url = normalizeToUrl(rawValue)

        viewModelScope.launch {
            _uiState.value = QrScanUiState.Analyzing
            runCatching { inspectTextUseCase(text = url, urls = listOf(url)) }
                .onSuccess { result -> _uiState.value = QrScanUiState.Completed(result) }
                .onFailure { e -> _uiState.value = QrScanUiState.Failed(e.message ?: "검사 중 오류가 발생했습니다.") }
        }
    }

    /** 결과 확인 후 다시 촬영하고 싶을 때 호출. */
    fun resetToScanning() {
        lastDetectedValue = null
        _uiState.value = QrScanUiState.Scanning
    }

    // QR 원문이 스킴 없이 도메인/경로만 담고 있는 경우를 대비해 http(s) 스킴을 보정한다.
    private fun normalizeToUrl(rawValue: String): String {
        val trimmed = rawValue.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }
}
