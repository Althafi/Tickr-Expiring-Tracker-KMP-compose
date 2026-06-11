package com.project.tickr.ui.screen.expiry.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun InfoBoxPair(
    categoryName: String,
    quantity: Int,
    unit: String,
    modifier: Modifier = Modifier,
) {
    val spacing = TickrTheme.spacing

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        InfoBox(
            label = "KATEGORI", // TODO(user): stringResource detail_label_category
            icon = { Icon(Icons.Outlined.Category, contentDescription = null, tint = TickrTheme.colors.primaryBrand, modifier = Modifier.size(18.dp)) },
            value = categoryName,
            modifier = Modifier.weight(1f),
        )
        InfoBox(
            label = "JUMLAH", // TODO(user): stringResource detail_label_quantity
            icon = { Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = TickrTheme.colors.primaryBrand, modifier = Modifier.size(18.dp)) },
            value = "$quantity $unit", // TODO(user): stringResource detail_quantity_format
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InfoBox(
    label: String,
    icon: @Composable () -> Unit,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.textSecondary.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(spacing.cardInner),
    ) {
        Text(
            text = label,
            style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(spacing.sm))
        icon()
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = typography.productName,
            color = colors.textPrimary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun InfoBoxPairPreview() {
    TickrTheme {
        InfoBoxPair(
            categoryName = "Makanan & Minuman",
            quantity = 1,
            unit = "Karton",
            modifier = Modifier.padding(16.dp),
        )
    }
}
