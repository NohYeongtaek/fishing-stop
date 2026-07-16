package com.example.fishingstop.feature.notice.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.AppCard
import com.example.fishingstop.core.ui.components.AppScaffold
import com.example.fishingstop.core.ui.components.AppTopBar
import com.example.fishingstop.core.ui.components.PrimaryButton
import com.example.fishingstop.feature.notice.domain.model.Notice
import com.example.fishingstop.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 공지사항 화면(FO_05_01) — Firestore 목록 + 당겨서 새로고침.
 * 항목을 누르면 아래로 펼쳐지는 아코디언(기존 UX 유지), 데이터만 Firestore로 교체.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeListScreen(
    onBack: () -> Unit,
    viewModel: NoticeListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors

    var expandedId by remember { mutableStateOf<String?>(null) }

    val isRefreshing = (state as? NoticeListUiState.Success)?.isRefreshing == true

    AppScaffold(
        topBar = { AppTopBar(title = "공지사항", onBack = onBack) }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            when (val s = state) {
                is NoticeListUiState.Loading ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = colors.greenPrimary)
                    }

                is NoticeListUiState.Empty ->
                    // 당겨서 새로고침이 되도록 스크롤 가능한 컨테이너로 감싼다
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Box(Modifier.fillMaxSize().padding(top = 120.dp), Alignment.TopCenter) {
                                Text(
                                    "등록된 공지가 없어요.",
                                    style = AppTheme.type.body,
                                    color = colors.textTertiary
                                )
                            }
                        }
                    }

                is NoticeListUiState.Error ->
                    Column(
                        Modifier.fillMaxSize().padding(AppTheme.spacing.screenX),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            s.message,
                            style = AppTheme.type.body,
                            color = colors.dangerPrimary
                        )
                        PrimaryButton(
                            text = "다시 시도",
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                is NoticeListUiState.Success ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = AppTheme.spacing.screenX),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.listGap)
                    ) {
                        items(s.notices, key = { it.id }) { notice ->
                            NoticeItem(
                                notice = notice,
                                expanded = expandedId == notice.id,
                                onClick = {
                                    expandedId = if (expandedId == notice.id) null else notice.id
                                }
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun NoticeItem(notice: Notice, expanded: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    AppCard(
        modifier = Modifier
            .clickable(onClick = onClick)
            .animateContentSize()
    ) {
        Text(notice.title, style = AppTheme.type.cardLabel, color = colors.textPrimary)
        Text(formatDate(notice.createdAtMillis), style = AppTheme.type.caption, color = colors.textTertiary)
        if (expanded) {
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = colors.borderDivider)
            Text(notice.body, style = AppTheme.type.body, color = colors.textSecondary)
        }
    }
}

/** 서버 미반영(0)이면 방금 등록으로 간주해 "방금 전"으로 표시. */
private fun formatDate(millis: Long): String =
    if (millis <= 0L) "방금 전"
    else SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(millis))
