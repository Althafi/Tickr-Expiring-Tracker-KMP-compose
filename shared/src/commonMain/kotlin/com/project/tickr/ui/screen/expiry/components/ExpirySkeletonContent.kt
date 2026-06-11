package com.project.tickr.ui.screen.expiry.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
internal fun rememberExpiryShimmerBrush(): Brush {
    val base = TickrTheme.colors.textSecondary
    val transition = rememberInfiniteTransition(label = "expiry_shimmer")
    val translateX by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "expiry_shimmer_x",
    )
    return Brush.linearGradient(
        colors = listOf(base.copy(0.07f), base.copy(0.20f), base.copy(0.07f)),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 600f, 0f),
    )
}

@Composable
internal fun ShimmerBox(brush: Brush, modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(6.dp)) {
    Box(modifier = modifier.clip(shape).background(brush))
}

@Composable
fun ExpirySkeletonContent(modifier: Modifier = Modifier) {
    val shimmer = rememberExpiryShimmerBrush()
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Column(modifier = modifier.padding(horizontal = spacing.screen)) {

        // ── Stat card pair skeleton ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            repeat(2) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(TickrCornerRadius.card))
                        .background(colors.surface)
                        .border(1.dp, colors.textSecondary.copy(0.08f), RoundedCornerShape(TickrCornerRadius.card))
                        .padding(spacing.cardInner),
                ) {
                    ShimmerBox(shimmer, Modifier.size(36.dp), RoundedCornerShape(50))
                    Spacer(Modifier.height(spacing.sm))
                    ShimmerBox(shimmer, Modifier.width(48.dp).height(28.dp))
                    Spacer(Modifier.height(4.dp))
                    ShimmerBox(shimmer, Modifier.fillMaxWidth(0.9f).height(12.dp))
                    Spacer(Modifier.height(2.dp))
                    ShimmerBox(shimmer, Modifier.fillMaxWidth(0.7f).height(12.dp))
                }
            }
        }

        Spacer(Modifier.height(spacing.lg))

        // ── Legend skeleton ──────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.lg)) {
            repeat(3) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerBox(shimmer, Modifier.size(8.dp), RoundedCornerShape(50))
                    ShimmerBox(shimmer, Modifier.width(44.dp).height(12.dp))
                }
            }
        }

        Spacer(Modifier.height(spacing.lg))

        // ── Section header ────────────────────────────────────────────────────
        ShimmerBox(shimmer, Modifier.fillMaxWidth(0.55f).height(18.dp))
        Spacer(Modifier.height(4.dp))
        ShimmerBox(shimmer, Modifier.fillMaxWidth(0.75f).height(13.dp))

        Spacer(Modifier.height(spacing.md))

        // ── Item card skeletons ──────────────────────────────────────────────
        repeat(5) { idx ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TickrCornerRadius.card))
                    .background(colors.surface)
                    .border(1.dp, colors.textSecondary.copy(0.10f), RoundedCornerShape(TickrCornerRadius.card))
                    .padding(spacing.cardInner),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                ShimmerBox(shimmer, Modifier.size(56.dp), RoundedCornerShape(TickrCornerRadius.thumbnail))
                Column(Modifier.weight(1f)) {
                    ShimmerBox(shimmer, Modifier.fillMaxWidth(0.68f).height(16.dp))
                    Spacer(Modifier.height(4.dp))
                    ShimmerBox(shimmer, Modifier.fillMaxWidth(0.50f).height(12.dp))
                    Spacer(Modifier.height(4.dp))
                    ShimmerBox(shimmer, Modifier.fillMaxWidth(0.60f).height(12.dp))
                }
                ShimmerBox(shimmer, Modifier.width(64.dp).height(24.dp), RoundedCornerShape(TickrCornerRadius.pill))
            }
            if (idx < 4) Spacer(Modifier.height(spacing.md))
        }
    }
}

@Preview(name = "Expiry Skeleton · Light", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpirySkeletonLightPreview() {
    TickrTheme { ExpirySkeletonContent() }
}

@Preview(name = "Expiry Skeleton · Dark", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun ExpirySkeletonDarkPreview() {
    TickrTheme(darkTheme = true) { ExpirySkeletonContent() }
}
