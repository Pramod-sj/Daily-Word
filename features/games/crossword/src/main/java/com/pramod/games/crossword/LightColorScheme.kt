package com.pramod.games.crossword

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // ── Primary ──
    primary = Color(0xFF4CAF50),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA5D6A7),
    onPrimaryContainer = Color(0xFF002107),
    inversePrimary = Color(0xFF66BB6A), // Used for Snackbars

    // ── Secondary ──
    secondary = Color(0xFF52634F), // M3 generated secondary for your green
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E8CF), // Replaced your grey with a tinted secondary
    onSecondaryContainer = Color(0xFF111F0F),

    // ── Tertiary (Accent) ──
    tertiary = Color(0xFF38656A), // A complementary teal
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBF0),
    onTertiaryContainer = Color(0xFF002022),

    // ── Error ──
    error = Color(0xFFBA1A1A), // M3 Standard Light Error
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // ── Background & Surface ──
    background = Color(0xFFFCFDF6), // Tinted white
    onBackground = Color(0xFF1A1C18), // ❌ Removed Alpha: Solid dark grey
    surface = Color(0xFFFCFDF6),
    onSurface = Color(0xFF1A1C18), // ❌ Removed Alpha: Solid dark grey
    inverseSurface = Color(0xFF2F312C), // Used for Snackbars
    inverseOnSurface = Color(0xFFF1F1EB),

    // ── Surface Variants & Outlines ──
    surfaceVariant = Color(0xFFDFE4D8), // Slightly tinted grey
    onSurfaceVariant = Color(0xFF43483E),
    outline = Color(0xFF73796E),
    outlineVariant = Color(0xFFC3C8BC), // Used for subtle borders

    // ── Surface Containers (M3 Elevation) ──
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F8F1),
    surfaceContainer = Color(0xFFF0F3E8),
    surfaceContainerHigh = Color(0xFFEBEEDF),
    surfaceContainerHighest = Color(0xFFE5E9D7),
)

private val DarkColorScheme = darkColorScheme(
    // ── Primary ──
    primary = Color(0xFF66BB6A),
    onPrimary = Color(0xFF00390A),
    primaryContainer = Color(0xFF005313),
    onPrimaryContainer = Color(0xFFA5D6A7),
    inversePrimary = Color(0xFF4CAF50),

    // ── Secondary ──
    secondary = Color(0xFFB9CCB4),
    onSecondary = Color(0xFF253423),
    secondaryContainer = Color(0xFF3B4B39),
    onSecondaryContainer = Color(0xFFD5E8CF),

    // ── Tertiary (Accent) ──
    tertiary = Color(0xFFA0CFCF),
    onTertiary = Color(0xFF00373B),
    tertiaryContainer = Color(0xFF1E4D52),
    onTertiaryContainer = Color(0xFFBCEBF0),

    // ── Error ──
    error = Color(0xFFFFB4AB), // M3 Standard Dark Error
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // ── Background & Surface ──
    background = Color(0xFF121410), // True M3 Dark (almost black with green tint)
    onBackground = Color(0xFFE2E3DE), // ❌ Removed Alpha: Solid off-white
    surface = Color(0xFF121410),
    onSurface = Color(0xFFE2E3DE), // ❌ Removed Alpha: Solid off-white
    inverseSurface = Color(0xFFE2E3DE),
    inverseOnSurface = Color(0xFF2F312C),

    // ── Surface Variants & Outlines ──
    surfaceVariant = Color(0xFF43483E),
    onSurfaceVariant = Color(0xFFC3C8BC),
    outline = Color(0xFF8D9287),
    outlineVariant = Color(0xFF43483E),

    // ── Surface Containers (M3 Elevation) ──
    surfaceContainerLowest = Color(0xFF0D0F0B), // Very dark
    surfaceContainerLow = Color(0xFF1A1C18),
    surfaceContainer = Color(0xFF1E201C), // Standard card color
    surfaceContainerHigh = Color(0xFF282A26),
    surfaceContainerHighest = Color(0xFF333530), // Used for dialogs/bottom sheets
)

@Composable
internal fun CrosswordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
