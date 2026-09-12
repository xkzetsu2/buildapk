package com.example.gallery.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFFCF6679),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    outline = Color(0xFF767676),
    outlineVariant = Color(0xFF4A4A4A)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF018786),
    tertiary = Color(0xFFC2185B),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFFE0E0E0),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFBDBDBD)
)

@Composable
fun GalleryTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}