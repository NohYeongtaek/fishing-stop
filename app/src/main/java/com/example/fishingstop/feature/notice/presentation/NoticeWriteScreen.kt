package com.example.fishingstop.feature.notice.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.AppScaffold
import com.example.fishingstop.core.ui.components.AppTextField
import com.example.fishingstop.core.ui.components.AppTopBar
import com.example.fishingstop.core.ui.components.PrimaryButton
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 공지 작성 화면(관리자 전용). 설정의 숨은 PIN 게이트를 통과해야 진입한다.
 * 제목/본문 입력 → Firestore 등록 → 성공 시 목록으로 복귀.
 *
 * @param onDone 등록 성공 후 돌아가기
 * @param onBack 취소/뒤로
 */
@Composable
fun NoticeWriteScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: NoticeWriteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val s = state) {
            is NoticeWriteUiState.Done -> {
                Toast.makeText(context, "공지가 등록되었어요.", Toast.LENGTH_SHORT).show()
                onDone()
            }
            is NoticeWriteUiState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                viewModel.consumeError()
            }
            else -> Unit
        }
    }

    AppScaffold(
        topBar = { AppTopBar(title = "공지 작성", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.spacing.screenX, vertical = AppTheme.spacing.cardPad),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stackGap)
        ) {
            Text(
                "새 공지를 작성합니다. 등록하면 모든 사용자에게 노출됩니다.",
                style = AppTheme.type.subtitle,
                color = colors.textSecondary
            )
            AppTextField(
                value = title,
                onValueChange = { title = it },
                label = "제목",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AppTextField(
                value = body,
                onValueChange = { body = it },
                label = "내용",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
            )
            PrimaryButton(
                text = if (state is NoticeWriteUiState.Submitting) "등록 중…" else "등록하기",
                enabled = state !is NoticeWriteUiState.Submitting,
                onClick = {
                    if (title.isBlank() || body.isBlank()) {
                        Toast.makeText(context, "제목과 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.submit(title, body)
                    }
                }
            )
        }
    }
}
