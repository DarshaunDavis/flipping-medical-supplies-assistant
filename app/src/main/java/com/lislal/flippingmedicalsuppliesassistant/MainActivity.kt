package com.lislal.flippingmedicalsuppliesassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lislal.flippingmedicalsuppliesassistant.ui.navigation.AppNavHost
import com.lislal.flippingmedicalsuppliesassistant.ui.theme.FlippingMedicalSuppliesAssistantTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        enableEdgeToEdge()

        setContent {
            FlippingMedicalSuppliesAssistantTheme {
                AppNavHost()
            }
        }
    }
}
