package com.example.fishingstop.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 보조 버튼(디자인 스펙 1-7 SecondaryButton).
 * 흰 배경 + 테두리(일반 2dp / 어르신 3dp), 높이 54/66dp, 글씨 18/23sp.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = AppTheme.shapes.button,
        border = BorderStroke(AppTheme.sizes.borderInput, AppTheme.colors.borderInput),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppTheme.colors.cardBg,
            contentColor = AppTheme.colors.textPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(AppTheme.sizes.btnSecondaryH)
    ) {
        Text(text = text, style = AppTheme.type.button)
    }
}
