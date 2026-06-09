package com.project.tickr.ui.screen.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.project.tickr.presentation.navigation.Destination
import com.project.tickr.presentation.navigation.Navigator
import kotlinx.coroutines.delay

@Composable
fun RegisterSuccessRoute(
    navigator: Navigator,
) {
    // Auto-navigate ke Home setelah animasi selesai (~1500ms)
    LaunchedEffect(Unit) {
        delay(1500L)
        navigator.navigate(Destination.Home, popUpToInclusive = true)
    }

    RegisterSuccessScreen()
}
