package com.example.fishingstop.feature.inspect.presentation.result

import androidx.compose.ui.graphics.Color
import com.example.fishingstop.core.util.RiskLevel

/**
 * 위험 등급의 화면 표시 속성(색/제목/설명).
 * 위험 색상은 의미론적(초록/노랑/빨강)이라 Material 색상 스킴과 별개로 명시적으로 정의한다.
 * 색은 다크/라이트 모두에서 대비가 확보되는 톤으로 선택했다(취약 계층 접근성 고려).
 */
data class RiskLevelUi(
    val title: String,
    val message: String,
    val container: Color,
    val onContainer: Color
)

fun RiskLevel.toUi(): RiskLevelUi = when (this) {
    RiskLevel.SAFE -> RiskLevelUi(
        title = "안전",
        message = "안전한 메시지로 분석되었습니다.",
        container = Color(0xFF2E7D32),   // green 800
        onContainer = Color.White
    )
    RiskLevel.WARNING -> RiskLevelUi(
        title = "주의",
        message = "의심스러운 정황이 있어요. 아래 항목을 확인하세요.",
        container = Color(0xFFF9A825),   // amber 800
        onContainer = Color(0xFF1B1B1B)
    )
    RiskLevel.DANGER -> RiskLevelUi(
        title = "위험",
        message = "피싱 위험이 높습니다. 링크 클릭·정보 입력·송금을 멈추세요.",
        container = Color(0xFFC62828),   // red 800
        onContainer = Color.White
    )
}
