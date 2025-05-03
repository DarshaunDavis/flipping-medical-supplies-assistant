package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary       = Color(0xFF1E4A78),
    onPrimary     = Color.White,
    secondary     = Color(0xFF72B8E0),
    onSecondary   = Color(0xFF1E4A78),
    background    = Color(0xFFFEFEFE),   // logo light‐bg
    onBackground  = Color(0xFF222222),
    surface       = Color(0xFFFFFFFF),
    onSurface     = Color(0xFF222222),
    error         = Color(0xFFFF3B3B),
    onError       = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary       = Color(0xFF72B8E0),
    onPrimary     = Color(0xFF0A1B33),
    secondary     = Color(0xFF1E4A78),
    onSecondary   = Color.White,
    background    = Color(0xFF072847),   // logo dark‐bg
    onBackground  = Color(0xFFE0E6F0),
    surface       = Color(0xFF112240),
    onSurface     = Color(0xFFE0E6F0),
    error         = Color(0xFFFF3B3B),
    onError       = Color(0xFF0A1B33)
)

@Composable
fun FlippingMedicalSuppliesAssistantTheme(
    // force your custom schemes rather than dynamic system colors
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,  // your existing typography
        shapes = Shapes,          // your existing shapes
        content = content
    )
}
