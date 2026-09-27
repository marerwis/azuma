package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AzoomaOrange,
    onPrimary = Color.White,
    primaryContainer = AzoomaOrangeLight,
    onPrimaryContainer = AzoomaOrangeDark,
    secondary = AzoomaOrangeDark,
    onSecondary = Color.White,
    background = AzoomaBackground,
    onBackground = AzoomaTextPrimary,
    surface = AzoomaSurface,
    onSurface = AzoomaTextPrimary,
    surfaceVariant = AzoomaSurfaceVariant,
    onSurfaceVariant = AzoomaTextSecondary,
    outline = AzoomaCardBorder,
)

private val DarkColorScheme = darkColorScheme(
    primary = AzoomaOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3E1A0F),
    onPrimaryContainer = Color(0xFFFFECE5),
    secondary = Color(0xFFFF7A45),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF383838),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
