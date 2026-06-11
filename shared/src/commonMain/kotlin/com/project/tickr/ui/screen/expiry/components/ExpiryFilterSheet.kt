package com.project.tickr.ui.screen.expiry.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.TimeRange
import com.project.tickr.domain.model.Urgency
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpiryFilterSheet(
    pendingUrgencies: Set<Urgency>,
    pendingTimeRange: TimeRange?,
    onToggleUrgency: (Urgency) -> Unit,
    onSelectTimeRange: (TimeRange?) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen)
                .navigationBarsPadding(),
        ) {
            Text(
                text = "Filter Status", // TODO(user): stringResource filter_title
                style = typography.sectionTitle,
                color = colors.textPrimary,
            )

            Spacer(Modifier.height(spacing.lg))

            // ── Urgency group ────────────────────────────────────────────────
            Text(
                text = "Status Urgensi", // TODO(user): stringResource
                style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                val urgencyLabels = listOf(
                    Urgency.SAFE to "Aman",        // TODO(user): stringResource
                    Urgency.WARNING to "Waspada",
                    Urgency.CRITICAL to "Kritis",
                )
                urgencyLabels.forEach { (urg, label) ->
                    FilterChip(
                        label = label,
                        isSelected = urg in pendingUrgencies,
                        onClick = { onToggleUrgency(urg) },
                    )
                }
            }

            Spacer(Modifier.height(spacing.lg))

            // ── Time range group ─────────────────────────────────────────────
            Text(
                text = "Rentang Waktu", // TODO(user): stringResource
                style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                val timeRangeLabels = listOf(
                    TimeRange.GT_6M to ">6 Bulan",   // TODO(user): stringResource
                    TimeRange.GT_3M to ">3 Bulan",
                    TimeRange.GT_30D to ">30 Hari",
                    TimeRange.GT_7D to ">7 Hari",
                    TimeRange.D0 to "0 Hari",
                )
                timeRangeLabels.forEach { (range, label) ->
                    FilterChip(
                        label = label,
                        isSelected = pendingTimeRange == range,
                        // single-select: tap again to deselect
                        onClick = { onSelectTimeRange(if (pendingTimeRange == range) null else range) },
                    )
                }
            }

            Spacer(Modifier.height(spacing.xxl))

            // ── CTA buttons ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Text(
                        text = "Reset", // TODO(user): stringResource
                        style = typography.productName,
                        color = colors.textSecondary,
                    )
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(2f).height(56.dp),
                    shape = RoundedCornerShape(TickrCornerRadius.button),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                ) {
                    Text(
                        text = "Terapkan Filter", // TODO(user): stringResource filter_apply
                        style = typography.productName,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(spacing.lg))
        }
    }
}

@Preview
@Composable
private fun ExpiryFilterSheetPreview() {
    TickrTheme {
        // Preview as inline content (ModalBottomSheet doesn't preview well in isolation)
        Column(modifier = Modifier.background(TickrTheme.colors.surface).padding(20.dp)) {
            Text("Filter Status", style = TickrTheme.typography.sectionTitle, color = TickrTheme.colors.textPrimary)
        }
    }
}
