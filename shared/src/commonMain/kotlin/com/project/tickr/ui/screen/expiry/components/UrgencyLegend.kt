package com.project.tickr.ui.screen.expiry.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun UrgencyLegend(modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(color = colors.critical, label = "Kritis")       // TODO(user): stringResource
        LegendItem(color = colors.warning, label = "Waspada")
        LegendItem(color = colors.safe, label = "Aman")
    }
}

@Composable
private fun LegendItem(color: Color, label: String, modifier: Modifier = Modifier) {
    val typography = TickrTheme.typography
    val colors = TickrTheme.colors

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = typography.caption,
            color = colors.textSecondary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun UrgencyLegendPreview() {
    TickrTheme {
        UrgencyLegend(modifier = Modifier.padding(16.dp))
    }
}
