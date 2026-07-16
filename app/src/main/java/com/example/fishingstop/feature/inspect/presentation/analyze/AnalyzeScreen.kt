package com.example.fishingstop.feature.inspect.presentation.analyze

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.PrimaryButton
import com.example.fishingstop.core.ui.components.SecondaryButton
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 분석 진행(로딩) 화면.
 * 분석이 끝나면 결과 화면으로 즉시 이동하고, 실패 시 재시도 버튼을 보여준다.
 *
 * @param onResult 분석 성공 → 결과 화면(id)으로 이동
 * @param onCancel 취소/뒤로
 */
@Composable
fun AnalyzeScreen(
    onResult: (Long) -> Unit,
    onCancel: () -> Unit,
    viewModel: AnalyzeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors

    // 성공 상태가 되면 한 번만 결과 화면으로 이동
    LaunchedEffect(state) {
        (state as? AnalyzeUiState.Success)?.let { onResult(it.inspectionId) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.pageBg)
            .padding(AppTheme.spacing.screenX),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val s = state) {
            is AnalyzeUiState.Loading -> {
                CircularProgressIndicator(color = colors.greenPrimary)
                Text(
                    text = "메시지를 분석하고 있어요…",
                    style = AppTheme.type.subtitle,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            is AnalyzeUiState.Error -> {
                Text(
                    text = "분석에 실패했어요\n${s.message}",
                    style = AppTheme.type.subtitle,
                    color = colors.dangerPrimary,
                    textAlign = TextAlign.Center
                )
                PrimaryButton(
                    text = "다시 시도",
                    onClick = { viewModel.analyze() },
                    modifier = Modifier.padding(top = 24.dp)
                )
                SecondaryButton(
                    text = "취소",
                    onClick = onCancel,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            is AnalyzeUiState.Success -> {
                // 이동은 위 LaunchedEffect에서 처리. 잠깐 스피너 유지.
                CircularProgressIndicator(color = colors.greenPrimary)
            }
        }
    }
}
