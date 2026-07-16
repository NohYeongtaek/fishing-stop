package com.example.fishingstop.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 리스트 행(디자인 스펙 1-7 ListRow). 아이콘 박스 + 제목/설명 + chevron(›).
 * 직접검사 방식 선택, 설정 링크 등에 사용. 아이콘 박스 52/62dp, chevron 20/26sp.
 *
 * @param leading 아이콘 박스 안에 그릴 내용(이모지 Text 또는 Icon). null이면 박스 생략.
 */
@Composable
fun ListRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    showChevron: Boolean = true
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Box(
                modifier = Modifier
                    .size(AppTheme.sizes.iconBox)
                    .background(colors.greenTint, AppTheme.shapes.iconBox),
                contentAlignment = Alignment.Center
            ) { leading() }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(title, style = AppTheme.type.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = colors.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = AppTheme.type.caption, color = colors.textTertiary)
            }
        }

        if (showChevron) {
            Text("›", style = AppTheme.type.chevron, color = colors.textPlaceholder)
        }
    }
}
