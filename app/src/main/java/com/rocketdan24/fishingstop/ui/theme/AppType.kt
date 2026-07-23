package com.rocketdan24.fishingstop.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 앱 타이포 토큰(디자인 스펙 1-2). 역할별 TextStyle을 일반/어르신 2세트로 제공한다.
 *
 * 폰트: 스펙은 Noto Sans KR. 지금은 [AppFontFamily] = FontFamily.Default (시스템 한글 글꼴)이며,
 * res/font 에 Noto Sans KR(Regular/Medium/Bold/Black)을 추가하면 아래 한 곳만 바꾸면 된다.
 * 크기는 sp이지만, [FishingstopTheme]에서 fontScale을 1로 고정해 시스템 글꼴 배율의 영향은 받지 않는다.
 * 어르신 모드는 그 위에 얹는 앱 자체 확대다.
 */
val AppFontFamily: FontFamily = FontFamily.Default // TODO: Noto Sans KR 등록 시 교체

data class AppType(
    val h1: TextStyle,
    val subtitle: TextStyle,
    val body: TextStyle,
    val cardLabel: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,
    val scoreNum: TextStyle,
    val hotlineNum: TextStyle,
    val tabLabel: TextStyle,
    val chevron: TextStyle
)

private fun style(
    size: Int,
    weight: FontWeight,
    lineHeightEm: Float = 1.4f
) = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (lineHeightEm).em
)

val NormalType = AppType(
    h1 = style(24, FontWeight.Black, 1.3f),
    subtitle = style(17, FontWeight.Normal, 1.55f),
    body = style(16, FontWeight.Normal, 1.6f),
    cardLabel = style(15, FontWeight.Bold, 1.4f),
    caption = style(14, FontWeight.Normal, 1.55f),
    button = style(19, FontWeight.Bold, 1.2f),
    scoreNum = style(34, FontWeight.Black, 1.0f),
    hotlineNum = style(38, FontWeight.Black, 1.0f),
    tabLabel = style(12, FontWeight.Medium, 1.2f),
    chevron = style(20, FontWeight.Normal, 1.0f)
)

val ElderType = AppType(
    h1 = style(29, FontWeight.Black, 1.3f),
    subtitle = style(20, FontWeight.Normal, 1.55f),
    body = style(20, FontWeight.Normal, 1.6f),
    cardLabel = style(18, FontWeight.Black, 1.4f),
    caption = style(17, FontWeight.Normal, 1.55f),
    button = style(23, FontWeight.Black, 1.2f),
    scoreNum = style(40, FontWeight.Black, 1.0f),
    hotlineNum = style(52, FontWeight.Black, 1.0f),
    tabLabel = style(14, FontWeight.Bold, 1.2f),
    chevron = style(26, FontWeight.Normal, 1.0f)
)

fun appTypeFor(elder: Boolean): AppType = if (elder) ElderType else NormalType
