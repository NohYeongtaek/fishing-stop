package com.example.fishingstop.feature.education.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.feature.education.domain.EducationCategory

/**
 * 예방 교육 상세 화면.
 * 유형 요약 + 의심 신호 체크리스트 + 대응 방법을 보여준다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationDetailScreen(
    onBack: () -> Unit,
    viewModel: EducationDetailViewModel = hiltViewModel()
) {
    val category = viewModel.category

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category?.title ?: "예방 교육") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (category == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("콘텐츠를 찾을 수 없습니다.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(category.summary, style = MaterialTheme.typography.bodyLarge)

            Section(title = "이런 신호를 의심하세요", items = category.warningSigns, bullet = "⚠️")
            Section(title = "이렇게 대처하세요", items = category.tips, bullet = "✅")
        }
    }
}

@Composable
private fun Section(title: String, items: List<String>, bullet: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        items.forEach { line ->
            Row {
                Text("$bullet ", style = MaterialTheme.typography.bodyLarge)
                Text(line, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
