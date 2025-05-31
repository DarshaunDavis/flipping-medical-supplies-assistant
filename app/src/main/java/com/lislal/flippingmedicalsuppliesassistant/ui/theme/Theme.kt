// app/src/main/java/com/tundynamcorp/com.lislal.flippingmedicalsuppliesassistant/ui/theme/Theme.kt
package com.lislal.flippingmedicalsuppliesassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colors from your logo
private val PrimaryBlue    = Color(0xFF1E4A78)
private val SecondaryBlue  = Color(0xFF72B8E0)
private val AccentRed      = Color(0xFFFF3B3B)

// Exact backgrounds sampled from your PNGs
private val LightBg        = Color(0xFFFEFEFE)
private val DarkBg         = Color(0xFF072847)

private val LightColorScheme = lightColorScheme(
    primary       = PrimaryBlue,
    onPrimary     = Color.White,

    secondary     = SecondaryBlue,
    onSecondary   = PrimaryBlue,

    tertiary      = AccentRed,
    onTertiary    = DarkBg,

    background    = LightBg,
    onBackground  = Color(0xFF222222),

    surface       = Color.White,
    onSurface     = Color(0xFF222222),

    error         = AccentRed,
    onError       = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary       = SecondaryBlue,
    onPrimary     = DarkBg,

    secondary     = PrimaryBlue,
    onSecondary   = Color.White,

    tertiary      = AccentRed,
    onTertiary    = Color.White,

    background    = DarkBg,
    onBackground  = Color(0xFFE0E6F0),

    surface       = Color(0xFF112240),
    onSurface     = Color(0xFFE0E6F0),

    error         = AccentRed,
    onError       = DarkBg
)

@Composable
fun FlippingMedicalSuppliesAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography, // your existing typography definitions
        shapes = Shapes,         // your existing shape definitions
        content = content
    )
}
