package com.tundynamcorp.flippingmedicalsuppliesassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.theme.FlippingMedicalSuppliesAssistantTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation.AppNavHost
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.splash.TwoLogoSplash

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the Splash Screen and keep it until our Compose content is ready
        val splash = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // This flag lets us delay the removal of the splash until our animation completes
        var splashVisible by mutableStateOf(true)
        splash.setKeepOnScreenCondition { splashVisible }

        setContent {
            FlippingMedicalSuppliesAssistantTheme {
                // Once the APIs splash window is ready, run our two‑logo fade
                if (splashVisible) {
                    TwoLogoSplash {
                        splashVisible = false
                    }
                } else {
                    // Your real app’s NavHost goes here
                    AppNavHost()
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FlippingMedicalSuppliesAssistantTheme {
        Greeting("Android")
    }
}