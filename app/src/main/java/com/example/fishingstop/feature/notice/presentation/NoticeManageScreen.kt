package com.example.fishingstop.feature.notice.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
 * 공지 관리 화면(관리자). 목록을 아코디언으로 펼쳐 상세를 확인하고,
 * 상세 하단 우측의 수정(연필)·삭제(휴지통) 버튼으로 편집한다.
 *
 * 자동 새로고침:
 *  - 삭제: ViewModel이 삭제 후 목록을 다시 불러온다.
 *  - 수정 복귀: 화면이 다시 보일 때(ON_RESUME) 목록을 새로고침한다.
 *
 * @param onEdit 수정 화면으로 이동(공지 id 전달)
 * @param onBack 뒤로
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeManageScreen(
    onEdit: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: NoticeManageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors

    var expandedId by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<Notice?>(null) }

    // 수정 화면에 다녀오면(ON_RESUME) 목록을 새로고침한다. 최초 진입 시엔 init 로드가 이미 있으므로 건너뛴다.
    val lifecycleOwner = LocalLifecycleOwner.current
    var firstResumeDone by rememberSaveable { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (firstResumeDone) viewModel.refresh() else firstResumeDone = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 삭제 확인 다이얼로그
    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = colors.cardBg,
            title = { Text("공지 삭제", style = AppTheme.type.cardLabel, color = colors.textPrimary) },
            text = {
                Text(
                    "‘${target.title}’ 공지를 삭제할까요?\n삭제하면 되돌릴 수 없어요.",
                    style = AppTheme.type.body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(target.id)
                    pendingDelete = null
                }) { Text("삭제", color = colors.dangerPrimary, style = AppTheme.type.button) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("취소", color = colors.textSecondary, style = AppTheme.type.button)
                }
            }
        )
    }

    val isRefreshing = (state as? NoticeListUiState.Success)?.isRefreshing == true

    AppScaffold(
        topBar = { AppTopBar(title = "공지 관리", onBack = onBack) }
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
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Box(Modifier.fillMaxSize().padding(top = 120.dp), Alignment.TopCenter) {
                                Text("등록된 공지가 없어요.", style = AppTheme.type.body, color = colors.textTertiary)
                            }
                        }
                    }

                is NoticeListUiState.Error ->
                    Column(
                        Modifier.fillMaxSize().padding(AppTheme.spacing.screenX),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(s.message, style = AppTheme.type.body, color = colors.dangerPrimary)
                        PrimaryButton(
                            text = "다시 시도",
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                is NoticeListUiState.Success ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = AppTheme.spacing.screenX),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.listGap)
                    ) {
                        items(s.notices, key = { it.id }) { notice ->
                            ManageNoticeItem(
                                notice = notice,
                                expanded = expandedId == notice.id,
                                onClick = { expandedId = if (expandedId == notice.id) null else notice.id },
                                onEdit = { onEdit(notice.id) },
                                onDelete = { pendingDelete = notice }
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun ManageNoticeItem(
    notice: Notice,
    expanded: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
            // content 하단 우측: 삭제(휴지통) + 수정(연필)
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "삭제", tint = colors.dangerPrimary)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "수정", tint = colors.greenPrimary)
                }
            }
        }
    }
}

private fun formatDate(millis: Long): String =
    if (millis <= 0L) "방금 전"
    else SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(millis))
