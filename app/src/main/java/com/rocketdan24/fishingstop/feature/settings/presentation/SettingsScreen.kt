package com.rocketdan24.fishingstop.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rocketdan24.fishingstop.core.ui.components.AppCard
import com.rocketdan24.fishingstop.core.util.ThemeMode
import com.rocketdan24.fishingstop.feature.notice.presentation.AdminPinDialog
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 설정 화면(FO_05): 다크/라이트 모드, 어르신 모드, 공지사항 진입,
 * 개인정보 처리방침 재열람, 버전 표기.
 *
 * "app version" 텍스트를 10번 연속 누르면 관리자 PIN 다이얼로그가 뜨고, 통과 시
 * 공지 작성 화면으로 이동한다(숨은 게이트). 힌트는 노출하지 않는다.
 *
 * @param onOpenNotice  공지사항 화면으로 이동
 * @param onOpenPrivacy 개인정보 처리방침 화면으로 이동
 * @param onOpenAdmin 관리자 PIN 통과 시 관리자 화면으로 이동
 * @param elderModeCoachMarkModifier 홈 탭 코치마크가 어르신 모드 행을 짚어주기 위한 modifier
 */
@Composable
fun SettingsScreen(
    onOpenNotice: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAdmin: () -> Unit,
    elderModeCoachMarkModifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val elderMode by viewModel.elderMode.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val colors = AppTheme.colors

    // 숨은 관리자 게이트: 버전 텍스트 10탭 → PIN
    var versionTapCount by remember { mutableIntStateOf(0) }
    var showPinDialog by remember { mutableStateOf(false) }

    if (showPinDialog) {
        AdminPinDialog(
            onSuccess = {
                showPinDialog = false
                versionTapCount = 0
                onOpenAdmin()
            },
            onDismiss = {
                showPinDialog = false
                versionTapCount = 0
            }
        )
    }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(elderModeCoachMarkModifier),
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

        // ── 알림 ──
        SettingGroup(title = "알림") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("공지 알림 받기", style = AppTheme.type.body, color = colors.textPrimary)
                    Text(
                        "새 공지사항이 등록되면 알림을 보내드립니다.",
                        style = AppTheme.type.caption,
                        color = colors.textTertiary
                    )
                }
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { viewModel.setNotificationEnabled(it) },
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

        // ── 버전(와이어프레임 표기 형식) + 숨은 관리자 게이트(10탭) ──
        // 리플/터치효과 없이(=일반 텍스트처럼 보이게) 클릭만 받는다.
        Text(
            text = "app version : ${viewModel.versionName}",
            style = AppTheme.type.caption,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    versionTapCount++
                    if (versionTapCount >= 10) {
                        versionTapCount = 0
                        showPinDialog = true
                    }
                }
                .padding(vertical = 8.dp)
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
