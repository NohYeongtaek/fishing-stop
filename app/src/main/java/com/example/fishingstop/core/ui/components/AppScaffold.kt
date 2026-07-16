package com.example.fishingstop.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 앱 기본 스캐폴드. 크림색 페이지 배경(brand.page.bg)을 깔고, topBar/bottomBar를 받는다.
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.pageBg,
        topBar = topBar,
        bottomBar = bottomBar,
        content = content
    )
}
