package com.rocketdan24.fishingstop.feature.inspect.presentation.analyze

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rocketdan24.fishingstop.core.ui.components.PrimaryButton
import com.rocketdan24.fishingstop.core.ui.components.SecondaryButton
import com.rocketdan24.fishingstop.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
    val ringSize = AppTheme.sizes.analyzeRing

    // 성공 상태가 되면 한 번만 결과 화면으로 이동
    LaunchedEffect(state) {
        (state as? AnalyzeUiState.Success)?.let { onResult(it.inspectionId) }
    }

    // 분석은 서버 응답이 올 때까지 실제 진행률을 알 수 없으므로, 남은 거리를 조금씩
    // 줄여나가는 점근선 방식으로 95%까지 자연스럽게 채우다가, 결과가 도착하면 100%로 마무리한다.
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(state) {
        when (state) {
            is AnalyzeUiState.Loading -> {
                progress = 0f
                while (isActive && progress < 0.95f) {
                    delay(175)
                    // ponytail: floor the step so the ring keeps visibly creeping on slow
                    // requests instead of asymptotically freezing just under 95%.
                    progress += maxOf((0.95f - progress) * 0.055f, 0.003f)
                }
            }
            is AnalyzeUiState.Success -> progress = 1f
            is AnalyzeUiState.Error -> progress = 0f
        }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200),
        label = "analyzeProgress"
    )

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
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(ringSize),
                        color = colors.greenPrimary,
                        strokeWidth = 8.dp
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = AppTheme.type.scoreNum,
                        color = colors.greenPrimary
                    )
                }
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
                // 이동은 위 LaunchedEffect에서 처리. 100%로 채운 채 잠깐 유지.
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(ringSize),
                        color = colors.greenPrimary,
                        strokeWidth = 8.dp
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = AppTheme.type.scoreNum,
                        color = colors.greenPrimary
                    )
                }
            }
        }
    }
}
