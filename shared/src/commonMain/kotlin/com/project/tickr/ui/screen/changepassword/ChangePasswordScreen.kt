package com.project.tickr.ui.screen.changepassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.tickr.presentation.changepassword.ChangePasswordAction
import com.project.tickr.presentation.changepassword.ChangePasswordStep
import com.project.tickr.presentation.changepassword.ChangePasswordUiState
import com.project.tickr.ui.screen.auth.components.PasswordTextField
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    state: ChangePasswordUiState,
    onAction: (ChangePasswordAction) -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    val topBarTitle = when (state.step) {
        ChangePasswordStep.Form -> "Ubah Kata Sandi" // TODO(user): string resource password_title
        else -> "Keamanan Akun" // TODO(user): string resource security_title
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = topBarTitle,
                        style = TickrTheme.typography.productName,
                        color = colors.primaryBrand,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(ChangePasswordAction.Back) }) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Kembali", // TODO(user): cd_back
                            tint = colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.surface,
                    scrolledContainerColor = colors.surface,
                ),
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.92f))
            },
            modifier = Modifier.padding(innerPadding),
        ) { step ->
            when (step) {
                ChangePasswordStep.Form -> ChangePasswordForm(state = state, onAction = onAction)
                ChangePasswordStep.Success -> ChangePasswordSuccess(onAction = onAction)
                ChangePasswordStep.Failure -> ChangePasswordFailure(
                    errorMessage = state.errorMessage,
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
private fun ChangePasswordForm(
    state: ChangePasswordUiState,
    onAction: (ChangePasswordAction) -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(spacing.xl))

        // Hero icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(colors.primaryBrand.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Security,
                contentDescription = null,
                tint = colors.primaryBrand,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(spacing.md))
        Text(
            text = "Amankan Akun Anda", // TODO(user): string resource password_hero_title
            style = TickrTheme.typography.sectionTitle,
            color = colors.primaryBrand,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = "Perbarui kata sandi secara berkala untuk menjaga data inventaris tetap aman.", // TODO(user): password_hero_subtitle
            style = TickrTheme.typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(spacing.xl))

        PasswordTextField(
            value = state.currentPassword,
            onValueChange = { onAction(ChangePasswordAction.CurrentChanged(it)) },
            placeholder = "Masukkan kata sandi saat ini",
            isVisible = state.currentVisible,
            onToggleVisibility = { onAction(ChangePasswordAction.ToggleCurrentVisibility) },
            label = "Kata Sandi Saat Ini", // TODO(user): string resource password_label_current
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(spacing.md))
        PasswordTextField(
            value = state.newPassword,
            onValueChange = { onAction(ChangePasswordAction.NewChanged(it)) },
            placeholder = "Masukkan kata sandi baru",
            isVisible = state.newVisible,
            onToggleVisibility = { onAction(ChangePasswordAction.ToggleNewVisibility) },
            label = "Kata Sandi Baru", // TODO(user): password_label_new
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(spacing.md))
        PasswordTextField(
            value = state.confirmPassword,
            onValueChange = { onAction(ChangePasswordAction.ConfirmChanged(it)) },
            placeholder = "Ulangi kata sandi baru",
            isVisible = state.confirmVisible,
            onToggleVisibility = { onAction(ChangePasswordAction.ToggleConfirmVisibility) },
            label = "Konfirmasi Kata Sandi Baru", // TODO(user): password_label_confirm
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(spacing.lg))

        // Info box
        Surface(
            shape = RoundedCornerShape(TickrCornerRadius.card),
            color = colors.primaryBrand.copy(alpha = 0.06f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(spacing.cardInner),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = colors.primaryBrand,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(spacing.sm))
                Text(
                    text = "Gunakan minimal 8 karakter dengan kombinasi huruf dan angka untuk keamanan maksimal.", // TODO(user): string resource password_rule_info
                    style = TickrTheme.typography.body,
                    color = colors.textSecondary,
                )
            }
        }

        Spacer(Modifier.height(spacing.xxl))

        Button(
            onClick = { onAction(ChangePasswordAction.Submit) },
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryBrand,
                disabledContainerColor = colors.primaryBrand.copy(alpha = 0.5f),
            ),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    text = "Perbarui Kata Sandi", // TODO(user): string resource password_cta_submit
                    style = TickrTheme.typography.productName,
                    color = Color.White,
                )
            }
        }
        Spacer(Modifier.height(spacing.xl))
    }
}

