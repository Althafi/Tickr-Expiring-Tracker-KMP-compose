package com.project.tickr.ui.screen.home.components

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
import androidx.compose.foundation.shape.CircleShape
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
private fun rememberShimmerBrush(): Brush {
    val baseColor = TickrTheme.colors.textSecondary
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )
    return Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.07f),
            baseColor.copy(alpha = 0.20f),
            baseColor.copy(alpha = 0.07f),
        ),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 600f, 0f),
    )
}

@Composable
private fun ShimmerBox(
    brush: Brush,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp),
) {
    Box(modifier = modifier.clip(shape).background(brush))
}

@Composable
fun HomeSkeletonContent(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmerBrush()
    val spacing = TickrTheme.spacing
    val colors = TickrTheme.colors

    Column(modifier = modifier) {

        // ── Greeting header ─────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = spacing.screen)) {
            ShimmerBox(
                brush = shimmer,
                modifier = Modifier.fillMaxWidth(0.62f).height(22.dp),
            )
            Spacer(Modifier.height(spacing.xs))
            ShimmerBox(
                brush = shimmer,
                modifier = Modifier.fillMaxWidth(0.44f).height(14.dp),
            )
        }

        Spacer(Modifier.height(spacing.md))

        // ── WasteInsightCard ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .padding(horizontal = spacing.screen)
                .fillMaxWidth()
                .clip(RoundedCornerShape(TickrCornerRadius.card))
                .background(colors.surface)
                .border(
                    1.dp,
                    colors.textSecondary.copy(alpha = 0.12f),
                    RoundedCornerShape(TickrCornerRadius.card),
                )
                .padding(spacing.cardInner),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            ShimmerBox(brush = shimmer, modifier = Modifier.size(44.dp), shape = CircleShape)
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(
                    brush = shimmer,
                    modifier = Modifier.fillMaxWidth(0.85f).height(16.dp),
                )
                Spacer(Modifier.height(spacing.xs))
                ShimmerBox(
                    brush = shimmer,
                    modifier = Modifier.fillMaxWidth(0.55f).height(12.dp),
                )
            }
        }

        Spacer(Modifier.height(spacing.lg))

        // ── ConsumptionDonutCard ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .padding(horizontal = spacing.screen)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(
                    brush = shimmer,
                    modifier = Modifier.fillMaxWidth(0.45f).height(18.dp),
                )
                ShimmerBox(
                    brush = shimmer,
                    modifier = Modifier.width(72.dp).height(14.dp),
                )
            }
            Spacer(Modifier.height(spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Ring-shaped donut skeleton (outer circle + surface hole)
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ShimmerBox(
                        brush = shimmer,
                        modifier = Modifier.size(140.dp),
                        shape = CircleShape,
                    )
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                    )
                }
                Spacer(Modifier.width(spacing.lg))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    repeat(3) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ShimmerBox(
                                brush = shimmer,
                                modifier = Modifier.size(10.dp),
                                shape = CircleShape,
                            )
                            ShimmerBox(
                                brush = shimmer,
                                modifier = Modifier.weight(1f).height(12.dp),
                                shape = RoundedCornerShape(4.dp),
                            )
                            ShimmerBox(
                                brush = shimmer,
                                modifier = Modifier.width(32.dp).height(12.dp),
                                shape = RoundedCornerShape(4.dp),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(spacing.lg))

        // ── ExpiringSection ──────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = spacing.screen)) {
            ShimmerBox(
                brush = shimmer,
                modifier = Modifier.fillMaxWidth(0.50f).height(18.dp),
            )
            Spacer(Modifier.height(spacing.md))

            // Category group header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerBox(
                    brush = shimmer,
                    modifier = Modifier.size(8.dp),
                    shape = CircleShape,
                )
                ShimmerBox(
                    brush = shimmer,
                    modifier = Modifier.width(100.dp).height(12.dp),
                    shape = RoundedCornerShape(4.dp),
                )
            }
            Spacer(Modifier.height(spacing.sm))

            // Expiring item rows
            repeat(2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TickrCornerRadius.card))
                        .background(colors.surface)
                        .border(
                            1.dp,
                            colors.textSecondary.copy(alpha = 0.10f),
                            RoundedCornerShape(TickrCornerRadius.card),
                        )
                        .padding(spacing.cardInner),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    ShimmerBox(
                        brush = shimmer,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(TickrCornerRadius.thumbnail),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        ShimmerBox(
                            brush = shimmer,
                            modifier = Modifier.fillMaxWidth(0.70f).height(16.dp),
                        )
                        Spacer(Modifier.height(spacing.xs))
                        ShimmerBox(
                            brush = shimmer,
                            modifier = Modifier.fillMaxWidth(0.50f).height(12.dp),
                        )
                    }
                    ShimmerBox(
                        brush = shimmer,
                        modifier = Modifier.width(60.dp).height(24.dp),
                        shape = RoundedCornerShape(TickrCornerRadius.pill),
                    )
                }
                if (it == 0) Spacer(Modifier.height(spacing.sm))
            }
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "Skeleton · Light", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun HomeSkeletonLightPreview() {
    TickrTheme {
        HomeSkeletonContent()
    }
}

@Preview(name = "Skeleton · Dark", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun HomeSkeletonDarkPreview() {
    TickrTheme(darkTheme = true) {
        HomeSkeletonContent()
    }
}
