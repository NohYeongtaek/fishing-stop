package com.rocketdan24.fishingstop.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 앱 기본 카드(디자인 스펙 1-7 Card).
 * 반경 20/22dp, 카드 배경, 그림자. 어르신 모드에서는 테두리(2dp) 추가.
 *
 * @param padding 내부 패딩(기본 토큰 cardPad). 0으로 주면 내용이 직접 패딩 관리.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AppTheme.colors
    val border = if (AppTheme.sizes.borderCard.value > 0f) {
        BorderStroke(AppTheme.sizes.borderCard, colors.borderCard)
    } else null

    Card(
        shape = AppTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = border,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(AppTheme.spacing.cardPad), content = content)
    }
}
