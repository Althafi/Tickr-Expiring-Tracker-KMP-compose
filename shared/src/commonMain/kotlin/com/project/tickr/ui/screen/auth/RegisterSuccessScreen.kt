package com.project.tickr.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.screen.auth.components.SuccessAnimation
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun RegisterSuccessScreen() {
    Scaffold(
        containerColor = TickrTheme.colors.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SuccessAnimation()

            Spacer(modifier = Modifier.height(TickrTheme.spacing.xl))

            Text(
                text = "Registrasi Berhasil!", // TODO(user): gunakan resource string auth_success_title
                style = TickrTheme.typography.onboardingTitle,
                color = TickrTheme.colors.primaryBrand,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.md))

            Text(
                text = "Akunmu sudah siap. Sekarang kamu bisa mulai mencatat barang dan memantau tanggal kedaluwarsa agar tidak ada lagi yang mubazir.", // TODO(user): gunakan resource string auth_success_body
                style = TickrTheme.typography.body,
                color = TickrTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(name = "RegisterSuccess · Light", showSystemUi = true)
@Composable
private fun PreviewSuccessLight() {
    TickrTheme(darkTheme = false) {
        RegisterSuccessScreen()
    }
}

@Preview(name = "RegisterSuccess · Dark", showSystemUi = true)
@Composable
private fun PreviewSuccessDark() {
    TickrTheme(darkTheme = true) {
        RegisterSuccessScreen()
    }
}
