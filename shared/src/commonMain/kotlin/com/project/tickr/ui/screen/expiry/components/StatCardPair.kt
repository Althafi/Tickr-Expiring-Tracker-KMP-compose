package com.project.tickr.ui.screen.expiry.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.ConsumptionStats
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun StatCardPair(
    stats: ConsumptionStats?,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        StatCard(
            value = stats?.consumed?.toString() ?: "–",
            label = "Barang Berhasil Dikonsumsi", // TODO(user): pakai stringResource
            icon = { Icon(Icons.Outlined.Check, contentDescription = null, tint = colors.safe, modifier = Modifier.size(20.dp)) },
            iconBg = colors.safeContainer,
            cardBg = colors.safeContainer.copy(alpha = 0.55f),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = stats?.wasted?.toString() ?: "–",
            label = "Barang Mubazir", // TODO(user): pakai stringResource
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.critical, modifier = Modifier.size(20.dp)) },
            iconBg = colors.criticalContainer,
            cardBg = colors.criticalContainer.copy(alpha = 0.55f),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: @Composable () -> Unit,
    iconBg: androidx.compose.ui.graphics.Color,
    cardBg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(TickrCornerRadius.card))
            .background(cardBg)
            .border(1.dp, colors.textSecondary.copy(alpha = 0.08f), RoundedCornerShape(TickrCornerRadius.card))
            .padding(spacing.cardInner),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { icon() }
        }
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = value,
            style = typography.displayDate,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = typography.caption,
            color = colors.textSecondary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun StatCardPairPreview() {
    TickrTheme {
        StatCardPair(
            stats = ConsumptionStats(consumed = 12, wasted = 3),
            modifier = Modifier.padding(16.dp),
        )
    }
}
