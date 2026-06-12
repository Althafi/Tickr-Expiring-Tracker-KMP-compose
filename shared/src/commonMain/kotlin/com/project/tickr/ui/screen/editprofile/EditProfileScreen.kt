package com.project.tickr.ui.screen.editprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.project.tickr.presentation.editprofile.EditProfileAction
import com.project.tickr.presentation.editprofile.EditProfileUiState
import com.project.tickr.ui.screen.auth.components.TickrTextField
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    onAction: (EditProfileAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    if (state.showAvatarPicker) {
        AvatarPickerSheet(
            currentSeed = state.avatarSeed,
            onSelected = { seed -> onAction(EditProfileAction.AvatarSelected(seed)) },
            onDismiss = { onAction(EditProfileAction.DismissAvatarPicker) },
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Profil", // TODO(user): string resource edit_profile_title
                        style = TickrTheme.typography.productName,
                        color = colors.primaryBrand,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(EditProfileAction.Back) },
                        modifier = Modifier.semantics { contentDescription = "Kembali" }, // TODO(user): cd_back
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    scrolledContainerColor = colors.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(spacing.lg))

            // Avatar with edit badge — tap to open picker
            AvatarWithEditBadge(
                seed = state.avatarSeed,
                onClick = { onAction(EditProfileAction.ShowAvatarPicker) },
            )

            Spacer(Modifier.height(spacing.sm))
            Text(
                text = "Ubah Avatar", // TODO(user): string resource edit_profile_change_photo
                style = TickrTheme.typography.body,
                color = colors.primaryBrand,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(spacing.xl))

            // Name field
            TickrTextField(
                value = state.name,
                onValueChange = { onAction(EditProfileAction.NameChanged(it)) },
                placeholder = "Nama Lengkap", // TODO(user): sesuaikan
                label = "Nama Lengkap", // TODO(user): string resource edit_profile_label_name
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                },
                error = state.nameError,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(spacing.md))

            // Email field (read-only)
            TickrTextField(
                value = state.email,
                onValueChange = {},
                placeholder = "nama@email.com",
                label = "Alamat Email", // TODO(user): string resource edit_profile_label_email
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                },
                enabled = false, // TODO(user): atur ke true bila email editable
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(spacing.lg))

            // Change password outline button
            OutlinedButton(
                onClick = { onAction(EditProfileAction.OpenChangePassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(TickrCornerRadius.button),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryBrand),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryBrand),
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(spacing.sm))
                Text(
                    text = "Ubah Kata Sandi", // TODO(user): string resource edit_profile_change_password
                    style = TickrTheme.typography.body,
                )
            }

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
                        text = "Informasi profil Anda akan digunakan untuk memudahkan proses pengelolaan stok dan pengingat tanggal kedaluwarsa.", // TODO(user): string resource edit_profile_info
                        style = TickrTheme.typography.body,
                        color = colors.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(spacing.xxl))

            // Save button
            Button(
                onClick = { onAction(EditProfileAction.Save) },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(TickrCornerRadius.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryBrand,
                    disabledContainerColor = colors.primaryBrand.copy(alpha = 0.5f),
                ),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Simpan Perubahan", // TODO(user): string resource edit_profile_save
                        style = TickrTheme.typography.productName,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(spacing.xl))
        }
    }
}

@Composable
fun AvatarWithEditBadge(
    seed: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 96,
) {
    val colors = TickrTheme.colors

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier,
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(colors.primaryBrand.copy(alpha = 0.08f))
                .border(1.5.dp, colors.primaryBrand.copy(alpha = 0.25f), CircleShape)
                .clickable(onClick = onClick)
                .semantics { contentDescription = "Foto profil" }, // TODO(user): cd_profile_avatar
        ) {
            if (seed.isNotBlank()) {
                AsyncImage(
                    model = dicebearUrl(seed, size = size * 2),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size.dp).clip(CircleShape),
                )
            } else {
                // Fallback sebelum seed tersedia
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = colors.primaryBrand.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size((size * 0.55f).dp)
                        .align(Alignment.Center),
                )
            }
        }

        // Edit badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(colors.primaryBrand)
                .offset(x = 2.dp, y = 2.dp)
                .clickable(onClick = onClick)
                .semantics { contentDescription = "Ubah avatar" }, // TODO(user): cd_edit_change_photo
            contentAlignment = Alignment.Center,
        ) {
            // Ikon pensil kecil
            Text("✎", color = Color.White, style = TickrTheme.typography.countdown)
        }
    }
}

@Preview(name = "EditProfile — Light", showBackground = true)
@Composable
private fun EditProfileScreenPreviewLight() {
    TickrTheme(darkTheme = false) {
        EditProfileScreen(
            state = EditProfileUiState(name = "Budi Santoso", email = "budi@email.com", avatarSeed = "Panda"),
            onAction = {},
        )
    }
}

@Preview(name = "EditProfile — Dark", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun EditProfileScreenPreviewDark() {
    TickrTheme(darkTheme = true) {
        EditProfileScreen(
            state = EditProfileUiState(name = "Budi Santoso", email = "budi@email.com", avatarSeed = "Koala"),
            onAction = {},
        )
    }
}
