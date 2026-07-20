package com.example.fishingstop.feature.inspect.presentation.check

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.fishingstop.core.ui.components.AppScaffold
import com.example.fishingstop.core.ui.components.AppTextField
import com.example.fishingstop.core.ui.components.AppTopBar
import com.example.fishingstop.core.ui.components.PrimaryButton
import com.example.fishingstop.core.ui.components.WarnBox
import com.example.fishingstop.ui.theme.AppTheme

/**
 * 링크 검사 화면(FO_02_02).
 * 복사해 둔 URL을 붙여넣으면 휴리스틱 + AI 병합 검사로 넘어간다.
 * 링크를 자동으로 열지 않는다(안전 원칙).
 */
@Composable
fun LinkCheckScreen(
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var url by remember { mutableStateOf("") }

    AppScaffold(
        topBar = { AppTopBar(title = "링크 검사", onBack = onBack) }
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
                "의심 가는 링크(URL)를 복사한 뒤 붙여넣어 주세요.",
                style = AppTheme.type.subtitle,
                color = AppTheme.colors.textSecondary
            )
            AppTextField(
                value = url,
                onValueChange = { url = it },
                label = "링크(URL)",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WarnBox {
                Text(
                    "안전을 위해 링크를 열지 않고 주소만 검사합니다.",
                    style = AppTheme.type.caption,
                    color = AppTheme.colors.warnText
                )
            }
            PrimaryButton(
                text = "링크 검사하기",
                onClick = {
                    val text = url.trim()
                    if (text.isEmpty()) {
                        Toast.makeText(context, "검사할 링크를 붙여넣어 주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        onSubmit(text)
                    }
                }
            )
        }
    }
}
