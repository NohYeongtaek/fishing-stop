package com.rocketdan24.fishingstop.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 필터 칩(디자인 스펙 1-7 FilterChip). pill 형태.
 * 활성=green.primary 배경/흰 글씨, 비활성=흰 배경+테두리. 줄바꿈 없음(nowrap).
 */
@Composable
fun AppFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val elder = AppTheme.elder

    val base = modifier
        .clickable(onClick = onClick)
        .then(
            if (selected) {
                Modifier.background(colors.greenPrimary, AppTheme.shapes.pill)
            } else {
                Modifier
                    .background(colors.cardBg, AppTheme.shapes.pill)
                    .border(BorderStroke(1.dp, colors.borderInput), AppTheme.shapes.pill)
            }
        )
        .padding(
            horizontal = if (elder) 22.dp else 18.dp,
            vertical = if (elder) 12.dp else 9.dp
        )

    Text(
        text = text,
        color = if (selected) colors.onGreen else colors.textSecondary,
        style = AppTheme.type.caption,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        softWrap = false,
        modifier = base
    )
}
