package com.example.lifesync.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.lifesync.AuthManager

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = OnSurfaceVariant,
    background = Surface,
    onBackground = OnSurface,
    outlineVariant = OutlineVariant,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceBright = SurfaceBright
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftLilac,
    onPrimary = Primary,
    primaryContainer = Primary,
    onPrimaryContainer = SoftLilac,
    secondary = Secondary,
    secondaryContainer = PrimaryContainer,
    onSecondaryContainer = SoftLilac,
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE2E2E8),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFC4C6CF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE2E2E8),
    outlineVariant = Color(0xFF44474E),
    surfaceContainerLow = Color(0xFF1E1E1E),
    surfaceContainerHighest = Color(0xFF2D2D2D),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceBright = Color(0xFF2D2D2D)
)

@Composable
fun VajraTheme(content: @Composable () -> Unit) {
    val isDarkMode = AuthManager.isDarkMode
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
