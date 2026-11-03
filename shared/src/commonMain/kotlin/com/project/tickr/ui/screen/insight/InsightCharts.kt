package com.project.tickr.ui.screen.insight

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.MonthBucket
import com.project.tickr.ui.i18n.shortMonthLabel
import com.project.tickr.ui.i18n.weekdayShortLabel
import com.project.tickr.ui.theme.TickrTheme

/** 0 → 1 sekali saat pertama tampil; dipakai agar grafik "tumbuh" masuk. */
@Composable
internal fun rememberReveal(key: Any? = Unit, durationMillis: Int = 700): Float {
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(key) { go = true }
    val progress by animateFloatAsState(
        targetValue = if (go) 1f else 0f,
        animationSpec = tween(durationMillis),
        label = "insight_reveal",
    )
    return progress
}

/** Cincin setengah-lingkaran-lebih (270°) untuk skor 0–100. */
@Composable
internal fun ScoreRing(
    score: Int,
    color: Color,
    modifier: Modifier = Modifier,
    ringSize: Dp = 148.dp,
    strokeWidth: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val track = TickrTheme.colors.textSecondary.copy(alpha = 0.15f)
    val progress = rememberReveal(score, durationMillis = 900)
    Box(modifier = modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(ringSize)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(
                color = track, startAngle = 135f, sweepAngle = 270f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
            )
            val sweep = 270f * (score.coerceIn(0, 100) / 100f) * progress
            if (sweep > 0f) {
                drawArc(
                    color = color, startAngle = 135f, sweepAngle = sweep, useCenter = false,
                    topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

/** Batang berpasangan (dikonsumsi vs terbuang) per bulan. */
@Composable
internal fun MonthlyTrendChart(
    months: List<MonthBucket>,
    consumedColor: Color,
    wastedColor: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 128.dp,
) {
    val typography = TickrTheme.typography
    val colors = TickrTheme.colors
    val maxValue = months.maxOfOrNull { maxOf(it.consumed, it.wasted) }?.coerceAtLeast(1) ?: 1
    val reveal = rememberReveal(months)

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        months.forEach { m ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.height(chartHeight).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    ValueBar(m.consumed, maxValue, chartHeight, consumedColor, reveal, Modifier.weight(1f))
                    ValueBar(m.wasted, maxValue, chartHeight, wastedColor, reveal, Modifier.weight(1f))
                }
                Box(Modifier.height(6.dp))
                Text(
                    text = shortMonthLabel(m.month),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun ValueBar(
    value: Int,
    max: Int,
    chartHeight: Dp,
    color: Color,
    reveal: Float,
    modifier: Modifier = Modifier,
) {
    val typography = TickrTheme.typography
    val colors = TickrTheme.colors
    Column(modifier = modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
        if (value > 0) {
            Text(text = value.toString(), style = typography.caption, color = colors.textSecondary)
        }
        val barHeight = if (value == 0) 3.dp else (chartHeight - 20.dp) * (value.toFloat() / max) * reveal
        Box(
            Modifier
                .fillMaxWidth()
                .height(barHeight.coerceAtLeast(3.dp))
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                .background(if (value == 0) color.copy(alpha = 0.25f) else color),
        )
    }
}

/** Satu batang tersegmentasi; segmen bernilai 0 dilewati. Bila semuanya 0 hanya menampilkan jalur kosong. */
@Composable
internal fun SegmentedBar(
    segments: List<Pair<Int, Color>>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 12.dp,
) {
    val shape = RoundedCornerShape(50)
    val reveal = rememberReveal(segments.map { it.first })
    val visible = segments.filter { it.first > 0 }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(shape)
            .background(TickrTheme.colors.textSecondary.copy(alpha = 0.12f)),
    ) {
        if (visible.isNotEmpty()) {
            Row(Modifier.fillMaxHeight().fillMaxWidth(reveal)) {
                visible.forEach { (value, color) ->
                    Box(Modifier.weight(value.toFloat()).fillMaxHeight().background(color))
                }
            }
        }
    }
}

/** Tujuh batang Senin–Minggu; hari tertinggi (jika ada) diberi warna penuh. */
@Composable
internal fun WeekdayChart(
    counts: List<Int>,
    highlightIndex: Int?,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 84.dp,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val max = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val reveal = rememberReveal(counts)

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        counts.forEachIndexed { index, count ->
            val highlighted = highlightIndex == index
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.height(chartHeight).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .width(18.dp)
                            .height((chartHeight * (count.toFloat() / max) * reveal).coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(
                                if (highlighted) colors.primaryBrand
                                else colors.primaryBrand.copy(alpha = if (count == 0) 0.1f else 0.3f),
                            ),
                    )
                }
                Box(Modifier.height(6.dp))
                Text(
                    text = weekdayShortLabel(index + 1),
                    style = typography.caption,
                    color = if (highlighted) colors.primaryBrand else colors.textSecondary,
                )
            }
        }
    }
}
