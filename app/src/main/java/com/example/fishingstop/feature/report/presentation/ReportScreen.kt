package com.example.fishingstop.feature.report.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.DisclaimerText
import com.example.fishingstop.core.ui.components.PrimaryButton
import com.example.fishingstop.feature.inspect.presentation.result.toUi

/** 경찰청 보이스피싱·사이버범죄 통합 신고 사이트 */
private const val OFFICIAL_REPORT_URL = "https://counterscam112.go.kr"

/** 보이스피싱 통합신고 전화번호(와이어프레임 FO_01_01_01) */
private const val UNIFIED_REPORT_NUMBER = "1394"

/**
 * 신고 화면.
 *
 * 기획 확정 사항 반영:
 *  - 원문은 전송하지 않으며, 화면에도 "실제 전송되는 정보"(등급·점수·근거)만 보여준다.
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

    Box(Modifier.fillMaxSize()) {
        when (val s = state) {
            is ReportUiState.Loading, is ReportUiState.Submitting ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = if (s is ReportUiState.Submitting) "신고를 접수하고 있어요…" else "불러오는 중…",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

            is ReportUiState.Ready ->
                ReadyContent(state = s, onSubmit = viewModel::submit, onBack = onBack)

            is ReportUiState.Success ->
                SuccessContent(
                    reportNumber = s.reportNumber,
                    onOpenOfficialSite = { openUrl(context, OFFICIAL_REPORT_URL) },
                    onHome = onHome
                )

            is ReportUiState.Error ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "신고를 진행할 수 없어요\n${s.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                    PrimaryButton(
                        text = "다시 시도",
                        onClick = viewModel::submit,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) { Text("돌아가기") }
                }
        }
    }
}

@Composable
private fun ReadyContent(
    state: ReportUiState.Ready,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val ui = state.riskLevel.toUi()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("신고하기", style = MaterialTheme.typography.headlineSmall)
        Text(
            "아래 분석 정보만 익명으로 전송됩니다. 메시지 원문과 개인정보는 전송되지 않습니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ── 실제 전송되는 정보 미리보기 ──
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "전송되는 정보",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                InfoRow(label = "위험 등급", value = ui.title)
                InfoRow(label = "AI 참고 점수", value = "${state.riskScore}점 / 100")
                InfoRow(label = "검사 방법", value = state.methodLabel)
                if (state.signals.isNotEmpty()) {
                    Text("판단 근거", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    state.signals.forEach { signal ->
                        Text("• $signal", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        DisclaimerText()

        PrimaryButton(text = "익명으로 신고하기", onClick = onSubmit)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("취소")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SuccessContent(
    reportNumber: String,
    onOpenOfficialSite: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "해당 검사 결과는 신고 되었습니다",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 40.dp)
        )
        Text("신고 번호", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = reportNumber,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 통합신고 전화 안내(와이어프레임 문구)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("보이스피싱 통합신고 번호", style = MaterialTheme.typography.bodyMedium)
                Text(
                    UNIFIED_REPORT_NUMBER,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            "실제 수사·처리는 전화(1394) 또는 아래 공식 신고 사이트를 통해 진행하실 수 있습니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PrimaryButton(text = "공식 신고 사이트 바로가기", onClick = onOpenOfficialSite)
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text("돌아가기")
        }
    }
}

/** 사용자가 직접 누른 경우에만 외부 링크(공식 신고 사이트)를 연다. */
private fun openUrl(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show() }
}
