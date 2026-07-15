package com.example.fishingstop.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 앱 전역에서 재사용하는 기본 강조 버튼.
 *
 * 취약 계층(어르신)도 누르기 쉽도록 최소 높이 56dp, 글자 18sp로 크게 잡았다.
 * 화면마다 버튼 스타일이 제각각이 되지 않도록 이 컴포넌트를 재사용한다.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Text(text = text, fontSize = 18.sp)
    }
}
