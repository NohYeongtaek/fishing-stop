package com.example.fishingstop.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.AppCard
import com.example.fishingstop.core.util.ThemeMode
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 설정 화면(FO_05): 다크/라이트 모드, 어르신 모드, 공지사항 진입,
 * 개인정보 처리방침 재열람, 버전 표기.
 *
 * @param onOpenNotice  공지사항 화면으로 이동
 * @param onOpenPrivacy 개인정보 처리방침 화면으로 이동
 */
@Composable
fun SettingsScreen(
    onOpenNotice: () -> Unit,
    onOpenPrivacy: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val elderMode by viewModel.elderMode.collectAsState()
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.screenX, vertical = AppTheme.spacing.cardPad),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stackGap + 6.dp)
    ) {
        Text("설정", style = AppTheme.type.h1, color = colors.textPrimary)

        // ── 화면 테마 ──
        SettingGroup(title = "화면 테마") {
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colors.greenPrimary,
                            unselectedColor = colors.textTertiary
                        )
                    )
                    Text(
                        mode.label,
                        style = AppTheme.type.body,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // ── 어르신 모드 ──
        SettingGroup(title = "접근성") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("어르신 모드", style = AppTheme.type.body, color = colors.textPrimary)
                    Text(
                        "글씨를 크게, 화면을 또렷하게 표시합니다.",
                        style = AppTheme.type.caption,
                        color = colors.textTertiary
                    )
                }
                Switch(
                    checked = elderMode,
                    onCheckedChange = { viewModel.setElderMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.onGreen,
                        checkedTrackColor = colors.greenPrimary,
                        uncheckedThumbColor = colors.textTertiary,
                        uncheckedTrackColor = colors.cardBg,
                        uncheckedBorderColor = colors.borderInput
                    )
                )
            }
        }

        // ── 개인정보 ──
        SettingGroup(title = "개인정보") {
            NavigationRow(text = "개인정보 처리방침 보기", onClick = onOpenPrivacy)
        }

        // ── 공지사항 ──
        SettingGroup(title = "공지") {
            NavigationRow(text = "공지사항", onClick = onOpenNotice)
        }

        // ── 버전(와이어프레임 표기 형식) ──
        Text(
            text = "app version : ${viewModel.versionName}",
            style = AppTheme.type.caption,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
    }
}

/** 다른 화면으로 이동하는 설정 행. */
@Composable
private fun NavigationRow(text: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = AppTheme.type.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text("›", style = AppTheme.type.chevron, color = colors.textPlaceholder)
    }
}

@Composable
private fun SettingGroup(title: String, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    Column {
        Text(
            title,
            style = AppTheme.type.cardLabel,
            color = colors.greenPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        AppCard { content() }
    }
}
