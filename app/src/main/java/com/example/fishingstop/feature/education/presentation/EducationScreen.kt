package com.example.fishingstop.feature.education.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.AppCard
import com.example.fishingstop.core.ui.components.SecondaryButton
import com.example.fishingstop.ui.theme.AppTheme

/** 어르신 모드에서 처음에 보여줄 카테고리 개수(그 뒤는 "더보기"로 펼침). */
private const val ELDER_INITIAL_COUNT = 5

/**
 * 예방 교육 목록 화면.
 * 사기 유형 카테고리를 나열하고, 항목을 누르면 상세로 이동한다.
 * 어르신 모드에서는 상위 5개만 먼저 보여주고 "더보기"로 나머지를 펼친다(Phase 3).
 */
@Composable
fun EducationScreen(
    onOpenCategory: (String) -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val elder = AppTheme.elder
    var expanded by remember { mutableStateOf(false) }

    val all = viewModel.categories
    val visible = if (elder && !expanded) all.take(ELDER_INITIAL_COUNT) else all

    Column(Modifier.fillMaxSize().padding(horizontal = AppTheme.spacing.screenX)) {
        Text(
            "피싱 예방 교육",
            style = AppTheme.type.h1,
            color = colors.textPrimary,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Text(
            "사기 유형별로 특징과 대처법을 알아두면 피해를 막을 수 있어요.",
            style = AppTheme.type.subtitle,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.listGap)
        ) {
            items(visible, key = { it.id }) { category ->
                AppCard(modifier = Modifier.clickable { onOpenCategory(category.id) }) {
                    Text(category.title, style = AppTheme.type.cardLabel, color = colors.textPrimary)
                    Text(
                        category.summary,
                        style = AppTheme.type.body,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (elder && !expanded && all.size > ELDER_INITIAL_COUNT) {
                item {
                    SecondaryButton(
                        text = "더보기 (${all.size - ELDER_INITIAL_COUNT}개 더)",
                        onClick = { expanded = true }
                    )
                }
            }
        }
    }
}
