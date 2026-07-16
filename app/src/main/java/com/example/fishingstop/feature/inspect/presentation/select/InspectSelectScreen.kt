package com.example.fishingstop.feature.inspect.presentation.select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fishingstop.core.ui.components.AppCard
import com.example.fishingstop.core.ui.components.ListRow
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 직접검사 탭 — 검사 선택 화면(FO_02).
 * QR·링크·이미지·문자 4개 진입만 제공하고, 실제 입력은 방식별 화면이 담당한다.
 * 모든 검사는 동일한 분석/결과 화면으로 합류한다.
 *
 * 어르신 모드에서는 사용 빈도가 높은 "문자 검사"를 맨 위로 올린다(Phase 3).
 */
@Composable
fun InspectSelectScreen(
    onQr: () -> Unit,
    onLink: () -> Unit,
    onImage: () -> Unit,
    onText: () -> Unit
) {
    val colors = AppTheme.colors

    val methods = listOf(
        ScanMethod("💬", "문자 검사", "의심 문자를 붙여넣어 검사", onText),
        ScanMethod("🔗", "링크 검사", "URL 주소가 안전한지 검사", onLink),
        ScanMethod("🖼️", "이미지 검사", "카톡·문자 캡처에서 글자 인식", onImage),
        ScanMethod("🔳", "QR 코드 검사", "QR 코드를 비춰 검사", onQr)
    )
    // 일반 모드는 와이어프레임 순서(QR·링크·이미지·문자), 어르신 모드는 문자 우선(위 순서).
    val ordered = if (AppTheme.elder) methods else methods.reversed()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.screenX, vertical = AppTheme.spacing.cardPad),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stackGap)
    ) {
        Text("직접 검사", style = AppTheme.type.h1, color = colors.textPrimary)
        Text(
            "검사할 방법을 선택하세요.",
            style = AppTheme.type.subtitle,
            color = colors.textSecondary
        )

        AppCard {
            ordered.forEachIndexed { index, m ->
                ListRow(
                    title = m.title,
                    subtitle = m.subtitle,
                    onClick = m.onClick,
                    leading = { Text(m.emoji, style = AppTheme.type.h1) }
                )
                if (index != ordered.lastIndex) {
                    androidx.compose.material3.HorizontalDivider(color = colors.borderDivider)
                }
            }
        }
    }
}

private data class ScanMethod(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)
