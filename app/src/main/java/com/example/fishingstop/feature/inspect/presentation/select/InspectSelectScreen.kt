package com.example.fishingstop.feature.inspect.presentation.select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 직접검사 탭 — 검사 선택 화면(FO_02).
 * QR·링크·이미지·문자 4개 진입 버튼만 제공하고, 실제 입력은 방식별 화면이 담당한다.
 * 모든 검사는 동일한 분석/결과 화면으로 합류한다.
 */
@Composable
fun InspectSelectScreen(
    onQr: () -> Unit,
    onLink: () -> Unit,
    onImage: () -> Unit,
    onText: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("직접 검사", style = MaterialTheme.typography.headlineSmall)
        Text(
            "검사할 방법을 선택하세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SelectButton(text = "QR 코드 검사", onClick = onQr)
        SelectButton(text = "링크 검사", onClick = onLink)
        SelectButton(text = "이미지 검사", onClick = onImage)
        SelectButton(text = "문자 검사", onClick = onText)
    }
}

@Composable
private fun SelectButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp) // 어르신도 누르기 쉬운 큰 버튼
    ) {
        Text(text, fontSize = 18.sp)
    }
}
