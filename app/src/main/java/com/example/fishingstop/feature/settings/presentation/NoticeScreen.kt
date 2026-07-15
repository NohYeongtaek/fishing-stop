package com.example.fishingstop.feature.settings.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 공지사항 화면(FO_05_01) — 월별 목록 + 아코디언(와이어프레임 확정).
 * 항목을 누르면 아래로 펼쳐지고, 다른 항목을 누르면 기존 항목은 닫힌다.
 * 이번 버전은 로컬 데이터이며, 서버 공지는 v2에서 연동한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeScreen(onBack: () -> Unit) {
    // 현재 펼쳐진 공지 id (하나만 펼침 — 아코디언)
    var expandedId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("공지사항") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(NOTICES, key = { it.id }) { notice ->
                NoticeItem(
                    notice = notice,
                    expanded = expandedId == notice.id,
                    onClick = {
                        // 같은 항목이면 닫고, 다른 항목이면 그것만 펼친다(아코디언).
                        expandedId = if (expandedId == notice.id) null else notice.id
                    }
                )
            }
        }
    }
}

@Composable
private fun NoticeItem(notice: Notice, expanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize() // 펼침/닫힘 부드럽게
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(notice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                notice.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                Text(notice.body, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** 공지 데이터(로컬). 서버 공지 연동 시 이 목록만 원격 데이터로 교체하면 된다. */
private data class Notice(val id: String, val title: String, val date: String, val body: String)

private val NOTICES = listOf(
    Notice(
        id = "n2",
        title = "AI 위험도 분석 기능 추가",
        date = "2026.07.14",
        body = "메시지·이미지·링크·QR 검사에 AI 기반 위험도 분석이 적용되었습니다. " +
            "분석 결과는 참고 정보이며, 의심스러운 경우 반드시 공식 기관을 통해 확인하세요."
    ),
    Notice(
        id = "n1",
        title = "피싱멈춰! 정식 출시",
        date = "2026.07.14",
        body = "보이스피싱·스미싱 의심 메시지를 쉽게 확인할 수 있는 피싱멈춰!가 출시되었습니다. " +
            "많은 이용 부탁드립니다."
    )
)
