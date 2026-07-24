package com.example.habittracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Zestreak brand palette: deep teal primary on a soft mint background,
// white cards — matches the app's onboarding and home screen reference design.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0E8C7F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFEFE9),
    onPrimaryContainer = Color(0xFF00201B),
    secondary = Color(0xFFFF7043),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF3A0B00),
    background = Color(0xFFEAF7F2),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE3EEEA),
    onSurfaceVariant = Color(0xFF49554F),
    onBackground = Color(0xFF13251F),
    onSurface = Color(0xFF13251F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005044),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFFFAB91),
    onSecondary = Color(0xFF4A1300),
    secondaryContainer = Color(0xFF66290F),
    onSecondaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF14181A),
    surface = Color(0xFF1C2124),
    surfaceVariant = Color(0xFF2A3033),
    onSurfaceVariant = Color(0xFFC2C8C7)
)

/** Reserved for a spot of accent color across the app (e.g. day-of-week selection). */
val HabitLightGreen = Color(0xFF81C784)

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HabitTrackerTypography,
        content = content
    )
}
