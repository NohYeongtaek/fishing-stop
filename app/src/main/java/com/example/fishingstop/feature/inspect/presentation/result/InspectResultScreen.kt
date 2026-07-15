package com.example.fishingstop.feature.inspect.presentation.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.DisclaimerText
import com.example.fishingstop.core.ui.components.PrimaryButton
import com.example.fishingstop.core.util.RiskLevel
import com.example.fishingstop.feature.inspect.domain.model.InspectionResult

/**
 * 검사 결과 화면.
 *
 * 스펙 반영:
 *  - 등급(안전/주의/위험)과 판단 근거를 크게, 숫자 점수는 "AI 참고 점수"로 작게 표시.
 *  - 위험 등급이면 붉은 경고 + 동작 차단 권고, 주의면 체크리스트 형태로 근거 제시.
 *  - 하단에 "법적 증거 아님" 면책 문구 노출.
 *
 * @param onReport 신고 화면으로 이동(id 전달)
 * @param onHome   홈으로 이동
 */
@Composable
fun InspectResultScreen(
    onReport: (Long) -> Unit,
    onHome: () -> Unit,
    viewModel: InspectResultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        is InspectResultUiState.Loading ->
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

        is InspectResultUiState.NotFound ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("결과를 찾을 수 없습니다.", style = MaterialTheme.typography.titleMedium)
            }

        is InspectResultUiState.Success ->
            ResultContent(result = s.result, onReport = onReport, onHome = onHome)
    }
}

@Composable
private fun ResultContent(
    result: InspectionResult,
    onReport: (Long) -> Unit,
    onHome: () -> Unit
) {
    val ui = result.riskLevel.toUi()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 등급 카드(가장 크게 강조) ──
        // 등급과 점수를 같은 카드에 함께 보여준다. 단, 점수에는 "AI 참고 점수" 라벨을
        // 붙여 확정 확률이 아님을 유지한다(단정 금지 원칙과의 절충).
        Card(
            colors = CardDefaults.cardColors(containerColor = ui.container),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ui.title,
                        color = ui.onContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${result.riskScore}점",
                            color = ui.onContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        Text(
                            text = "AI 참고 점수 / 100",
                            color = ui.onContainer.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
                Text(text = ui.message, color = ui.onContainer, fontSize = 18.sp)
            }
        }

        // ── 검사한 내용(원문) — 접이식 카드 ──
        InputTextCard(inputText = result.inputText)

        // ── 판단 근거 ──
        if (result.signals.isNotEmpty()) {
            Text(
                text = if (result.riskLevel == RiskLevel.WARNING) "확인 체크리스트" else "판단 근거",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            result.signals.forEach { signal ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text("•  ", style = MaterialTheme.typography.bodyLarge)
                    Text(signal, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // ── 행동 권고 ──
        if (result.advice.isNotBlank()) {
            Text(
                text = "권고",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(result.advice, style = MaterialTheme.typography.bodyLarge)
        }

        DisclaimerText()

        // ── 액션 버튼 ──
        // (신고 완료 시 재신고 방지 비활성)
        // 이미 신고한 건은 재신고할 수 없도록 비활성 처리한다(와이어프레임 FO_01_01).
        PrimaryButton(
            text = if (result.isReported) "신고 완료" else "신고하기",
            enabled = !result.isReported,
            onClick = { onReport(result.id) }
        )
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text("홈으로")
        }
    }
}

/**
 * 검사한 내용(원문)을 보여주는 접이식 카드.
 * 기본은 5줄까지만 보여주고, "더보기"로 전체를 펼친다(긴 문자 대응).
 */
@Composable
private fun InputTextCard(inputText: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "검사한 내용",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = inputText.ifBlank { "(내용 없음)" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )
            // 짧은 텍스트에는 버튼을 굳이 보여주지 않는다(대략 5줄 이상일 때만).
            if (inputText.length > 120 || inputText.lineSequence().count() > 5) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "접기" else "더보기")
                }
            }
        }
    }
}
