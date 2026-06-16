package com.project.tickr.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.project.tickr.domain.model.CategoryGroup
import com.project.tickr.domain.model.ExpiringItem
import com.project.tickr.domain.model.Urgency
import com.project.tickr.ui.theme.DonutCategoryColors
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun ExpiringSection(
    groups: List<CategoryGroup>,
    criticalCount: Int,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Column(modifier = modifier) {
        // Header row: judul + badge "PERLU TINDAKAN"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Produk Kadaluwarsa", // TODO(user): pakai stringResource
                style = typography.sectionTitle,
                color = colors.textPrimary,
            )
            if (criticalCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(TickrCornerRadius.pill))
                        .background(colors.criticalContainer)
                        .padding(horizontal = spacing.sm, vertical = spacing.xs),
                ) {
                    Text(
                        text = "PERLU TINDAKAN", // TODO(user): pakai stringResource
                        style = typography.caption.copy(letterSpacing = 0.5.sp),
                        color = colors.critical,
                    )
                }
            }
        }

        if (criticalCount > 0) {
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = "Kritis: $criticalCount item", // TODO(user): format dinamis
                style = typography.caption,
                color = colors.textSecondary,
            )
        }

        Spacer(Modifier.height(spacing.md))

        groups.forEach { group ->
            CategoryGroupHeader(
                categoryName = group.categoryName,
                colorHex = group.colorHex,
            )
            Spacer(Modifier.height(spacing.sm))
            group.items.forEach { item ->
                ExpiringItemRow(
                    item = item,
                    onClick = { onItemClick(item.id) },
                )
                Spacer(Modifier.height(spacing.sm))
            }
            Spacer(Modifier.height(spacing.md))
        }
    }
}

@Composable
private fun CategoryGroupHeader(
    categoryName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(DonutCategoryColors.fromHex(colorHex)),
        )
        Text(
            text = categoryName.uppercase(), // TODO(user): pakai stringResource
            style = typography.caption.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            ),
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun ExpiringItemRow(
    item: ExpiringItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    val (badgeBg, badgeText) = when (item.urgency) {
        Urgency.CRITICAL -> colors.criticalContainer to colors.critical
        Urgency.WARNING -> colors.warningContainer to colors.warning
        Urgency.SAFE -> colors.safeContainer to colors.safe
    }

    val countdownText = when {
        item.daysUntilExpiry < 0 -> "Lewat ${-item.daysUntilExpiry} hari" // TODO(user): pakai stringResource
        item.daysUntilExpiry == 0 -> "Hari ini"
        item.daysUntilExpiry < 30 -> "${item.daysUntilExpiry} hari"
        else -> {
            val months = item.daysUntilExpiry / 30
            val days = item.daysUntilExpiry % 30
            if (days == 0) "$months bulan" else "$months bulan $days hari"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.card))
            .background(colors.surface)
            .border(1.dp, colors.textSecondary.copy(alpha = 0.10f), RoundedCornerShape(TickrCornerRadius.card))
            .clickable(onClick = onClick)
            .padding(spacing.cardInner)
            .semantics { contentDescription = "${item.name}, kedaluwarsa $countdownText" },
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
                .background(colors.textSecondary.copy(alpha = 0.12f)),
        )

        // Info tengah
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = typography.productName,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.categoryName} • ${item.expiryDate}", // TODO(user): format tanggal lebih ramah
                style = typography.caption,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Badge countdown
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(TickrCornerRadius.pill))
                .background(badgeBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = countdownText,
                style = typography.countdown,
                color = badgeText,
            )
        }

        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Lihat detail barang", // TODO(user): pakai stringResource
            tint = colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpiringSectionPreview() {
    TickrTheme {
        ExpiringSection(
            groups = listOf(
                CategoryGroup(
                    "Makanan & Minuman", "#0D6759",
                    listOf(
                        ExpiringItem(1, "Susu UHT", 1, "Makanan & Minuman", "#0D6759", "2026-06-11", null, 2, "Pcs", 1, Urgency.CRITICAL),
                        ExpiringItem(2, "Yogurt", 1, "Makanan & Minuman", "#0D6759", "2026-06-17", null, 1, "Pcs", 7, Urgency.WARNING),
                        ExpiringItem(3, "Madu", 1, "Makanan & Minuman", "#0D6759", "2026-07-10", null, 1, "Botol", 30, Urgency.SAFE),
                    )
                )
            ),
            criticalCount = 1,
            onItemClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
