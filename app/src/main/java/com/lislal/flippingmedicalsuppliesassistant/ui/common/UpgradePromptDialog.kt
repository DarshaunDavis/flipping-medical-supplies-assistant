// app/src/main/java/com/tundynamcorp/com.lislal.flippingmedicalsuppliesassistant/ui/common/UpgradePromptDialog.kt
package com.lislal.flippingmedicalsuppliesassistant.ui.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun UpgradePromptDialog(
    onSubscribe: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Upgrade to Premium") },
        text    = { Text("Unlock unlimited invoicing and all features by subscribing.") },
        confirmButton = {
            TextButton(onClick = onSubscribe) { Text("Subscribe") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Not now") }
        }
    )
}
