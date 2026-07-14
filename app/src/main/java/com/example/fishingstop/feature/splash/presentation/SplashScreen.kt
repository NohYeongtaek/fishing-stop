package com.example.fishingstop.feature.splash.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * 스플래시 화면.
 * 동의 상태가 로딩되면 다음 화면으로 즉시 분기한다.
 *
 * 분기 규칙(기획 확정):
 *  - 공유(ACTION_SEND) 진입 → 곧장 검사 플로우(동의는 검사 게이트가 처리)
 *  - 동의 화면을 아직 본 적 없음(최초 실행) → 동의 화면
 *  - 그 외(동의했든 둘러보기를 택했든) → 홈
 *
 * @param sharedText 공유로 앱이 시작된 경우의 원문
 */
@Composable
fun SplashScreen(
    sharedText: String?,
    onNavigateToConsent: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAnalyze: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsState()

    LaunchedEffect(status) {
        val s = status ?: return@LaunchedEffect // 로딩 중: 대기
        when {
            !sharedText.isNullOrBlank() -> onNavigateToAnalyze(sharedText)
            !s.seen -> onNavigateToConsent()
            else -> onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "피싱멈춰!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
