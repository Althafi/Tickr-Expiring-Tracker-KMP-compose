package com.project.tickr.ui.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import coil3.compose.AsyncImage
import com.project.tickr.ui.screen.editprofile.dicebearUrl
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun ProfileHeader(
    name: String,
    email: String,
    avatarSeed: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(colors.primaryBrand.copy(alpha = 0.08f))
                    .border(1.5.dp, colors.primaryBrand.copy(alpha = 0.25f), CircleShape)
                    .semantics { contentDescription = "Foto profil pengguna" }, // TODO(user): cd_profile_avatar
                contentAlignment = Alignment.Center,
            ) {
                if (avatarSeed.isNotBlank()) {
                    AsyncImage(
                        model = dicebearUrl(avatarSeed, size = 192),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(96.dp).clip(CircleShape),
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
                    .clickable(onClick = onEditClick)
                    .semantics { contentDescription = "Ubah profil" }, // TODO(user): cd_profile_edit_badge
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Spacer(Modifier.height(spacing.md))
        Text(
            text = name.ifBlank { "—" },
            style = TickrTheme.typography.productName,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = email.ifBlank { "—" },
            style = TickrTheme.typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
