package com.example.fishingstop.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** 형태(반경) 토큰(디자인 스펙 1-3). */
data class AppShapes(
    val card: Shape,
    val button: Shape,
    val iconBox: Shape,
    val pill: Shape,
    val input: Shape
)

val NormalShapes = AppShapes(
    card = RoundedCornerShape(20.dp),
    button = RoundedCornerShape(18.dp),
    iconBox = RoundedCornerShape(16.dp),
    pill = RoundedCornerShape(999.dp),
    input = RoundedCornerShape(18.dp)
)

val ElderShapes = AppShapes(
    card = RoundedCornerShape(22.dp),
    button = RoundedCornerShape(20.dp),
    iconBox = RoundedCornerShape(18.dp),
    pill = RoundedCornerShape(999.dp),
    input = RoundedCornerShape(20.dp)
)

fun appShapesFor(elder: Boolean): AppShapes = if (elder) ElderShapes else NormalShapes
