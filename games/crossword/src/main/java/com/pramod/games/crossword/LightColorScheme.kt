package com.pramod.games.crossword

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF4CAF50), // green_500
        onPrimary = Color.White,
        primaryContainer = Color(0xFFA5D6A7), // green_200
        onPrimaryContainer = Color(0xFF002107),
        secondaryContainer = Color(0xFFF5F3F3), // grey_extra_light
        onSecondaryContainer = Color(0xFF1A1C1E),
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xB3000000), // textColor_highEmphasis
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xB3000000), // textColor_highEmphasis
        surfaceVariant = Color(0xFFE6E6E6), // grey_light
        onSurfaceVariant = Color(0xFF4C4B4B), // grey_very_extra_dark
        outline = Color(0xFFACABAB), // grey_dark
        outlineVariant = Color(0xFFE6E6E6), // grey_light
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF66BB6A), // green_400
        onPrimary = Color(0xFF00390A),
        primaryContainer = Color(0xFF005313),
        onPrimaryContainer = Color(0xFFA5D6A7),
        secondaryContainer = Color(0xFF282828), // home_past_word_color in dark
        onSecondaryContainer = Color(0xFFDEFFFFFF),
        background = Color(0xFF202020), // windowBackgroundColor in dark
        onBackground = Color(0xDEFFFFFF), // dark_textColor_highEmphasis
        surface = Color(0xFF181818), // setting_card_color in dark
        onSurface = Color(0xDEFFFFFF), // dark_textColor_highEmphasis
        surfaceVariant = Color(0xFF303030), // splash_wave_color in dark
        onSurfaceVariant = Color(0xFFACABAB),
        outline = Color(0xFF5E5D5D),
        outlineVariant = Color(0xFF3E3D3D), // stroke_about_cards in dark
    )

@Composable
fun CrosswordTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Since you use enableEdgeToEdge(), we only need to manage icon contrast
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
