package com.example.gotouchgrass.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ===========================================
// GO TOUCH GRASS - THEME CONFIGURATION
// ===========================================
// Design Philosophy:
// 1. Nature-inspired colors reinforce "touch grass" outdoor messaging
// 2. Golden accents for gamification feel rewarding & valuable
// 3. Warm neutrals create organic, inviting atmosphere
// 4. High contrast for outdoor readability
// 5. Rounded corners (16dp default) for friendly, approachable feel

private val LightColorScheme = lightColorScheme(
    // Primary - Forest Green (main actions, FAB, nav selection)
    primary = ForestGreen,
    onPrimary = WarmWhite,
    primaryContainer = Color(0xFFB8D4BE),      // Light green container
    onPrimaryContainer = ForestGreenDark,

    // Secondary - Used for less prominent elements
    secondary = Color(0xFF4A6B52),              // Muted forest green
    onSecondary = WarmWhite,
    secondaryContainer = SandLight,
    onSecondaryContainer = TextPrimary,

    // Tertiary - Golden Yellow (XP, coins, achievements)
    tertiary = GoldenYellow,
    onTertiary = TextPrimary,
    tertiaryContainer = Color(0xFFFFF0C2),     // Light gold container
    onTertiaryContainer = GoldenYellowDark,

    // Background & Surface
    background = WarmCream,
    onBackground = TextPrimary,
    surface = WarmWhite,
    onSurface = TextPrimary,
    surfaceVariant = SandLight,
    onSurfaceVariant = TextSecondary,

    // Other
    outline = SandBorder,
    outlineVariant = SandMuted,
    error = Error,
    onError = WarmWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    // Primary - Lighter green for dark mode visibility
    primary = ForestGreenLight,
    onPrimary = DarkForest,
    primaryContainer = ForestGreen,
    onPrimaryContainer = Color(0xFFB8D4BE),

    // Secondary
    secondary = Color(0xFF8FB996),
    onSecondary = DarkForest,
    secondaryContainer = DarkForestSecondary,
    onSecondaryContainer = TextOnDark,

    // Tertiary - Golden Yellow (stays vibrant in dark mode)
    tertiary = GoldenYellow,
    onTertiary = DarkForest,
    tertiaryContainer = GoldenYellowDark,
    onTertiaryContainer = Color(0xFFFFF0C2),

    // Background & Surface
    background = DarkForest,
    onBackground = TextOnDark,
    surface = DarkForestCard,
    onSurface = TextOnDark,
    surfaceVariant = DarkForestSecondary,
    onSurfaceVariant = TextOnDarkSecondary,

    // Other
    outline = DarkForestBorder,
    outlineVariant = DarkForestMuted,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun GoTouchGrassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to maintain brand consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // We intentionally disable dynamic color to keep our nature-inspired brand
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Update system bar colors to match theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar color matches background
            window.statusBarColor = colorScheme.background.toArgb()
            // Navigation bar color
            window.navigationBarColor = colorScheme.surface.toArgb()
            // Set light/dark icons based on theme
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// ===========================================
// DESIGN TOKENS & DIMENSIONS
// ===========================================
// Using these for consistent spacing and sizing throughout the app

object GoTouchGrassDimens {
    // Corner Radius - Rounded for friendly, approachable feel
    val RadiusSmall = 8.dp       // Small chips, tags
    val RadiusMedium = 12.dp     // Buttons, inputs
    val RadiusLarge = 16.dp      // Cards, sheets (DEFAULT)
    val RadiusXLarge = 24.dp     // Bottom sheets, modals
    val RadiusFull = 100.dp      // Circular elements (avatars, FAB)

    // Spacing
    val SpacingXs = 4.dp
    val SpacingSm = 8.dp
    val SpacingMd = 16.dp
    val SpacingLg = 24.dp
    val SpacingXl = 32.dp
    val SpacingXxl = 48.dp

    // Touch Targets - Minimum 48dp for accessibility
    val TouchTargetMin = 48.dp
    val ButtonHeight = 52.dp
    val IconButtonSize = 44.dp

    // Card Elevation
    val ElevationNone = 0.dp
    val ElevationLow = 2.dp
    val ElevationMedium = 4.dp
    val ElevationHigh = 8.dp
}
