package com.example.fishingstop.feature.education.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.AppCard
import com.example.fishingstop.core.ui.components.AppScaffold
import com.example.fishingstop.core.ui.components.AppTopBar
import com.example.fishingstop.core.ui.components.WarnBox
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 예방 교육 상세 화면.
 * 유형 요약 + 의심 신호 체크리스트(주의 박스) + 대응 방법(카드)을 보여준다.
 */
@Composable
fun EducationDetailScreen(
    onBack: () -> Unit,
    viewModel: EducationDetailViewModel = hiltViewModel()
) {
    val category = viewModel.category
    val colors = AppTheme.colors

    AppScaffold(
        topBar = { AppTopBar(title = category?.title ?: "예방 교육", onBack = onBack) }
    ) { innerPadding ->
        if (category == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("콘텐츠를 찾을 수 없습니다.", style = AppTheme.type.body, color = colors.textSecondary)
            }
            return@AppScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.spacing.screenX, vertical = AppTheme.spacing.cardPad),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stackGap)
        ) {
            Text(category.summary, style = AppTheme.type.body, color = colors.textPrimary)

            // 의심 신호 = 주의(warn) 박스
            WarnBox {
                Text("이런 신호를 의심하세요", style = AppTheme.type.cardLabel, color = colors.warnText)
                category.warningSigns.forEach { line ->
                    Row(Modifier.padding(top = 6.dp)) {
                        Text("• ", style = AppTheme.type.body, color = colors.warnText)
                        Text(line, style = AppTheme.type.body, color = colors.warnText)
                    }
                }
            }

            // 대처법 = 일반 카드
            AppCard {
                Text("이렇게 대처하세요", style = AppTheme.type.cardLabel, color = colors.textPrimary)
                category.tips.forEach { line ->
                    Row(Modifier.padding(top = 6.dp)) {
                        Text("• ", style = AppTheme.type.body)
                        Text(line, style = AppTheme.type.body, color = colors.textPrimary)
                    }
                }
            }
        }
    }
}