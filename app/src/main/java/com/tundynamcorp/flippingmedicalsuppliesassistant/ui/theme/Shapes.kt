package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Defines standard corner shapes for small, medium, and large surfaces across the app.
 */
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),   // Buttons, chips
    medium = RoundedCornerShape(8.dp),  // Cards, dialogs
    large = RoundedCornerShape(16.dp)   // Full-bleed elements, large containers
)
