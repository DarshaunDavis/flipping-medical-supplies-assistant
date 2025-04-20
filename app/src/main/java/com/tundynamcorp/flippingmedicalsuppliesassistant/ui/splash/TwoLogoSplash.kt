package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.splash

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import kotlinx.coroutines.delay

@Composable
fun TwoLogoSplash(onFinished: () -> Unit) {
    // Use the vector foreground icon as a placeholder
    val images = listOf(
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground
    )

    var index by remember { mutableIntStateOf(0) }
    var alpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(index) {
        // Fade in
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        ) { value, _ -> alpha = value }

        // Hold
        delay(2000)

        // Fade out
        animate(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        ) { value, _ -> alpha = value }

        if (index < images.lastIndex) {
            index += 1
        } else {
            onFinished()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val painter = painterResource(id = images[index])
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
        )
    }
}
