package com.example.fishingstop.features.inspect.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishingstop.features.inspect.presentation.viewmodels.InspectResultViewModel
import com.example.fishingstop.features.risk_engine.domain.entities.RiskAnalysisResult
import com.example.fishingstop.features.risk_engine.domain.entities.RiskLevel

/**
 * 검사 결과 화면.
 *
 * 메시지/이미지/URL/QR 검사 중 어디서 왔든 동일한 형태로 결과를 보여준다.
 * 기획 원칙: 0~100 숫자 점수는 확정된 확률이 아니라 참고 지표이므로 크게 강조하지 않고,
 * 등급(안전/주의/위험)과 판정 근거·행동 조언을 화면의 중심에 둔다.
 */
@Composable
fun InspectResultScreen(
    onBackToHome: () -> Unit,
    onReportClick: () -> Unit,
    viewModel: InspectResultViewModel = hiltViewModel(),
) {
    val result by viewModel.result.collectAsState()

    Scaffold { innerPadding ->
        val current = result

        if (current == null) {
            // 결과 없이 이 화면에 바로 진입한 예외 상황(프로세스 복구 등) 대비용 안내.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("표시할 검사 결과가 없습니다.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackToHome) { Text("홈으로") }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(20.dp),
        ) {
            RiskBanner(result = current)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "판정 근거",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            current.reasons.forEach { reason ->
                Text(
                    text = "• $reason",
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "이렇게 해보세요",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = current.advice)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 숫자 점수는 참고용임을 분명히 하기 위해 작은 글씨로만 노출한다.
            Text(
                text = "AI 참고 점수 ${current.score}점",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = current.disclaimer,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onReportClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("신고하기")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("홈으로")
            }
        }
    }
}

@Composable
private fun RiskBanner(result: RiskAnalysisResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = result.level.toColor().copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = result.level.toLabel(),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = result.level.toColor(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = result.level.toHeadline(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

private fun RiskLevel.toColor(): Color = when (this) {
    RiskLevel.SAFE -> Color(0xFF2E7D32)
    RiskLevel.WARNING -> Color(0xFFF9A825)
    RiskLevel.DANGER -> Color(0xFFC62828)
}

private fun RiskLevel.toLabel(): String = when (this) {
    RiskLevel.SAFE -> "안전"
    RiskLevel.WARNING -> "주의"
    RiskLevel.DANGER -> "위험"
}

private fun RiskLevel.toHeadline(): String = when (this) {
    RiskLevel.SAFE -> "안전한 메시지로 분석되었습니다."
    RiskLevel.WARNING -> "피싱 가능성이 있어 주의가 필요합니다. 아래 근거를 확인하세요."
    RiskLevel.DANGER -> "위험한 메시지로 분석되었습니다. 요청된 행동을 따르지 마세요."
}
