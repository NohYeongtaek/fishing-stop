package com.example.fishingstop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = OnEmeraldContainerLight,
    secondary = EmeraldLight,
    onSecondary = OnEmeraldContainerLight,
    tertiary = Pink80,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = OnEmeraldContainerDark,
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = OnEmeraldContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    secondary = Emerald,
    onSecondary = Color.White,
    tertiary = Pink40,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = OnEmeraldContainerLight,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = OnEmeraldContainerLight

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun FishingstopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 브랜드 포인트 컬러로 톤을 통일하기 위해 기본값은 끈다(Android 12+ 배경화면 기반 동적 색상 미사용).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}