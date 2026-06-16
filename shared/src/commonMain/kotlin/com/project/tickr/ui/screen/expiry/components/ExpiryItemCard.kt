package com.project.tickr.ui.screen.expiry.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.project.tickr.domain.model.ExpiringItem
import com.project.tickr.domain.model.Urgency
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun ExpiryItemCard(
    item: ExpiringItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "cardScale",
    )

    Row(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.card))
            .background(colors.surface)
            .border(1.dp, colors.textSecondary.copy(alpha = 0.10f), RoundedCornerShape(TickrCornerRadius.card))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(spacing.cardInner),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = "Foto produk ${item.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(TickrCornerRadius.thumbnail))
                .background(colors.primaryBrand.copy(alpha = 0.08f)),
        )

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = typography.productName,
                color = colors.textPrimary,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.categoryName,
                style = typography.body,
                color = colors.textSecondary,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Kedaluwarsa: ${item.expiryDate.toExpiryDisplayDate()}", // TODO(user): stringResource
                style = typography.caption,
                color = colors.textSecondary,
                maxLines = 1,
            )
        }

        // Countdown badge
        val (badgeBg, badgeText) = when (item.urgency) {
            Urgency.CRITICAL -> colors.criticalContainer to colors.critical
            Urgency.WARNING -> colors.warningContainer to colors.warning
            Urgency.SAFE -> colors.safeContainer to colors.safe
        }
        Text(
            text = item.daysUntilExpiry.toCountdownLabel(),
            style = typography.countdown,
            color = badgeText,
            modifier = Modifier
                .clip(RoundedCornerShape(TickrCornerRadius.pill))
                .background(badgeBg)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

// Formatting helpers (presentation layer — no locale dependency for now)
internal fun Int.toCountdownLabel(): String = when {
    this <= 0 -> "0 hari"          // TODO(user): finalkan threshold & format
    this < 30 -> "$this hari"
    else -> {
        val months = this / 30
        val days = this % 30
        if (days == 0) "$months bulan" else "$months bulan $days hari"
    }
}

internal fun String.toExpiryDisplayDate(): String {
    val parts = split("-")
    if (parts.size != 3) return this
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des") // TODO(user): sesuaikan
    val idx = (parts[1].toIntOrNull() ?: 0) - 1
    val month = monthNames.getOrNull(idx) ?: parts[1]
    return "${parts[2]} $month ${parts[0]}"
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpiryItemCardCriticalPreview() {
    TickrTheme {
        ExpiryItemCard(
            item = ExpiringItem(1, "Susu UHT", 1, "Makanan", "#0D6759", "2026-06-11", null, 2, "Karton", 0, Urgency.CRITICAL),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpiryItemCardWarningPreview() {
    TickrTheme {
        ExpiryItemCard(
            item = ExpiringItem(2, "Yogurt Strawberry", 1, "Makanan", "#0D6759", "2026-06-15", null, 1, "Pcs", 5, Urgency.WARNING),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpiryItemCardSafePreview() {
    TickrTheme {
        ExpiryItemCard(
            item = ExpiringItem(3, "Vitamin C", 2, "Obat & Vitamin", "#FA9A08", "2026-07-20", null, 3, "Botol", 40, Urgency.SAFE),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
