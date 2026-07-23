package com.rocketdan24.fishingstop.feature.inspect.presentation.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.rocketdan24.fishingstop.core.util.RiskLevel
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 위험 등급의 화면 표시 속성(색/제목/설명/이모지).
 *
 * 위험 색상은 의미론적(초록/노랑/빨강)이라 디자인 토큰(AppColors)에서 등급별로 매핑한다.
 * 라이트/다크·어르신 4조합 모두 토큰이 알아서 대비를 확보한다.
 *  - gradient/onGradient : 결과 히어로(가장 크게 강조) 배경/글씨
 *  - chipBg/chipText     : 뱃지·작은 표기
 *  - emoji               : 어르신 모드 결과 화면의 큰 상태 표시(Phase 3)
 */
data class RiskLevelUi(
    val title: String,
    val message: String,
    val gradient: List<Color>,
    val onGradient: Color,
    val chipBg: Color,
    val chipText: Color,
    val emoji: String
)

/** 토큰 기반 등급 표시 속성. @Composable 이라 현재 테마(라이트/다크·어르신)를 반영한다. */
@Composable
@ReadOnlyComposable
fun riskUi(level: RiskLevel): RiskLevelUi {
    val c = AppTheme.colors
    return when (level) {
        RiskLevel.SAFE -> RiskLevelUi(
            title = "안전",
            message = "안전한 메시지로 분석되었습니다.",
            gradient = c.greenGradient,
            onGradient = c.onGreen,
            chipBg = c.greenChipBg,
            chipText = c.greenChipText,
            emoji = "😊"
        )
        RiskLevel.WARNING -> RiskLevelUi(
            title = "주의",
            message = "의심스러운 정황이 있어요. 아래 항목을 확인하세요.",
            gradient = c.warnGradient,
            onGradient = c.onWarn,
            chipBg = c.warnBg,
            chipText = c.warnText,
            emoji = "⚠️"
        )
        RiskLevel.DANGER -> RiskLevelUi(
            title = "위험",
            message = "피싱 위험이 높습니다. 링크 클릭·정보 입력·송금을 멈추세요.",
            gradient = c.dangerGradient,
            onGradient = c.onDanger,
            chipBg = c.dangerChipBg,
            chipText = c.dangerChipText,
            emoji = "🚨"
        )
    }
}
