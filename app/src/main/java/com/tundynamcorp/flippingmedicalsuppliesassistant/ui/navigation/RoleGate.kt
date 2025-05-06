// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/navigation/RoleGate.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation

import androidx.compose.runtime.Composable
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.UserRole

@Composable
fun RoleGate(
    currentRole: UserRole,
    allowed: Set<UserRole>,
    onDenied: @Composable () -> Unit = { /* show your Upgrade dialog here */ },
    content: @Composable () -> Unit
) {
    if (currentRole in allowed) {
        content()
    } else {
        onDenied()
    }
}
