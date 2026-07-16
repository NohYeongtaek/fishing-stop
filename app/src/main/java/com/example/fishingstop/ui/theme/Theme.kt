package com.example.fishingstop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * 앱 테마. (다크, 어르신) 조합에 맞춰 커스텀 디자인 토큰(AppColors/Type/Shapes/Sizes/Spacing)을
 * CompositionLocal로 Provide하고, Material3 컴포넌트가 브랜드 톤을 따르도록 ColorScheme도 매핑한다.
 *
 * 화면/위젯은 [AppTheme] 접근자로 토큰을 읽는다. 어르신 토글 시에도 컴포지션 구조가 바뀌지 않도록
 * 항상 동일하게 Provider로 감싼다(내비게이션 리셋 방지).
 */
@Composable
fun FishingstopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    elder: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = appColorsFor(dark = darkTheme, elder = elder)
    val type = appTypeFor(elder)
    val shapes = appShapesFor(elder)
    val sizes = appSizesFor(elder)
    val spacing = appSpacingFor(elder)

    // Material 베이스 컴포넌트가 브랜드색을 따르도록 ColorScheme을 토큰에서 만든다.
    val materialScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.greenPrimary,
            onPrimary = colors.onGreen,
            secondary = colors.greenPrimary,
            onSecondary = colors.onGreen,
            error = colors.dangerPrimary,
            onError = colors.onDanger,
            background = colors.pageBg,
            onBackground = colors.textPrimary,
            surface = colors.cardBg,
            onSurface = colors.textPrimary,
            onSurfaceVariant = colors.textSecondary,
            surfaceVariant = colors.greenTint,
            primaryContainer = colors.greenChipBg,
            onPrimaryContainer = colors.greenChipText,
            outline = colors.borderInput,
            outlineVariant = colors.borderDivider
        )
    } else {
        lightColorScheme(
            primary = colors.greenPrimary,
            onPrimary = colors.onGreen,
            secondary = colors.greenPrimary,
            onSecondary = colors.onGreen,
            error = colors.dangerPrimary,
            onError = colors.onDanger,
            background = colors.pageBg,
            onBackground = colors.textPrimary,
            surface = colors.cardBg,
            onSurface = colors.textPrimary,
            onSurfaceVariant = colors.textSecondary,
            surfaceVariant = colors.greenTint,
            primaryContainer = colors.greenChipBg,
            onPrimaryContainer = colors.greenChipText,
            outline = colors.borderInput,
            outlineVariant = colors.borderDivider
        )
    }

    // 버튼·카드 등 크기 토큰(AppSizes)은 dp로 고정돼 있어, 시스템 글꼴 배율을 그대로 따르면
    // 글자가 넘치거나 레이아웃이 깨질 수 있다. fontScale을 1로 고정해 시스템 설정과 무관하게 만든다.
    val fixedFontScaleDensity = Density(
        density = LocalDensity.current.density,
        fontScale = 1f
    )

    CompositionLocalProvider(
        LocalDensity provides fixedFontScaleDensity,
        LocalAppColors provides colors,
        LocalAppType provides type,
        LocalAppShapes provides shapes,
        LocalAppSizes provides sizes,
        LocalAppSpacing provides spacing,
        LocalElderMode provides elder
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
