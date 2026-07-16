package com.example.fishingstop.feature.inspect.presentation.check

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fishingstop.core.ui.components.AppScaffold
import com.example.fishingstop.core.ui.components.AppTextField
import com.example.fishingstop.core.ui.components.AppTopBar
import com.example.fishingstop.core.ui.components.PrimaryButton
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 문자 검사 화면(FO_02_04).
 * 의심 문자를 붙여넣어 검사한다. 공유(ACTION_SEND)로 진입하면 [prefill] 이 자동 입력되어
 * 사용자가 내용을 확인·수정한 뒤 검사할 수 있다.
 *
 * @param prefill  공유로 전달된 텍스트(없으면 null)
 * @param onSubmit 검사 실행(분석 화면으로 이동)
 * @param onBack   뒤로
 */
@Composable
fun TextCheckScreen(
    prefill: String?,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf(prefill.orEmpty()) }

    AppScaffold(
        topBar = { AppTopBar(title = "문자 검사", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.spacing.screenX, vertical = AppTheme.spacing.cardPad),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stackGap)
        ) {
            Text(
                "의심 가는 문자를 복사한 뒤 붙여넣어 주세요.",
                style = AppTheme.type.subtitle,
                color = AppTheme.colors.textSecondary
            )
            AppTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = "문자 내용",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp)
            )
            PrimaryButton(
                text = "문자 검사하기",
                onClick = {
                    val text = inputText.trim()
                    if (text.isEmpty()) {
                        Toast.makeText(context, "검사할 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        onSubmit(text)
                    }
                }
            )
        }
    }
}
