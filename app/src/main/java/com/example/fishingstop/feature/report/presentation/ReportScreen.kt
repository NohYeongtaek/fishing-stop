package com.example.fishingstop.feature.report.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.AppCard
import com.example.fishingstop.core.ui.components.AppScaffold
import com.example.fishingstop.core.ui.components.AppTextField
import com.example.fishingstop.core.ui.components.AppTopBar
import com.example.fishingstop.core.ui.components.DisclaimerText
import com.example.fishingstop.core.ui.components.PrimaryButton
import com.example.fishingstop.core.ui.components.SecondaryButton
import com.example.fishingstop.feature.inspect.presentation.result.riskUi
import com.example.fishingstop.ui.theme.AppTheme

/** 경찰청 보이스피싱·사이버범죄 통합 신고 사이트 */
private const val OFFICIAL_REPORT_URL = "https://counterscam112.go.kr"

/** 보이스피싱 통합신고 전화번호(와이어프레임 FO_01_01_01) */
private const val UNIFIED_REPORT_NUMBER = "1394"

/**
 * 신고 화면.
 *
 * 기획 확정 사항 반영(시나리오 B — 수동 대신신고):
 *  - 문자 원문 + 신고 대상 지표(전화번호·링크 등)를 함께 전송한다(담당자가 기관에 대신 신고).
 *  - 자동추출된 지표를 사용자가 체크박스로 확인하고, 발신번호는 직접 입력할 수 있다.
 *  - 완료 화면: 신고 번호 + 통합신고 1394 안내 + 공식 신고 사이트 링크(사용자가 직접 클릭).
 *
 * @param onBack 취소/뒤로
 * @param onHome 완료 후 홈으로
 */
@Composable
fun ReportScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val colors = AppTheme.colors

    AppScaffold(
        topBar = {
            AppTopBar(
                title = if (state is ReportUiState.Success) "신고 완료" else "신고하기",
                onBack = if (state is ReportUiState.Ready || state is ReportUiState.Error) onBack else null
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (val s = state) {
                is ReportUiState.Loading, is ReportUiState.Submitting ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = colors.greenPrimary)
                        Text(
                            text = if (s is ReportUiState.Submitting) "신고를 접수하고 있어요…" else "불러오는 중…",
                            style = AppTheme.type.body,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                is ReportUiState.Ready ->
                    ReadyContent(
                        state = s,
                        onToggleIndicator = viewModel::toggleIndicator,
                        onManualPhoneChange = viewModel::setManualPhone,
                        onSubmit = viewModel::submit,
                        onBack = onBack
                    )

                is ReportUiState.Success ->
                    SuccessContent(
                        reportNumber = s.reportNumber,
                        onOpenOfficialSite = { openUrl(context, OFFICIAL_REPORT_URL) },
                        onHome = onHome
                    )

                is ReportUiState.Error ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(AppTheme.spacing.screenX),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "신고를 진행할 수 없어요\n${s.message}",
                            color = colors.dangerPrimary,
                            style = AppTheme.type.subtitle,
                            textAlign = TextAlign.Center
                        )
                        PrimaryButton(
                            text = "다시 시도",
                            onClick = viewModel::submit,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                        SecondaryButton(
                            text = "돌아가기",
                            onClick = onBack,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
            }
        }
    }
}

@Composable
private fun ReadyContent(
    state: ReportUiState.Ready,
    onToggleIndicator: (Int) -> Unit,
    onManualPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val ui = riskUi(state.riskLevel)
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.screenX, vertical = AppTheme.spacing.cardPad),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stackGap)
    ) {
        Text(
            "선택한 신고 대상과 문자 원문이 익명으로 전송되며, 담당자가 확인해 수사기관에 신고합니다. " +
                "신고자 개인정보는 수집하지 않습니다.",
            style = AppTheme.type.body,
            color = colors.textSecondary
        )

        // ── 실제 전송되는 정보 미리보기 ──
        AppCard {
            Text("전송되는 정보", style = AppTheme.type.cardLabel, color = colors.textPrimary)
            Column(
                Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(label = "위험 등급", value = ui.title)
                InfoRow(label = "AI 참고 점수", value = "${state.riskScore}점 / 100")
                InfoRow(label = "검사 방법", value = state.methodLabel)
                InfoRow(label = "문자 원문", value = "포함됨")
                if (state.signals.isNotEmpty()) {
                    Text("판단 근거", style = AppTheme.type.caption, color = colors.textTertiary)
                    state.signals.forEach { signal ->
                        Text("• $signal", style = AppTheme.type.body, color = colors.textPrimary)
                    }
                }
            }
        }

        // ── 신고 대상 확인 ──
        Text("신고 대상 확인", style = AppTheme.type.cardLabel, color = colors.textPrimary)
        Text(
            "체크한 항목만 신고에 포함됩니다.",
            style = AppTheme.type.caption,
            color = colors.textTertiary
        )

        if (state.autoIndicators.isEmpty()) {
            Text(
                "자동으로 찾은 신고 대상이 없어요. 발신 번호를 직접 입력해 주세요.",
                style = AppTheme.type.body,
                color = colors.textSecondary
            )
        } else {
            state.autoIndicators.forEachIndexed { index, indicator ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleIndicator(index) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = index in state.selectedIndicators,
                        onCheckedChange = { onToggleIndicator(index) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.greenPrimary,
                            uncheckedColor = colors.borderInput
                        )
                    )
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(indicator.type.label, style = AppTheme.type.caption, color = colors.textTertiary)
                        Text(indicator.value, style = AppTheme.type.body, color = colors.textPrimary)
                    }
                }
            }
        }

        // 발신번호 직접 입력
        AppTextField(
            value = state.manualPhone,
            onValueChange = onManualPhoneChange,
            label = "발신 전화번호 (선택)",
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        DisclaimerText()

        PrimaryButton(text = "익명으로 신고하기", onClick = onSubmit)
        SecondaryButton(text = "취소", onClick = onBack)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth()) {
        Text(
            label,
            style = AppTheme.type.body,
            color = colors.textTertiary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            value,
            style = AppTheme.type.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            color = colors.textPrimary
        )
    }
}

