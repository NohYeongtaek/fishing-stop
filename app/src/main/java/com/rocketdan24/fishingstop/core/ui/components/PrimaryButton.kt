package com.rocketdan24.fishingstop.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 앱 전역 기본 강조 버튼(디자인 스펙 1-7 PrimaryButton).
 *
 * 토큰: 높이 58/70dp, 반경 18/20dp, 글씨 19/23sp, green.primary. 어르신 모드에서 자동으로 커진다.
 * (기존 시그니처를 유지해 여러 화면의 호출부를 그대로 둔다.)
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = AppTheme.shapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.greenPrimary,
            contentColor = AppTheme.colors.onGreen
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(AppTheme.sizes.btnPrimaryH)
    ) {
        Text(text = text, style = AppTheme.type.button)
    }
}
