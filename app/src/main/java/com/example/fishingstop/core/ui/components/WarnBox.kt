package com.example.fishingstop.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 주의 안내 박스(디자인 스펙 warn.* 색). "먼저 열지 마세요" 같은 경고 문구용.
 * warn.bg 배경 + warn.border 테두리. 내부 텍스트는 warn.text 색을 쓰도록 호출부에서 지정.
 */
@Composable
fun WarnBox(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.warnBg, AppTheme.shapes.card)
            .border(BorderStroke(1.dp, colors.warnBorder), AppTheme.shapes.card)
            .padding(AppTheme.spacing.cardPad),
        content = content
    )
}
