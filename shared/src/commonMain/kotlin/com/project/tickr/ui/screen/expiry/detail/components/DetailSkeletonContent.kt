package com.project.tickr.ui.screen.expiry.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.screen.expiry.components.ShimmerBox
import com.project.tickr.ui.screen.expiry.components.rememberExpiryShimmerBrush
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun DetailSkeletonContent(modifier: Modifier = Modifier) {
    val shimmer = rememberExpiryShimmerBrush()
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    Column(modifier = modifier) {
        // Hero shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(colors.textSecondary.copy(alpha = 0.08f)),
        )

        // Content sheet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(spacing.screen),
        ) {
            Spacer(Modifier.height(spacing.md))

            // Banner shimmer
            ShimmerBox(shimmer, Modifier.fillMaxWidth().height(48.dp), RoundedCornerShape(TickrCornerRadius.button))

            Spacer(Modifier.height(spacing.lg))

            // Title shimmer
            ShimmerBox(shimmer, Modifier.fillMaxWidth(0.72f).height(28.dp))
            Spacer(Modifier.height(4.dp))
            ShimmerBox(shimmer, Modifier.fillMaxWidth(0.5f).height(20.dp))

            Spacer(Modifier.height(spacing.lg))

            // Info box pair shimmer
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                repeat(2) {
                    Column(
                        modifier = Modifier.weight(1f).background(colors.surface, RoundedCornerShape(14.dp)).padding(spacing.cardInner),
                    ) {
                        ShimmerBox(shimmer, Modifier.width(56.dp).height(12.dp))
                        Spacer(Modifier.height(spacing.sm))
                        ShimmerBox(shimmer, Modifier.width(18.dp).height(18.dp), RoundedCornerShape(4.dp))
                        Spacer(Modifier.height(4.dp))
                        ShimmerBox(shimmer, Modifier.fillMaxWidth(0.8f).height(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(spacing.md))

            // Note box shimmer
            ShimmerBox(shimmer, Modifier.fillMaxWidth().height(72.dp), RoundedCornerShape(14.dp))

            Spacer(Modifier.height(spacing.xxl))

            // CTA shimmer
            ShimmerBox(shimmer, Modifier.fillMaxWidth().height(56.dp), RoundedCornerShape(TickrCornerRadius.button))
        }
    }
}

@Preview(name = "Detail Skeleton · Light", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun DetailSkeletonLightPreview() {
    TickrTheme { DetailSkeletonContent() }
}

@Preview(name = "Detail Skeleton · Dark", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun DetailSkeletonDarkPreview() {
    TickrTheme(darkTheme = true) { DetailSkeletonContent() }
}
