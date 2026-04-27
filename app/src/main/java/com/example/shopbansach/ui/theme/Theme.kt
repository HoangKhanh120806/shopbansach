package com.example.shopbansach.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD6BFA9), // Accent color in dark
    secondary = Color(0xFF1A2E35),
    tertiary = Color(0xFFD0BCFF),
    background = Color(0xFF041920), // Deep Dark Blue
    surface = Color(0xFF0A1F26),
    onPrimary = Color(0xFF041920),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFFBF9F4), // Light cream for text
    onSurface = Color(0xFFFBF9F4),
    surfaceVariant = Color(0xFF1A2E35),
    onSurfaceVariant = Color(0xFF8F8F8F)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF041920),
    secondary = Color(0xFF1A2E35),
    tertiary = Color(0xFFD6BFA9),
    background = Color(0xFFFBF9F4),
    surface = Color(0xFFF5F3EE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF041920),
    onBackground = Color(0xFF1B1C19),
    onSurface = Color(0xFF1B1C19),
    surfaceVariant = Color(0xFFE4E2DD),
    onSurfaceVariant = Color(0xFF8F8F8F)
)

@Composable
fun ShopbansachTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Tắt dynamic color để dùng theme tùy chỉnh của mình
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
