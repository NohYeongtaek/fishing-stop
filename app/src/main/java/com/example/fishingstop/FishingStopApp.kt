package com.example.fishingstop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.navigation.FishingStopNavGraph
import com.example.fishingstop.core.util.ThemeMode
import com.example.fishingstop.ui.theme.FishingstopTheme

/**
 * 앱 루트 컴포저블.
 * 설정(테마/어르신 모드)을 읽어 전체 UI에 적용한 뒤 네비게이션 그래프를 그린다.
 *
 * @param sharedText 공유(ACTION_SEND)로 전달된 텍스트(없으면 null)
 * @param onExitApp  동의 거부 시 앱 종료 콜백
 */
@Composable
fun FishingStopApp(
    sharedText: String?,
    onExitApp: () -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val themeMode by appViewModel.themeMode.collectAsState()
    val elderMode by appViewModel.elderMode.collectAsState()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // 어르신 모드에서는 고대비를 위해 동적 색상을 끄고, 글씨를 크게 키운다.
    FishingstopTheme(darkTheme = darkTheme, dynamicColor = !elderMode) {
        // 분기(if/else)로 감싸면 컴포지션 구조가 달라져 NavGraph가 재생성(내비게이션 리셋)된다.
        // 항상 CompositionLocalProvider로 감싸고, 밀도(글씨 배율)만 바꿔 그 문제를 피한다.
        val base = LocalDensity.current
        val density = if (elderMode) Density(base.density, base.fontScale * 1.3f) else base
        CompositionLocalProvider(LocalDensity provides density) {
            FishingStopNavGraph(sharedText = sharedText, onExitApp = onExitApp)
        }
    }
}
