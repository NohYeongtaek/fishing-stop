package com.example.fishingstop.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 앱 디자인 토큰 접근자.
 *
 * 화면/컴포넌트는 `AppTheme.colors.greenPrimary`, `AppTheme.type.h1`,
 * `AppTheme.sizes.btnPrimaryH`, `AppTheme.spacing.screenX`, `AppTheme.elder` 처럼 읽는다.
 * 값은 [FishingstopTheme] 이 (다크, 어르신)에 맞춰 Provide 한다.
 */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
    val type: AppType
        @Composable @ReadOnlyComposable get() = LocalAppType.current
    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = LocalAppShapes.current
    val sizes: AppSizes
        @Composable @ReadOnlyComposable get() = LocalAppSizes.current
    val spacing: AppSpacing
        @Composable @ReadOnlyComposable get() = LocalAppSpacing.current
    val elder: Boolean
        @Composable @ReadOnlyComposable get() = LocalElderMode.current
}

val LocalAppColors = staticCompositionLocalOf { NormalLightColors }
val LocalAppType = staticCompositionLocalOf { NormalType }
val LocalAppShapes = staticCompositionLocalOf { NormalShapes }
val LocalAppSizes = staticCompositionLocalOf { NormalSizes }
val LocalAppSpacing = staticCompositionLocalOf { NormalSpacing }
val LocalElderMode = staticCompositionLocalOf { false }
