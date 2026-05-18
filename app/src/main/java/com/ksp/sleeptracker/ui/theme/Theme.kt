package com.ksp.sleeptracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SlumberDarkColors = darkColorScheme(
    primary = AccentIndigo,
    onPrimary = NightNavy,
    primaryContainer = MidnightIndigo,
    onPrimaryContainer = TextPrimary,
    secondary = AccentGreen,
    onSecondary = NightNavy,
    tertiary = AccentAmber,
    onTertiary = NightNavy,
    background = NightNavy,
    onBackground = TextPrimary,
    surface = NightNavy,
    onSurface = TextPrimary,
    surfaceVariant = DeepSlate,
    onSurfaceVariant = TextMuted,
    surfaceContainer = DeepSlate,
    surfaceContainerHigh = MidnightIndigo,
    surfaceContainerHighest = MidnightIndigo,
    outline = Divider,
    outlineVariant = Divider,
    error = AccentRose,
    onError = TextPrimary
)

private val SlumberLightColors = lightColorScheme(
    primary = SeedColor,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E3F8),
    onPrimaryContainer = Color(0xFF1B1D45),
    secondary = AccentGreen,
    tertiary = AccentAmber,
    background = Color(0xFFF8F8FD),
    onBackground = Color(0xFF15172A),
    surface = Color.White,
    onSurface = Color(0xFF15172A),
    surfaceVariant = Color(0xFFE8E9F4),
    onSurfaceVariant = Color(0xFF5A6090)
)

@Composable
fun SleepTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SlumberDarkColors
        else -> SlumberLightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
