package com.project.tickr.ui.screen.expiry.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

private val CATEGORY_NAMES = listOf(
    "Makanan & Minuman",
    "Kecantikan",
    "Obat & Vitamin",
    "Lainnya",
)

@Composable
fun CategoryFilterRow(
    selectedCategoryName: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            label = "Semua",
            isSelected = selectedCategoryName == null,
            onClick = { onCategorySelected(null) },
        )
        CATEGORY_NAMES.forEach { name ->
            FilterChip(
                label = name,
                isSelected = selectedCategoryName == name,
                onClick = { onCategorySelected(name) },
            )
        }
    }
}

@Composable
internal fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.primaryBrand else colors.surface,
        animationSpec = tween(180),
        label = "chipBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else colors.textSecondary,
        animationSpec = tween(180),
        label = "chipText",
    )
    val borderColor = if (isSelected) Color.Transparent else colors.textSecondary.copy(alpha = 0.25f)

    Text(
        text = label,
        style = typography.caption,
        color = textColor,
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(TickrCornerRadius.pill))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(TickrCornerRadius.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun CategoryFilterRowPreview() {
    TickrTheme {
        CategoryFilterRow(
            selectedCategoryName = "Makanan & Minuman",
            onCategorySelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
