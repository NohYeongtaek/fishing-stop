package com.example.fishingstop.feature.education.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * 예방 교육 목록 화면.
 * 사기 유형 카테고리를 나열하고, 항목을 누르면 상세로 이동한다.
 */
@Composable
fun EducationScreen(
    onOpenCategory: (String) -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            "피싱 예방 교육",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Text(
            "사기 유형별로 특징과 대처법을 알아두면 피해를 막을 수 있어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.categories, key = { it.id }) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCategory(category.id) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            category.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            category.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
