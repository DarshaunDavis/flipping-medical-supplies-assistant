package com.tundynamcorp.flippingmedicalsuppliesassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation.AppNavHost
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.theme.FlippingMedicalSuppliesAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FlippingMedicalSuppliesAssistantTheme {
                AppNavHost()
            }
        }
    }
}
