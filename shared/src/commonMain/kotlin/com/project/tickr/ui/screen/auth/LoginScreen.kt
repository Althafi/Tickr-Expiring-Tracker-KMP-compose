package com.project.tickr.ui.screen.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import com.project.tickr.presentation.login.LoginAction
import com.project.tickr.presentation.login.LoginUiState
import com.project.tickr.ui.screen.auth.components.GoogleButton
import com.project.tickr.ui.screen.auth.components.PasswordTextField
import com.project.tickr.ui.screen.auth.components.TickrTextField
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import org.jetbrains.compose.resources.painterResource
import tickrapp.shared.generated.resources.Res
import tickrapp.shared.generated.resources.tickr_logo

@Composable
fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
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
            Spacer(modifier = Modifier.height(48.dp))

            // Logo
            Image(
                painter = painterResource(Res.drawable.tickr_logo),
                contentDescription = "Logo Tickr", // TODO(user): gunakan resource string cd_logo_tickr
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(TickrCornerRadius.card)),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.xxl))

            // Google button
            GoogleButton(
                text = "Masuk dengan Google", // TODO(user): gunakan resource string login_google_cta
                onClick = { onAction(LoginAction.GoogleClicked) },
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.lg))

            // Divider "ATAU"
            OrDivider(label = "ATAU") // TODO(user): gunakan resource string login_divider_or

            Spacer(modifier = Modifier.height(TickrTheme.spacing.lg))

            // Identifier field
            TickrTextField(
                value = state.identifier,
                onValueChange = { onAction(LoginAction.IdentifierChanged(it)) },
                placeholder = "Masukan Nama Lengkap / Email", // TODO(user): gunakan resource string login_field_identifier_hint
                error = state.identifierError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.md))

            // Password field
            PasswordTextField(
                value = state.password,
                onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                placeholder = "Masukan Password", // TODO(user): gunakan resource string login_field_password_hint
                isVisible = state.isPasswordVisible,
                onToggleVisibility = { onAction(LoginAction.TogglePasswordVisibility) },
                error = state.passwordError,
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.xl))

            // Submit button
            PrimaryButton(
                text = "Masuk", // TODO(user): gunakan resource string login_cta_submit
                isLoading = state.isLoading,
                enabled = state.isSubmitEnabled,
                onClick = { onAction(LoginAction.Submit) },
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.md))

            // Register button
            SecondaryOutlinedButton(
                text = "Daftar jika belum punya akun", // TODO(user): gunakan resource string login_cta_register
                onClick = { onAction(LoginAction.NavigateToRegister) },
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer terms
            TermsFooter()

            Spacer(modifier = Modifier.height(TickrTheme.spacing.xl))
        }
    }
}

// ─── Shared composables ──────────────────────────────────────────────────────

@Composable
internal fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(100, easing = FastOutSlowInEasing),
        label = "btn_scale",
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                )
            },
        shape = RoundedCornerShape(TickrCornerRadius.button),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = TickrTheme.colors.primaryBrand,
            contentColor = Color.White,
            disabledContainerColor = TickrTheme.colors.primaryBrand.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.6f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = TickrTheme.typography.productName,
            )
        }
    }
}

@Composable
internal fun SecondaryOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(TickrCornerRadius.button),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = TickrTheme.colors.primaryBrand,
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TickrTheme.colors.primaryBrand,
        ),
    ) {
        Text(
            text = text,
            style = TickrTheme.typography.productName,
        )
    }
}

@Composable
internal fun OrDivider(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = TickrTheme.colors.textSecondary.copy(alpha = 0.25f),
        )
        Text(
            text = label,
            style = TickrTheme.typography.countdown,
            color = TickrTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = TickrTheme.spacing.md),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = TickrTheme.colors.textSecondary.copy(alpha = 0.25f),
        )
    }
}

@Composable
private fun TermsFooter() {
    // TODO(user): buka URL Syarat & Ketentuan / Kebijakan Privasi saat diklik
    val termsText = buildAnnotatedString {
        append("Dengan melanjutkan, kamu menyetujui ") // TODO(user): gunakan resource string login_footer_terms
        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("Syarat & Ketentuan")
        }
        append(" serta ")
        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("Kebijakan Privasi")
        }
        append(" kami.")
    }
    Text(
        text = termsText,
        style = TickrTheme.typography.countdown,
        color = TickrTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
    )
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(name = "Login · Light", showSystemUi = true)
@Composable
private fun PreviewLoginLight() {
    TickrTheme(darkTheme = false) {
        LoginScreen(state = LoginUiState(), onAction = {})
    }
}

@Preview(name = "Login · Dark", showSystemUi = true)
@Composable
private fun PreviewLoginDark() {
    TickrTheme(darkTheme = true) {
        LoginScreen(state = LoginUiState(), onAction = {})
    }
}

@Preview(name = "Login · Loading", showSystemUi = true)
@Composable
private fun PreviewLoginLoading() {
    TickrTheme(darkTheme = false) {
        LoginScreen(
            state = LoginUiState(
                identifier = "demo@tickr.app",
                password = "password123",
                isLoading = true,
            ),
            onAction = {},
        )
    }
}
