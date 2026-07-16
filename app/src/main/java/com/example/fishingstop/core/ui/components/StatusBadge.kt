package com.example.fishingstop.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fishingstop.core.util.RiskLevel
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 위험 등급 뱃지(디자인 스펙 1-7 StatusBadge). pill 형태, 등급별 색.
 * 안전=green.chip, 위험=danger.chip, 주의=warn 톤.
 */
@Composable
fun StatusBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val (bg, fg, label) = when (riskLevel) {
        RiskLevel.SAFE -> Triple(colors.greenChipBg, colors.greenChipText, "안전")
        RiskLevel.WARNING -> Triple(colors.warnBg, colors.warnText, "주의")
        RiskLevel.DANGER -> Triple(colors.dangerChipBg, colors.dangerChipText, "위험")
    }
    val elder = AppTheme.elder

    Text(
        text = label,
        color = fg,
        style = AppTheme.type.cardLabel.copy(fontWeight = FontWeight.Black),
        modifier = modifier
            .background(bg, AppTheme.shapes.pill)
            .padding(
                horizontal = if (elder) 16.dp else 12.dp,
                vertical = if (elder) 6.dp else 4.dp
            )
    )
}
