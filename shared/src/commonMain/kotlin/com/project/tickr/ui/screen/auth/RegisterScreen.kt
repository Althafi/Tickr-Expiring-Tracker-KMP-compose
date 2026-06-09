package com.project.tickr.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.presentation.register.RegisterAction
import com.project.tickr.presentation.register.RegisterUiState
import com.project.tickr.ui.screen.auth.components.GoogleButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.project.tickr.ui.screen.auth.components.PasswordStrengthMeter
import com.project.tickr.ui.screen.auth.components.PasswordTextField
import com.project.tickr.ui.screen.auth.components.TickrTextField
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import org.jetbrains.compose.resources.painterResource
import tickrapp.shared.generated.resources.Res
import tickrapp.shared.generated.resources.tickr_logo

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onAction: (RegisterAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = TickrTheme.colors.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TickrTheme.spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo kecil
            Image(
                painter = painterResource(Res.drawable.tickr_logo),
                contentDescription = "Logo Tickr", // TODO(user): gunakan resource string cd_logo_tickr
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(TickrCornerRadius.thumbnail)),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.md))

            // Judul
            Text(
                text = "Daftar Akun Baru", // TODO(user): gunakan resource string register_title
                style = TickrTheme.typography.onboardingTitle.copy(
                    textAlign = TextAlign.Center,
                ),
                color = TickrTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.sm))

            // Subjudul
            Text(
                text = "Mulai kelola persediaan rumah Anda dengan lebih mudah.", // TODO(user): gunakan resource string register_subtitle
                style = TickrTheme.typography.body,
                color = TickrTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.xl))

            // Field: Nama Lengkap
            TickrTextField(
                value = state.fullName,
                onValueChange = { onAction(RegisterAction.FullNameChanged(it)) },
                placeholder = "Contoh: Budi Santoso", // TODO(user): gunakan resource string register_hint_name
                label = "Nama Lengkap", // TODO(user): gunakan resource string register_label_name
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = TickrTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                },
                error = state.fullNameError,
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.md))

            // Field: Alamat Email
            TickrTextField(
                value = state.email,
                onValueChange = { onAction(RegisterAction.EmailChanged(it)) },
                placeholder = "nama@email.com", // TODO(user): gunakan resource string register_hint_email
                label = "Alamat Email", // TODO(user): gunakan resource string register_label_email
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = TickrTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                },
                error = state.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.md))

            // Field: Kata Sandi
            PasswordTextField(
                value = state.password,
                onValueChange = { onAction(RegisterAction.PasswordChanged(it)) },
                placeholder = "Min. 8 karakter", // TODO(user): gunakan resource string register_hint_password
                label = "Kata Sandi", // TODO(user): gunakan resource string register_label_password
                isVisible = state.isPasswordVisible,
                onToggleVisibility = { onAction(RegisterAction.TogglePasswordVisibility) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = TickrTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )

            // Password strength meter (muncul saat password tidak kosong)
            if (state.password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(TickrTheme.spacing.sm))
                PasswordStrengthMeter(
                    strength = state.passwordStrength,
                    requirements = state.passwordRequirements,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(TickrTheme.spacing.xl))

            // Submit button
            PrimaryButton(
                text = "Daftar Sekarang", // TODO(user): gunakan resource string register_cta_submit
                isLoading = state.isLoading,
                enabled = state.isSubmitEnabled,
                onClick = { onAction(RegisterAction.Submit) },
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.lg))

            // Link: sudah punya akun
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Sudah punya akun? ", // TODO(user): gunakan resource string register_have_account
                    style = TickrTheme.typography.body,
                    color = TickrTheme.colors.textSecondary,
                )
                val loginLinkText = buildAnnotatedString {
                    withStyle(SpanStyle(color = TickrTheme.colors.primaryBrand)) {
                        append("Masuk di sini") // TODO(user): gunakan resource string register_login_link
                    }
                }
                Text(
                    text = loginLinkText,
                    style = TickrTheme.typography.body,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .clickableWithMinTarget { onAction(RegisterAction.NavigateToLogin) },
                )
            }

            Spacer(modifier = Modifier.height(TickrTheme.spacing.lg))

            // Divider "Atau daftar dengan"
            OrDivider(label = "Atau daftar dengan") // TODO(user): gunakan resource string register_divider_or

            Spacer(modifier = Modifier.height(TickrTheme.spacing.md))

            // Google button
            GoogleButton(
                text = "Google", // TODO(user): gunakan resource string register_google_cta
                onClick = { onAction(RegisterAction.GoogleClicked) },
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.xl))
        }
    }
}

// Modifier extension untuk minimum touch target 44dp
private fun Modifier.clickableWithMinTarget(onClick: () -> Unit): Modifier =
    this
        .padding(4.dp)
        .clickable(onClick = onClick)

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(name = "Register · Light", showSystemUi = true)
@Composable
private fun PreviewRegisterLight() {
    TickrTheme(darkTheme = false) {
        RegisterScreen(state = RegisterUiState(), onAction = {})
    }
}

@Preview(name = "Register · Dark", showSystemUi = true)
@Composable
private fun PreviewRegisterDark() {
    TickrTheme(darkTheme = true) {
        RegisterScreen(state = RegisterUiState(), onAction = {})
    }
}
