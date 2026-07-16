package com.example.fishingstop.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.fishingstop.ui.theme.AppTheme

/**
 * "AI 분석 결과는 참고 정보이며 법적 증거가 아니다"라는 면책 문구.
 *
 * 스펙의 안전·개인정보 원칙에 따라 분석 결과가 나오는 화면 하단 등에 반복 노출한다.
 * 문구를 한 곳에서 관리하기 위해 공통 컴포넌트로 분리했다.
 */
@Composable
fun DisclaimerText(modifier: Modifier = Modifier) {
    Text(
        text = "본 분석 결과는 AI 기반 참고 정보이며, 법적 효력이나 확정 판정이 아닙니다.",
        style = AppTheme.type.caption,
        color = AppTheme.colors.textTertiary,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}
