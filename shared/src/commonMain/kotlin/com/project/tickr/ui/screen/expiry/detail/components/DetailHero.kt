package com.project.tickr.ui.screen.expiry.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun DetailHero(
    imageUrl: String?,
    productName: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.primaryBrand.copy(alpha = 0.08f)),
    ) {
        // Hero image — TODO(user): tambahkan Coil3 dependency dan AsyncImage saat aset siap
        // imageUrl tersedia di parameter tapi belum dirender (placeholder box bg sudah tampil)

        // Back button (kiri-atas)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.surface.copy(alpha = 0.88f))
                .semantics { contentDescription = "Kembali" }, // TODO(user): stringResource cd_detail_back
        ) {
            Icon(
                Icons.Outlined.ArrowBack,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        // Delete button (kanan-atas)
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.criticalContainer.copy(alpha = 0.9f))
                .semantics { contentDescription = "Hapus barang" }, // TODO(user): stringResource cd_detail_delete
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = null,
                tint = colors.critical,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
