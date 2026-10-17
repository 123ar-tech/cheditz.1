package com.cheditz.admin.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val VioletPrimary = Color(0xFF9C6BFF)
val VioletSecondary = Color(0xFF7C4DFF)
val CyanAccent = Color(0xFF00E5FF)
val OrangeAccent = Color(0xFFFF9100)
val DeepBlack = Color(0xFF0A0A0F)
val DarkSurface = Color(0xFF12121A)
val DarkCard = Color(0xFF1A1A26)
val DarkCardBorder = Color(0xFF2A2A3A)
val TextPrimary = Color(0xFFEAEAF0)
val TextSecondary = Color(0xFF9898A8)
val SuccessGreen = Color(0xFF00E676)
val ErrorRed = Color(0xFFFF5252)
val WarningAmber = Color(0xFFFFC107)

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    secondary = VioletSecondary,
    onSecondary = Color.White,
    tertiary = CyanAccent,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = VioletSecondary,
    onPrimary = Color.White,
    secondary = VioletPrimary,
    background = Color(0xFFF8F8FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F0F5)
)

@Composable
fun CheditzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        typography = CheditzTypography,
        content = content
    )
}
