package com.rocketdan24.fishingstop.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 앱 커스텀 색상 토큰(디자인 스펙 1-1).
 *
 * Material ColorScheme와 별개로, 브랜드/등급/텍스트/테두리 등 스펙 전용 색을 담는다.
 * `(다크, 어르신)` 4조합으로 선택된다: NormalLight / ElderLight / NormalDark / ElderDark.
 * 라이트 값은 스펙 그대로, 다크 값은 제안값(온디바이스 튜닝 대상)이다.
 *
 * borderCard 가 Color.Transparent 면 카드 테두리 없음(그림자만) — 두께는 AppSizes.borderCard 로 제어.
 */
data class AppColors(
    val isDark: Boolean,
    // 배경
    val pageBg: Color,
    val cardBg: Color,
    val navbarBg: Color,
    // 그린(안전/주요)
    val greenPrimary: Color,
    val greenLabel: Color,
    val greenTint: Color,
    val greenChipBg: Color,
    val greenChipText: Color,
    val greenGradient: List<Color>,
    val onGreen: Color,
    // 데인저(위험)
    val dangerPrimary: Color,
    val dangerChipBg: Color,
    val dangerChipText: Color,
    val dangerGradient: List<Color>,
    val onDanger: Color,
    // 주의(warn)
    val warnBg: Color,
    val warnBorder: Color,
    val warnText: Color,
    val warnGradient: List<Color>,
    val onWarn: Color,
    // 텍스트
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textPlaceholder: Color,
    // 테두리/구분선
    val borderInput: Color,
    val borderNavbar: Color,
    val borderDivider: Color,
    val borderCard: Color,
    // 기타
    val star: Color,
    val highlightBg: Color
)

// ── 일반 · 라이트 (스펙 그대로) ──
val NormalLightColors = AppColors(
    isDark = false,
    pageBg = Color(0xFFF7F4EC),
    cardBg = Color(0xFFFFFFFF),
    navbarBg = Color(0xFFFFFFFF),
    greenPrimary = Color(0xFF2F7A4A),
    greenLabel = Color(0xFF7A9471),
    greenTint = Color(0xFFEAF3EC),
    greenChipBg = Color(0xFFEAF3EC),
    greenChipText = Color(0xFF2F7A4A),
    greenGradient = listOf(Color(0xFF3E8A56), Color(0xFF2F7A4A)),
    onGreen = Color(0xFFFFFFFF),
    dangerPrimary = Color(0xFFC24E44),
    dangerChipBg = Color(0xFFFBEAE7),
    dangerChipText = Color(0xFFC24E44),
    dangerGradient = listOf(Color(0xFFD65A50), Color(0xFFC24E44)),
    onDanger = Color(0xFFFFFFFF),
    warnBg = Color(0xFFFDF6E7),
    warnBorder = Color(0xFFEBD9A8),
    warnText = Color(0xFF9A6B12),
    warnGradient = listOf(Color(0xFFF2C14E), Color(0xFFE0A233)),
    onWarn = Color(0xFF3D2B08),
    textPrimary = Color(0xFF2E3A2B),
    textSecondary = Color(0xFF5C564A),
    textTertiary = Color(0xFF8A8271),
    textPlaceholder = Color(0xFFA39B87),
    borderInput = Color(0xFFD8D2C2),
    borderNavbar = Color(0xFFEAE5D8),
    borderDivider = Color(0xFFF0EDE2),
    borderCard = Color.Transparent,
    star = Color(0xFFE4B93C),
    highlightBg = Color(0xFFF3EBCE)
)

// ── 어르신 · 라이트 (대비 강화, 스펙) ──
val ElderLightColors = NormalLightColors.copy(
    greenPrimary = Color(0xFF276B3F),
    greenChipBg = Color(0xFFE4F0E6),
    greenChipText = Color(0xFF276B3F),
    greenGradient = listOf(Color(0xFF40824F), Color(0xFF276B3F)),
    dangerPrimary = Color(0xFFB23A31),
    dangerChipText = Color(0xFFB23A31),
    dangerGradient = listOf(Color(0xFFCE5046), Color(0xFFB23A31)),
    textPrimary = Color(0xFF1F2A1D),
    textSecondary = Color(0xFF3A362C),
    textTertiary = Color(0xFF4A453B),
    textPlaceholder = Color(0xFF8A8271),
    borderInput = Color(0xFFB7AE94),
    borderNavbar = Color(0xFFE0D9C6),
    borderDivider = Color(0xFFE0D9C6),
    borderCard = Color(0xFFE0D9C6)
)

// ── 일반 · 다크 (제안값) ──
val NormalDarkColors = AppColors(
    isDark = true,
    pageBg = Color(0xFF121410),
    cardBg = Color(0xFF1E211B),
    navbarBg = Color(0xFF1A1D17),
    greenPrimary = Color(0xFF5FB07A),
    greenLabel = Color(0xFF9DB394),
    greenTint = Color(0xFF1F3327),
    greenChipBg = Color(0xFF1F3327),
    greenChipText = Color(0xFF8FD3A5),
    greenGradient = listOf(Color(0xFF3E8A56), Color(0xFF2F7A4A)),
    onGreen = Color(0xFF06210F),
    dangerPrimary = Color(0xFFE07A70),
    dangerChipBg = Color(0xFF3A2320),
    dangerChipText = Color(0xFFF0A79E),
    dangerGradient = listOf(Color(0xFFD65A50), Color(0xFFC24E44)),
    onDanger = Color(0xFF2A0D0A),
    warnBg = Color(0xFF332C1A),
    warnBorder = Color(0xFF5C4E28),
    warnText = Color(0xFFE8C87A),
    warnGradient = listOf(Color(0xFF7A6529), Color(0xFF5C4E28)),
    onWarn = Color(0xFFFBEFC9),
    textPrimary = Color(0xFFECEFE8),
    textSecondary = Color(0xFFC2C6BB),
    textTertiary = Color(0xFF8F978A),
    textPlaceholder = Color(0xFF6E756A),
    borderInput = Color(0xFF3A3E34),
    borderNavbar = Color(0xFF2A2E24),
    borderDivider = Color(0xFF262A20),
    borderCard = Color.Transparent,
    star = Color(0xFFE4B93C),
    highlightBg = Color(0xFF3A3320)
)

// ── 어르신 · 다크 (제안값, 더 밝고 대비 강함) ──
val ElderDarkColors = NormalDarkColors.copy(
    greenPrimary = Color(0xFF6FC08A),
    greenTint = Color(0xFF24402E),
    greenChipBg = Color(0xFF24402E),
    dangerPrimary = Color(0xFFEC8C82),
    warnBg = Color(0xFF3A3320),
    warnBorder = Color(0xFF6B5A2E),
    warnText = Color(0xFFF0D289),
    warnGradient = listOf(Color(0xFF8A7331), Color(0xFF6B5A2E)),
    onWarn = Color(0xFFFDF6E0),
    textPrimary = Color(0xFFF5F7F0),
    textSecondary = Color(0xFFD2D6CB),
    textTertiary = Color(0xFF9AA290),
    textPlaceholder = Color(0xFF7A8175),
    borderInput = Color(0xFF4A4E42),
    borderNavbar = Color(0xFF33372E),
    borderDivider = Color(0xFF33372E),
    borderCard = Color(0xFF3A3E34)
)

/** (다크, 어르신) 조합으로 색상 세트를 고른다. */
fun appColorsFor(dark: Boolean, elder: Boolean): AppColors = when {
    dark && elder -> ElderDarkColors
    dark -> NormalDarkColors
    elder -> ElderLightColors
    else -> NormalLightColors
}
