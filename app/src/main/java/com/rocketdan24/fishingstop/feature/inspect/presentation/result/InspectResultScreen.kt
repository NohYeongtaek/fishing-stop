package com.rocketdan24.fishingstop.feature.inspect.presentation.result

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rocketdan24.fishingstop.core.ui.components.AppCard
import com.rocketdan24.fishingstop.core.ui.components.AppScaffold
import com.rocketdan24.fishingstop.core.ui.components.AppTopBar
import com.rocketdan24.fishingstop.core.ui.components.DisclaimerText
import com.rocketdan24.fishingstop.core.ui.components.PrimaryButton
import com.rocketdan24.fishingstop.core.ui.components.SecondaryButton
import com.rocketdan24.fishingstop.core.ui.components.WarnBox
import com.rocketdan24.fishingstop.core.util.RiskLevel
import com.rocketdan24.fishingstop.feature.inspect.domain.model.InspectionResult
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 검사 결과 화면.
 *
 * 스펙 반영:
 *  - 등급(안전/주의/위험)을 그라디언트 히어로로 크게, 숫자 점수는 "AI 참고 점수"로 작게 표시.
 *  - 어르신 모드(Phase 3): 점수 대신 큰 상태 이모지 + "① 링크 안누르기 ② 답장 안하기
 *    ③ 송금 안하기" 3단계 행동 카드를 보여준다.
 *  - 하단에 "법적 증거 아님" 면책 문구 노출.
 *
 * @param onReport 신고 화면으로 이동(id 전달)
 * @param onHome   홈으로 이동
 * @param onBack   상단바 뒤로가기(검사기록에서 들어온 경우 검사기록으로, 그 외에는 이전 화면으로)
 */
@Composable
fun InspectResultScreen(
    onReport: (Long) -> Unit,
    onHome: () -> Unit,
    onBack: () -> Unit,
    viewModel: InspectResultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    AppScaffold(topBar = { AppTopBar(title = "검사 결과", onBack = onBack) }) { innerPadding ->
        when (val s = state) {
            is InspectResultUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                    CircularProgressIndicator(color = AppTheme.colors.greenPrimary)
                }

            is InspectResultUiState.NotFound ->
                Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                    Text(
                        "결과를 찾을 수 없습니다.",
                        style = AppTheme.type.subtitle,
                        color = AppTheme.colors.textSecondary
                    )
                }

            is InspectResultUiState.Success ->
                ResultContent(
                    result = s.result,
                    contentPadding = innerPadding,
                    onReport = onReport,
                    onHome = onHome
                )
        }
    }
}

@Composable
private fun ResultContent(
    result: InspectionResult,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onReport: (Long) -> Unit,
    onHome: () -> Unit
) {
    val ui = riskUi(result.riskLevel)
    val colors = AppTheme.colors
    val elder = AppTheme.elder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.screenX, vertical = AppTheme.spacing.cardPad),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stackGap)
    ) {
        // ── 등급 히어로(그라디언트, 가장 크게 강조) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(ui.gradient), AppTheme.shapes.card)
                .padding(AppTheme.spacing.cardPad + 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(text = ui.title, color = ui.onGradient, style = AppTheme.type.h1)
                    Text(
                        text = ui.message,
                        color = ui.onGradient,
                        style = AppTheme.type.body,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                if (elder) {
                    // 어르신: 점수 대신 큰 상태 이모지
                    Text(ui.emoji, fontSize = 56.sp, modifier = Modifier.padding(start = 12.dp))
                } else {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = "${result.riskScore}",
                            color = ui.onGradient,
                            style = AppTheme.type.scoreNum
                        )
                        Text(
                            text = "AI 참고 점수/100",
                            color = ui.onGradient.copy(alpha = 0.85f),
                            style = AppTheme.type.caption
                        )
                    }
                }
            }
        }

        // ── 어르신 3단계 행동 카드(주의·위험일 때) ──
        if (elder && result.riskLevel != RiskLevel.SAFE) {
            WarnBox {
                Text(
                    "이렇게 하세요",
                    style = AppTheme.type.cardLabel,
                    color = colors.warnText
                )
                listOf("① 링크 안 누르기", "② 답장 안 하기", "③ 송금 안 하기").forEach {
                    Text(
                        it,
                        style = AppTheme.type.body,
                        color = colors.warnText,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        // ── 검사한 내용(원문) — 접이식 카드 ──
        InputTextCard(inputText = result.inputText)

        // ── 판단 근거 ──
        if (result.signals.isNotEmpty()) {
            Text(
                text = if (result.riskLevel == RiskLevel.WARNING) "확인 체크리스트" else "판단 근거",
                style = AppTheme.type.cardLabel,
                color = colors.textPrimary
            )
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.listGap)) {
                    result.signals.forEach { signal ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Text("•  ", style = AppTheme.type.body, color = colors.greenPrimary)
                            Text(
                                signal,
                                style = AppTheme.type.body,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ── 행동 권고 ──
        if (result.advice.isNotBlank()) {
            Text(text = "권고", style = AppTheme.type.cardLabel, color = colors.textPrimary)
            Text(result.advice, style = AppTheme.type.body, color = colors.textSecondary)
        }

        DisclaimerText()

        // ── 액션 버튼 ──
        // (신고 완료 시 재신고 방지 비활성)
        PrimaryButton(
            text = if (result.isReported) "신고 완료" else "신고하기",
            enabled = !result.isReported,
            onClick = { onReport(result.id) }
        )
        SecondaryButton(text = "홈으로", onClick = onHome)
    }
}

/**
 * 검사한 내용(원문)을 보여주는 접이식 카드.
 * 기본은 5줄까지만 보여주고, "더보기"로 전체를 펼친다(긴 문자 대응).
 */
@Composable
private fun InputTextCard(inputText: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val colors = AppTheme.colors

    AppCard {
        Text(
            text = "검사한 내용",
            style = AppTheme.type.cardLabel,
            color = colors.textSecondary
        )
        // animateContentSize는 내부적으로 clipToBounds()를 적용한다. 카드 전체를 감싸면
        // AppCard의 그림자(elevation)까지 잘려 다른 카드와 다르게 보이므로, 크기가
        // 실제로 바뀌는 이 텍스트에만 걸어 카드 그림자는 그대로 둔다.
        Text(
            text = inputText.ifBlank { "(내용 없음)" },
            style = AppTheme.type.body,
            color = colors.textPrimary,
            maxLines = if (expanded) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 8.dp)
                .animateContentSize()
        )
        // 짧은 텍스트에는 버튼을 굳이 보여주지 않는다(대략 5줄 이상일 때만).
        if (inputText.length > 120 || inputText.lineSequence().count() > 5) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    if (expanded) "접기" else "더보기",
                    style = AppTheme.type.button.copy(fontWeight = FontWeight.Medium),
                    color = colors.greenPrimary
                )
            }
        }
    }
}
