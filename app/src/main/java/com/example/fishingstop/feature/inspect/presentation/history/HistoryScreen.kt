package com.example.fishingstop.feature.inspect.presentation.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.feature.inspect.domain.model.InspectionResult
import com.example.fishingstop.feature.inspect.presentation.result.toUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 검사 기록 화면.
 * 과거 검사 이력을 날짜·방법·위험도로 보여주고, 즐겨찾기 토글·상세보기(결과 화면 재사용)·
 * 삭제(길게 누르기 → 확인)를 지원한다. 기록은 기기에만 저장된다.
 *
 * @param onOpenResult 항목을 누르면 해당 검사 결과 화면으로 이동
 */
@Composable
fun HistoryScreen(
    onOpenResult: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val favoritesOnly by viewModel.favoritesOnly.collectAsState()

    // 길게 눌러 삭제를 요청한 항목(확인 다이얼로그 표시용)
    var pendingDelete by remember { mutableStateOf<InspectionResult?>(null) }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("기록 삭제") },
            text = { Text("이 검사 기록을 삭제할까요?\n삭제하면 되돌릴 수 없어요.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(target)
                    pendingDelete = null
                }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("취소") }
            }
        )
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            "검사 기록",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // 필터: 전체 / 즐겨찾기
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !favoritesOnly,
                onClick = { viewModel.setFavoritesOnly(false) },
                label = { Text("전체") }
            )
            FilterChip(
                selected = favoritesOnly,
                onClick = { viewModel.setFavoritesOnly(true) },
                label = { Text("즐겨찾기") }
            )
        }

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (favoritesOnly) "즐겨찾기한 기록이 없습니다." else "검사 기록이 없습니다.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    HistoryItem(
                        item = item,
                        onClick = { onOpenResult(item.id) },
                        onLongClick = { pendingDelete = item },
                        onToggleFavorite = { viewModel.toggleFavorite(item) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryItem(
    item: InspectionResult,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val ui = item.riskLevel.toUi()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // 짧게 누르면 상세, 길게 누르면 삭제 확인
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 위험 등급 뱃지
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ui.container)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(ui.title, color = ui.onContainer, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "${item.method.label} · ${formatDate(item.createdAt)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.inputText.replace("\n", " ").take(40).ifBlank { "(내용 없음)" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }

            // 즐겨찾기 토글 (아이콘 폰트 의존을 피해 유니코드 별 사용)
            IconButton(onClick = onToggleFavorite) {
                Text(
                    text = if (item.isFavorite) "★" else "☆",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (item.isFavorite) ui.container else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).format(Date(epochMillis))
