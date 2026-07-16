package com.example.fishingstop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.navigation.FishingStopNavGraph
import com.example.fishingstop.core.util.ThemeMode
import com.example.fishingstop.ui.theme.FishingstopTheme

/**
 * 앱 루트 컴포저블.
 * 설정(테마/어르신 모드)을 읽어 전체 UI에 적용한 뒤 네비게이션 그래프를 그린다.
 *
 * 어르신 모드는 예전의 글씨 배율(density) 해킹이 아니라, 테마가 어르신 토큰 세트
 * (더 큰 글씨·버튼, 진한 색, 테두리 추가)를 통째로 적용하는 방식이다.
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

    FishingstopTheme(darkTheme = darkTheme, elder = elderMode) {
        FishingStopNavGraph(sharedText = sharedText, onExitApp = onExitApp)
    }
}
