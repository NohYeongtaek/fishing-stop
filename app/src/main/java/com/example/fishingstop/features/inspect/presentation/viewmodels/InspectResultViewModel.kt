package com.example.fishingstop.features.inspect.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.fishingstop.features.risk_engine.domain.entities.RiskAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 검사 결과 화면이 보여줄 최신 RiskAnalysisResult를 들고 있는 공유 ViewModel.
 *
 * QR 검사, 메시지 검사, 이미지 검사, URL 검사 등 어떤 화면에서 검사를 마쳤든
 * 결과 화면으로 넘어가기 직전에 setResult()로 값을 채워두면, 같은 인스턴스를
 * Activity 범위로 주입받는 결과 화면이 그 값을 읽어 보여준다.
 *
 * RiskAnalysisResult처럼 필드가 많은 객체를 Navigation 인자(문자열/번들)로 직접 넘기면
 * 화면이 늘어날 때마다 직렬화 코드를 반복해야 하므로, 이 프로젝트에서는 의도적으로
 * "Activity 범위 공유 ViewModel"로 결과를 전달하는 방식을 택했다.
 */
@HiltViewModel
class InspectResultViewModel @Inject constructor() : ViewModel() {

    private val _result = MutableStateFlow<RiskAnalysisResult?>(null)
    val result: StateFlow<RiskAnalysisResult?> = _result.asStateFlow()

    fun setResult(result: RiskAnalysisResult) {
        _result.value = result
    }
}
