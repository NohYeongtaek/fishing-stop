package com.rocketdan24.fishingstop.feature.notice.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rocketdan24.fishingstop.core.ui.components.AppScaffold
import com.rocketdan24.fishingstop.core.ui.components.AppTextField
import com.rocketdan24.fishingstop.core.ui.components.AppTopBar
import com.rocketdan24.fishingstop.core.ui.components.PrimaryButton
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 공지 작성/수정 화면(관리자 전용).
 * noticeId 유무에 따라 "공지 작성"/"공지 수정"으로 동작한다(제목·버튼 문구가 바뀜).
 * 저장 성공 시 목록으로 복귀한다.
 *
 * @param onDone 저장 성공 후 돌아가기(호출측에서 목록 새로고침을 트리거)
 * @param onBack 취소/뒤로
 */
@Composable
fun NoticeWriteScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: NoticeWriteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val isEdit = state.isEdit

    LaunchedEffect(state.done) {
        if (state.done) {
            Toast.makeText(context, if (isEdit) "공지가 수정되었어요." else "공지가 등록되었어요.", Toast.LENGTH_SHORT).show()
            onDone()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeError()
        }
    }

    AppScaffold(
        topBar = { AppTopBar(title = if (isEdit) "공지 수정" else "공지 작성", onBack = onBack) }
    ) { innerPadding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                CircularProgressIndicator(color = colors.greenPrimary)
            }
            return@AppScaffold
        }

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
                if (isEdit) "공지를 수정합니다. 저장하면 변경 내용이 모든 사용자에게 반영됩니다."
                else "새 공지를 작성합니다. 등록하면 모든 사용자에게 노출됩니다.",
                style = AppTheme.type.subtitle,
                color = colors.textSecondary
            )
            AppTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "제목",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AppTextField(
                value = state.body,
                onValueChange = viewModel::onBodyChange,
                label = "내용",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
            )
            PrimaryButton(
                text = when {
                    state.submitting && isEdit -> "수정 중…"
                    state.submitting -> "등록 중…"
                    isEdit -> "공지 수정하기"
                    else -> "공지 작성하기"
                },
                enabled = !state.submitting,
                onClick = viewModel::submit
            )
        }
    }
}
