package com.example.fishingstop.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 간격 토큰(디자인 스펙 1-5). */
data class AppSpacing(
    val screenX: Dp,
    val cardPad: Dp,
    val stackGap: Dp,
    val listGap: Dp,
    val navbarTop: Dp,
    val navbarSide: Dp,
    val navbarBottom: Dp
)

val NormalSpacing = AppSpacing(
    screenX = 24.dp,
    cardPad = 20.dp,
    stackGap = 14.dp,
    listGap = 12.dp,
    navbarTop = 10.dp,
    navbarSide = 8.dp,
    navbarBottom = 18.dp
)

val ElderSpacing = AppSpacing(
    screenX = 24.dp,
    cardPad = 22.dp,
    stackGap = 16.dp,
    listGap = 14.dp,
    navbarTop = 12.dp,
    navbarSide = 6.dp,
    navbarBottom = 20.dp
)

fun appSpacingFor(elder: Boolean): AppSpacing = if (elder) ElderSpacing else NormalSpacing
