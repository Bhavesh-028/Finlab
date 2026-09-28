package com.example.ui.theme

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
    primary = FinCyanAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0E3A4B),
    onPrimaryContainer = Color(0xFFA5F3FC),
    secondary = FinGoldAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF452E03),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = FinGreenGain,
    onTertiary = Color.Black,
    background = FinDarkBackground,
    onBackground = FinTextPrimaryDark,
    surface = FinDarkSurface,
    onSurface = FinTextPrimaryDark,
    surfaceVariant = FinDarkSurfaceVariant,
    onSurfaceVariant = FinTextSecondaryDark,
    outline = FinDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFFB45309),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = FinLightBackground,
    onBackground = FinTextPrimaryLight,
    surface = FinLightSurface,
    onSurface = FinTextPrimaryLight,
    surfaceVariant = FinLightSurfaceVariant,
    onSurfaceVariant = FinTextSecondaryLight,
    outline = FinLightCardBorder
)

@Composable
fun FinPulseTheme(
    darkTheme: Boolean = true, // Default to sleek financial terminal dark mode
    dynamicColor: Boolean = false, // Keep branded financial styling consistent
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
