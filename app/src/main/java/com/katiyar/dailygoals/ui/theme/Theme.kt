package com.katiyar.dailygoals.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.katiyar.dailygoals.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Sage,
    onPrimaryContainer = Ink,
    secondary = ColorTokens.Secondary,
    tertiary = Amber,
    error = Coral,
    background = Cream,
    surface = Cream,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE5EAE7),
)

private val DarkColors = darkColorScheme(
    primary = ForestDark,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF073728),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF184536),
    secondary = androidx.compose.ui.graphics.Color(0xFFB8CCC1),
    tertiary = androidx.compose.ui.graphics.Color(0xFFF4C574),
    background = DarkSurface,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
)

private object ColorTokens {
    val Secondary = androidx.compose.ui.graphics.Color(0xFF52665C)
}

@Composable
fun DailyGoalsTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = DailyGoalsTypography,
        content = content,
    )
}
