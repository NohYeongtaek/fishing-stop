package com.example.fishingstop.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 앱 상단바(디자인 스펙 2-3 Header). ← 뒤로 버튼 + 제목(18/23sp).
 * 배경은 크림 페이지색과 동일하게 두어 이음매 없이 보이게 한다.
 *
 * @param onBack null이면 뒤로 버튼을 숨긴다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    TopAppBar(
        title = { Text(title, style = AppTheme.type.subtitle.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = colors.textPrimary) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로",
                        tint = colors.textPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.pageBg,
            titleContentColor = colors.textPrimary
        )
    )
}
