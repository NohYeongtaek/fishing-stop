package com.rocketdan24.fishingstop.feature.inspect.presentation.check

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import com.rocketdan24.fishingstop.core.ui.components.SecondaryButton
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 이미지 검사 화면(FO_02_03).
 *
 * 포토피커로 캡처 이미지를 선택 → 온디바이스 OCR → "추출 텍스트 미리보기"에서
 * 사용자가 확인·수정 → 검사. (기획 확정: 미리보기 단계 필수)
 * 이미지는 기기 밖으로 전송되지 않으며, 추출된 텍스트만 분석에 사용된다.
 */
@Composable
fun ImageCheckScreen(
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ImageCheckViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    // 시스템 사진 선택기(저장소 권한 불필요)
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onImagePicked(uri) }

    // 오류는 토스트로 알리고 상태를 소비한다.
    LaunchedEffect(state) {
        (state as? ImageCheckUiState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            viewModel.consumeError()
        }
    }

    AppScaffold(
        topBar = { AppTopBar(title = "이미지 검사", onBack = onBack) }
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
            when (val s = state) {
                is ImageCheckUiState.Idle, is ImageCheckUiState.Error -> {
                    Text(
                        "의심되는 카톡·문자 캡처 이미지를 올려주세요.",
                        style = AppTheme.type.subtitle,
                        color = AppTheme.colors.textSecondary
                    )
                    PrimaryButton(
                        text = "이미지 파일 업로드",
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }

                is ImageCheckUiState.Processing -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = AppTheme.colors.greenPrimary)
                        Text(
                            "이미지에서 글자를 읽고 있어요…",
                            style = AppTheme.type.subtitle,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                is ImageCheckUiState.Preview -> {
                    Text(
                        "이미지에서 읽어낸 내용이에요. 잘못 읽힌 부분이 있으면 고친 뒤 검사해 주세요.",
                        style = AppTheme.type.subtitle,
                        color = AppTheme.colors.textSecondary
                    )
                    AppTextField(
                        value = s.text,
                        onValueChange = viewModel::updatePreviewText,
                        label = "추출된 텍스트",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp)
                    )
                    PrimaryButton(
                        text = "이미지 검사하기",
                        onClick = {
                            val text = s.text.trim()
                            if (text.isEmpty()) {
                                Toast.makeText(context, "검사할 내용이 없습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                onSubmit(text)
                            }
                        }
                    )
                    SecondaryButton(
                        text = "다른 이미지 선택",
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
            }
        }
    }
}
