package com.project.tickr.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.presentation.profile.ProfileAction
import com.project.tickr.presentation.profile.ProfileUiState
import com.project.tickr.ui.screen.profile.components.ProfileHeader
import com.project.tickr.ui.screen.profile.components.ProfileMenuItem
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
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
        Spacer(Modifier.height(spacing.xxl))

        if (state.isLoading) {
            CircularProgressIndicator(color = colors.primaryBrand)
        } else {
            ProfileHeader(
                name = state.fullName,
                email = state.email,
                avatarSeed = state.avatarSeed,
                onEditClick = { onAction(ProfileAction.EditProfile) },
            )
        }

        Spacer(Modifier.height(spacing.xxl))

        // Menu card
        Surface(
            shape = RoundedCornerShape(TickrCornerRadius.card),
            color = colors.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = colors.textSecondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(TickrCornerRadius.card),
                ),
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Outlined.Link,
                    label = "Hubungkan Google", // TODO(user): gunakan string resource profile_menu_connect_google
                    onClick = {},
                    enabled = false,
                )
                MenuDivider()
                ProfileMenuItem(
                    icon = Icons.Outlined.Notifications,
                    label = "Pengaturan Notifikasi", // TODO(user): profile_menu_notifications
                    onClick = {},
                    enabled = false,
                )
                MenuDivider()
                ProfileMenuItem(
                    icon = Icons.Outlined.Lock,
                    label = "Kebijakan Privasi", // TODO(user): profile_menu_privacy
                    onClick = {},
                    enabled = false,
                )
                MenuDivider()
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Outlined.Help,
                    label = "Bantuan", // TODO(user): profile_menu_help
                    onClick = { onAction(ProfileAction.OpenHelp) },
                    enabled = true,
                )
            }
        }

        Spacer(Modifier.height(spacing.xxl))

        // Logout button — destructive style
        Button(
            onClick = { onAction(ProfileAction.SignOut) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.criticalContainer,
                contentColor = colors.critical,
            ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(spacing.sm))
            Text(
                text = "Keluar dari Akun", // TODO(user): gunakan string resource profile_logout
                style = TickrTheme.typography.body,
            )
        }

        Spacer(Modifier.height(spacing.xl))
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun MenuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .padding(horizontal = TickrTheme.spacing.lg)
            .background(TickrTheme.colors.textSecondary.copy(alpha = 0.15f)),
    )
}

@Preview(name = "Profile — Light", showBackground = true)
@Composable
private fun ProfileScreenPreviewLight() {
    TickrTheme(darkTheme = false) {
        ProfileScreen(
            state = ProfileUiState(fullName = "Budi Santoso", email = "budi@email.com", avatarSeed = "Panda"),
            onAction = {},
        )
    }
}

@Preview(name = "Profile — Dark", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun ProfileScreenPreviewDark() {
    TickrTheme(darkTheme = true) {
        ProfileScreen(
            state = ProfileUiState(fullName = "Budi Santoso", email = "budi@email.com", avatarSeed = "Panda"),
            onAction = {},
        )
    }
}
