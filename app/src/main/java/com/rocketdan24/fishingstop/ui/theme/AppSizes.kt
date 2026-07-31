package com.rocketdan24.fishingstop.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 크기 토큰(디자인 스펙 1-4). 스펙의 px는 dp로 매핑한다. */
data class AppSizes(
    val btnPrimaryH: Dp,
    val btnSecondaryH: Dp,
    val iconBox: Dp,
    val tabIcon: Dp,
    val homeCircle: Dp,
    val scoreCircle: Dp,
    val switchW: Dp,
    val switchH: Dp,
    val statusCircle: Dp,
    val borderCard: Dp,
    val borderInput: Dp,
    val analyzeRing: Dp
)

val NormalSizes = AppSizes(
    btnPrimaryH = 58.dp,
    btnSecondaryH = 54.dp,
    iconBox = 52.dp,
    tabIcon = 21.dp,
    homeCircle = 210.dp,
    scoreCircle = 74.dp,
    switchW = 54.dp,
    switchH = 32.dp,
    statusCircle = 84.dp,
    borderCard = 0.dp,
    borderInput = 2.dp,
    analyzeRing = 96.dp
)

val ElderSizes = AppSizes(
    btnPrimaryH = 70.dp,
    btnSecondaryH = 66.dp,
    iconBox = 62.dp,
    tabIcon = 26.dp,
    homeCircle = 240.dp,
    scoreCircle = 74.dp, // 어르신 결과는 이모지로 대체(Phase 3)
    switchW = 64.dp,
    switchH = 38.dp,
    statusCircle = 100.dp,
    borderCard = 2.dp,
    borderInput = 3.dp,
    analyzeRing = 116.dp
)

fun appSizesFor(elder: Boolean): AppSizes = if (elder) ElderSizes else NormalSizes
