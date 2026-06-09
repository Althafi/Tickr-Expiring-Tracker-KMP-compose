package com.project.tickr.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.project.tickr.ui.theme.TickrTheme

// TODO(user): hapus layar ini saat Home (Phase berikutnya) siap diimplementasikan.
@Composable
fun HomePlaceholderScreen() {
    Scaffold(
        containerColor = TickrTheme.colors.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Home (Phase berikutnya)",
                style = TickrTheme.typography.productName,
                color = TickrTheme.colors.textSecondary,
            )
        }
    }
}
