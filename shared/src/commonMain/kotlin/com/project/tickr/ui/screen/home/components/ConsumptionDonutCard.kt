package com.project.tickr.ui.screen.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.CategorySlice
import com.project.tickr.ui.theme.DonutCategoryColors
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import tickrapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.project.tickr.ui.i18n.categoryLabel

@Composable
fun ConsumptionDonutCard(
    slices: List<CategorySlice>,
    totalItems: Int,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.card))
            .background(colors.surface)
            .border(
                1.dp,
                colors.textSecondary.copy(alpha = 0.12f),
                RoundedCornerShape(TickrCornerRadius.card),
            )
            .padding(spacing.cardInner),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.home_consumption_title),
                style = typography.sectionTitle,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(Res.string.home_see_all),
                style = typography.body,
                color = colors.primaryBrand,
                modifier = Modifier.clickable(onClick = onSeeAll),
            )
        }

        Spacer(Modifier.height(spacing.md))

        if (slices.isEmpty()) {
            Text(
                text = stringResource(Res.string.home_no_category_data),
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = spacing.lg),
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DonutChart(
                    slices = slices,
                    totalItems = totalItems,
                    modifier = Modifier.size(140.dp),
                )
                Spacer(Modifier.width(spacing.lg))
                DonutLegend(slices = slices, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<CategorySlice>,
    totalItems: Int,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    // Animasi sweep 0 → 1 saat pertama tampil
    var animTrigger by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "donut_sweep",
    )
    LaunchedEffect(slices) { animTrigger = true }

    val sliceColors = slices.map { DonutCategoryColors.fromHex(it.colorHex) }
    val strokeWidth = 44f // stroke tebal ~22dp
    val gap = 4f         // gap kecil antar segmen

    val accessibilityText = slices.map { "${categoryLabel(it.key, it.name)} ${it.percentage}%" }.joinToString(", ")
    val chartDescription = stringResource(Res.string.cd_donut_chart, accessibilityText)

    Box(
        modifier = modifier.semantics {
            contentDescription = chartDescription
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val inset = strokeWidth / 2f + 4f
            val arcSize = canvasSize - inset * 2
            val topLeft = Offset(inset, inset)
            var startAngle = -90f

            slices.forEachIndexed { i, slice ->
                val sweepFull = slice.percentage * 3.6f
                // Satu segmen penuh (100%) tidak butuh celah pemisah.
                val segmentGap = if (slices.size > 1) gap else 0f
                val sweep = (sweepFull - segmentGap).coerceAtLeast(0f) * animProgress
                drawArc(
                    color = sliceColors[i],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
                startAngle += sweepFull
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.home_donut_total_label),
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(
                text = totalItems.toString(),
                style = typography.donutTotalNum,
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
private fun DonutLegend(
    slices: List<CategorySlice>,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slices.forEach { slice ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(DonutCategoryColors.fromHex(slice.colorHex)),
                )
                Text(
                    text = categoryLabel(slice.key, slice.name),
                    style = typography.body,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${slice.percentage}%",
                    style = typography.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = colors.textPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ConsumptionDonutCardPreview() {
    TickrTheme {
        ConsumptionDonutCard(
            slices = listOf(
                CategorySlice(1, "Makanan & Minuman", "#0D6759", 8, 50),
                CategorySlice(2, "Kecantikan", "#FA9A08", 5, 31),
                CategorySlice(3, "Obat & Vitamin", "#5EC9B7", 3, 19),
            ),
            totalItems = 16,
            onSeeAll = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
