package com.example.fishingstop.feature.splash.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 스플래시 화면.
 * 최초 실행 상태가 로딩되면 다음 화면으로 즉시 분기한다.
 *
 * 분기 규칙(동의 필수):
 *  - 미동의 → 동의 화면 (거부 시 앱 종료)
 *  - 동의 & 온보딩 미열람 → 온보딩
 *  - 동의 & 온보딩 열람 & 공유 진입 → 문자 검사(자동 입력)
 *  - 동의 & 온보딩 열람 → 홈
 *
 * @param sharedText 공유(ACTION_SEND)로 앱이 시작된 경우의 원문
 */
@Composable
fun SplashScreen(
    sharedText: String?,
    onNavigateToConsent: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAnalyze: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsState()

    LaunchedEffect(status) {
        val s = status ?: return@LaunchedEffect // 로딩 중: 대기
        when {
            !s.agreed -> onNavigateToConsent()
            !s.onboardingSeen -> onNavigateToOnboarding()
            !sharedText.isNullOrBlank() -> onNavigateToAnalyze(sharedText)
            else -> onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.pageBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "피싱멈춰!",
            style = AppTheme.type.h1,
            color = AppTheme.colors.greenPrimary
        )
    }
}
