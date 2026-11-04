package com.project.tickr.ui.screen.expiry.detail.components

import com.project.tickr.ui.common.TickrIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.project.tickr.ui.common.rememberProductImageModel
import com.project.tickr.ui.theme.TickrTheme
import tickrapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailHero(
    imageUrl: String?,
    productName: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val backDescription = stringResource(Res.string.cd_back)
    val deleteDescription = stringResource(Res.string.cd_detail_delete)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.primaryBrand.copy(alpha = 0.08f)),
    ) {
        AsyncImage(
            model = rememberProductImageModel(imageUrl),
            contentDescription = stringResource(Res.string.cd_detail_photo, productName),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        )

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
                .semantics { contentDescription = backDescription },
        ) {
            Icon(
                TickrIcons.ArrowBack,
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
                .semantics { contentDescription = deleteDescription },
        ) {
            Icon(
                TickrIcons.Delete,
                contentDescription = null,
                tint = colors.critical,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