@Composable
private fun ChangePasswordFailure(
    errorMessage: String?,
    onAction: (ChangePasswordAction) -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.screen)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Illustration placeholder — TODO(user): ganti dengan ill_password_failed
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(colors.criticalContainer, CircleShape)
                .semantics { contentDescription = "Ilustrasi gagal memperbarui kata sandi" }, // TODO(user): cd_password_fail_illustration
            contentAlignment = Alignment.Center,
        ) {
            Text("✕", fontSize = 48.sp, color = colors.critical)
        }

        Spacer(Modifier.height(spacing.xl))
        Text(
            text = "Gagal Memperbarui Kata Sandi", // TODO(user): string resource password_fail_title
            style = TickrTheme.typography.sectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = errorMessage
                ?: "Maaf, terjadi kendala saat mencoba memperbarui kata sandi Anda. Pastikan koneksi internet stabil atau coba gunakan kata sandi yang berbeda.", // TODO(user): password_fail_body
            style = TickrTheme.typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.xxl))
        Button(
            onClick = { onAction(ChangePasswordAction.Retry) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
        ) {
            Text(
                text = "Coba Lagi", // TODO(user): string resource password_fail_retry
                style = TickrTheme.typography.productName,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(spacing.sm))
        TextButton(onClick = { /* TODO(user): hubungkan ke Destination.Help */ }) {
            Text(
                text = "Bantuan", // TODO(user): string resource password_fail_help
                style = TickrTheme.typography.body,
                color = colors.primaryBrand,
            )
        }
    }
}

@Composable
private fun ChangePasswordSuccess(onAction: (ChangePasswordAction) -> Unit) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.screen)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Illustration placeholder — TODO(user): ganti dengan ill_password_success
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(colors.safeContainer, CircleShape)
                .semantics { contentDescription = "Ilustrasi kata sandi berhasil diperbarui" }, // TODO(user): cd_password_success_illustration
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", fontSize = 48.sp, color = colors.safe)
        }

        Spacer(Modifier.height(spacing.xl))
        Text(
            text = "Kata Sandi Berhasil Diperbarui", // TODO(user): string resource password_success_title
            style = TickrTheme.typography.sectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = "Bagus! Kata sandi Anda telah berhasil diubah. Sekarang akun Anda lebih aman dan siap digunakan kembali.", // TODO(user): password_success_body
            style = TickrTheme.typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.xxl))
        Button(
            onClick = { onAction(ChangePasswordAction.BackToProfile) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
        ) {
            Text(
                text = "Kembali ke Profil", // TODO(user): string resource password_success_back
                style = TickrTheme.typography.productName,
                color = Color.White,
            )
        }
    }
}

@Preview(name = "ChangePassword Form — Light", showBackground = true)
@Composable
private fun ChangePasswordFormPreviewLight() {
    TickrTheme(darkTheme = false) {
        ChangePasswordScreen(state = ChangePasswordUiState(), onAction = {})
    }
}

@Preview(name = "ChangePassword Form — Dark", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun ChangePasswordFormPreviewDark() {
    TickrTheme(darkTheme = true) {
        ChangePasswordScreen(state = ChangePasswordUiState(), onAction = {})
    }
}

@Preview(name = "ChangePassword Success — Light", showBackground = true)
@Composable
private fun ChangePasswordSuccessPreview() {
    TickrTheme(darkTheme = false) {
        ChangePasswordScreen(state = ChangePasswordUiState(step = ChangePasswordStep.Success), onAction = {})
    }
}

@Preview(name = "ChangePassword Failure — Light", showBackground = true)
@Composable
private fun ChangePasswordFailurePreview() {
    TickrTheme(darkTheme = false) {
        ChangePasswordScreen(
            state = ChangePasswordUiState(step = ChangePasswordStep.Failure, errorMessage = "Kata sandi lama salah."),
            onAction = {},
        )
    }
}
