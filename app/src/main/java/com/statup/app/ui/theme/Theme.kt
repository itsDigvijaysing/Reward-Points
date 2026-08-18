package com.rewardpoints.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextOnAccent,
    primaryContainer = AccentPrimary.copy(alpha = 0.3f),
    onPrimaryContainer = TextPrimary,
    secondary = AccentSecondary,
    onSecondary = TextOnAccent,
    secondaryContainer = AccentSecondary.copy(alpha = 0.3f),
    onSecondaryContainer = TextPrimary,
    tertiary = PointsGold,
    onTertiary = BackgroundBase,
    tertiaryContainer = PointsGold.copy(alpha = 0.3f),
    onTertiaryContainer = TextPrimary,
    error = Error,
    onError = TextOnAccent,
    errorContainer = Error.copy(alpha = 0.3f),
    onErrorContainer = TextPrimary,
    background = BackgroundBase,
    onBackground = TextPrimary,
    surface = BackgroundBase,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundBase,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = BackgroundBase,
    surfaceContainerLow = BackgroundBase,
    surfaceContainer = BackgroundBase,
    surfaceContainerHigh = BackgroundBase,
    surfaceContainerHighest = BackgroundBase,
    outline = GlassBorder,
    outlineVariant = TextTertiary,
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundBase,
    inversePrimary = AccentPrimary,
    surfaceTint = BackgroundBase
)

@Composable
fun StatUpTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