@Composable
private fun SuccessContent(
    reportNumber: String,
    onOpenOfficialSite: () -> Unit,
    onHome: () -> Unit
) {
    val colors = AppTheme.colors
    val elder = AppTheme.elder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.screenX, vertical = AppTheme.spacing.cardPad),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stackGap)
    ) {
        Text("✅", fontSize = 48.sp, modifier = Modifier.padding(top = 24.dp))
        Text(
            "해당 검사 결과는 신고 되었습니다",
            style = AppTheme.type.h1,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        // 어르신 모드에서는 신고번호를 작게, 1394만 크게 강조한다(Phase 3).
        if (!elder) {
            Text("신고 번호", style = AppTheme.type.body, color = colors.textSecondary)
            Text(reportNumber, style = AppTheme.type.hotlineNum.copy(fontSize = 26.sp), color = colors.greenPrimary)
        } else {
            Text("신고 번호 $reportNumber", style = AppTheme.type.caption, color = colors.textTertiary)
        }

        // 통합신고 전화 안내(와이어프레임 문구)
        AppCard {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("보이스피싱 통합신고 번호", style = AppTheme.type.body, color = colors.textSecondary)
                Text(
                    UNIFIED_REPORT_NUMBER,
                    style = AppTheme.type.hotlineNum,
                    color = colors.greenPrimary
                )
            }
        }

        Text(
            "실제 수사·처리는 전화(1394) 또는 아래 공식 신고 사이트를 통해 진행하실 수 있습니다.",
            style = AppTheme.type.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        PrimaryButton(text = "공식 신고 사이트 바로가기", onClick = onOpenOfficialSite)
        SecondaryButton(text = "돌아가기", onClick = onHome)
    }
}

/** 사용자가 직접 누른 경우에만 외부 링크(공식 신고 사이트)를 연다. */
private fun openUrl(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show() }
}
