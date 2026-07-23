package com.rocketdan24.fishingstop.core.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 앱 입력창(디자인 스펙 1-7 Input). 반경 18/20dp, 테두리 2/3dp, 글씨 body(16/20sp).
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val colors = AppTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label?.let { { Text(it, style = AppTheme.type.caption) } },
        singleLine = singleLine,
        textStyle = AppTheme.type.body,
        shape = AppTheme.shapes.input,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.greenPrimary,
            unfocusedBorderColor = colors.borderInput,
            focusedContainerColor = colors.cardBg,
            unfocusedContainerColor = colors.cardBg,
            unfocusedPlaceholderColor = colors.textPlaceholder,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
        ),
        modifier = modifier
    )
}
