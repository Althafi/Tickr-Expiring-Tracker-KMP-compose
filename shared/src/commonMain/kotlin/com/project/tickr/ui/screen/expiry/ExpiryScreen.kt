package com.project.tickr.ui.screen.expiry

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.project.tickr.domain.model.ConsumptionStats
import com.project.tickr.domain.model.ExpiringItem
import com.project.tickr.domain.model.Urgency
import com.project.tickr.presentation.expiry.ExpiryAction
import com.project.tickr.presentation.expiry.ExpiryLoadState
import com.project.tickr.presentation.expiry.ExpiryUiState
import com.project.tickr.ui.screen.expiry.components.CategoryFilterRow
import com.project.tickr.ui.screen.expiry.components.ExpiryEmptyState
import com.project.tickr.ui.screen.expiry.components.ExpiryFilterSheet
import com.project.tickr.ui.screen.expiry.components.ExpiryItemCard
import com.project.tickr.ui.screen.expiry.components.ExpirySkeletonContent
import com.project.tickr.ui.screen.expiry.components.ExpirySearchBar
import com.project.tickr.ui.screen.expiry.components.StatCardPair
import com.project.tickr.ui.screen.expiry.components.UrgencyLegend
import com.project.tickr.ui.screen.home.components.TickrTopBar
import com.project.tickr.ui.theme.TickrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryScreen(
    state: ExpiryUiState,
    onAction: (ExpiryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = TickrTheme.spacing
    val typography = TickrTheme.typography
    val colors = TickrTheme.colors
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(ExpiryAction.Refresh) },
        state = pullState,
        modifier = modifier,
        indicator = {},
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar (wordmark + jam berdetak) ─────────────────────────────
            item {
                TickrTopBar(
                    nowEpochMillis = state.nowEpochMillis,
                    displayDate = state.displayDate,
                    displayTime = state.displayTime,
                    onNotificationClick = { /* TODO: navigasi notifikasi */ },
                )
            }

            // ── Search + Filter ───────────────────────────────────────────────
            item {
                ExpirySearchBar(
                    query = state.activeFilter.query,
                    onQueryChange = { onAction(ExpiryAction.QueryChanged(it)) },
                    onFilterClick = { onAction(ExpiryAction.OpenFilterSheet) },
                    modifier = Modifier.padding(horizontal = spacing.screen),
                )
                Spacer(Modifier.height(spacing.md))
            }

            // ── Category filter chips ─────────────────────────────────────────
            item {
                CategoryFilterRow(
                    selectedCategoryName = state.activeFilter.categoryName,
                    onCategorySelected = { onAction(ExpiryAction.CategorySelected(it)) },
                    modifier = Modifier.padding(horizontal = spacing.screen),
                )
                Spacer(Modifier.height(spacing.lg))
            }

            // ── Content area — 3 states ───────────────────────────────────────
            when (state.loadState) {

                ExpiryLoadState.Loading -> {
                    item {
                        ExpirySkeletonContent()
                        Spacer(Modifier.height(spacing.xxl))
                    }
                }

                ExpiryLoadState.Content -> {
                    item {
                        StatCardPair(
                            stats = state.stats,
                            modifier = Modifier.padding(horizontal = spacing.screen),
                        )
                        Spacer(Modifier.height(spacing.md))
                    }
                    item {
                        UrgencyLegend(modifier = Modifier.padding(horizontal = spacing.screen))
                        Spacer(Modifier.height(spacing.lg))
                    }
                    item {
                        Text(
                            text = "Status Kedaluwarsa", // TODO(user): stringResource expiry_section_title
                            style = typography.sectionTitle,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = spacing.screen),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Pantau kesegaran stok rumah Anda.", // TODO(user): stringResource
                            style = typography.body,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = spacing.screen),
                        )
                        Spacer(Modifier.height(spacing.md))
                    }
                    items(state.items, key = { it.id }) { item ->
                        ExpiryItemCard(
                            item = item,
                            onClick = { onAction(ExpiryAction.ItemClicked(item.id)) },
                            modifier = Modifier
                                .padding(horizontal = spacing.screen)
                                .animateItem(),
                        )
                        Spacer(Modifier.height(spacing.md))
                    }
                    item { Spacer(Modifier.height(spacing.xxl)) }
                }

                ExpiryLoadState.Empty -> {
                    if (state.stats != null) {
                        item {
                            StatCardPair(
                                stats = state.stats,
                                modifier = Modifier.padding(horizontal = spacing.screen),
                            )
                            Spacer(Modifier.height(spacing.md))
                        }
                    }
                    item {
                        ExpiryEmptyState(
                            onAddClick = { onAction(ExpiryAction.AddClicked) },
                            modifier = Modifier.padding(horizontal = spacing.screen),
                        )
                    }
                }

                is ExpiryLoadState.Error -> {
                    item {
                        Text(
                            text = state.loadState.message,
                            style = typography.body,
                            color = colors.critical,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(spacing.screen),
                        )
                    }
                }
            }
        }
    }

    // ── Filter Bottom Sheet ───────────────────────────────────────────────────
    if (state.isFilterSheetVisible) {
        ExpiryFilterSheet(
            pendingUrgencies = state.pendingUrgencies,
            pendingTimeRange = state.pendingTimeRange,
            onToggleUrgency = { onAction(ExpiryAction.ToggleUrgency(it)) },
            onSelectTimeRange = { onAction(ExpiryAction.SelectTimeRange(it)) },
            onApply = { onAction(ExpiryAction.ApplyFilter) },
            onReset = { onAction(ExpiryAction.ResetFilter) },
            onDismiss = { onAction(ExpiryAction.DismissFilterSheet) },
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val sampleItems = listOf(
    ExpiringItem(1, "Susu UHT", 1, "Makanan", "#0D6759", "2026-06-11", null, 2, "Karton", 0, Urgency.CRITICAL),
    ExpiringItem(2, "Yogurt", 1, "Makanan", "#0D6759", "2026-06-15", null, 1, "Pcs", 5, Urgency.WARNING),
    ExpiringItem(3, "Vitamin C", 2, "Obat", "#FA9A08", "2026-07-20", null, 1, "Botol", 40, Urgency.SAFE),
)

@Preview(name = "Expiry · Content (Light)", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpiryContentPreview() {
    TickrTheme {
        ExpiryScreen(
            state = ExpiryUiState(
                loadState = ExpiryLoadState.Content,
                items = sampleItems,
                stats = ConsumptionStats(consumed = 8, wasted = 2),
                nowEpochMillis = 1749470828000L,
                displayDate = "Rabu, 11 Jun 2026",
                displayTime = "09:15:32",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Expiry · Content (Dark)", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun ExpiryContentDarkPreview() {
    TickrTheme(darkTheme = true) {
        ExpiryScreen(
            state = ExpiryUiState(
                loadState = ExpiryLoadState.Content,
                items = sampleItems,
                stats = ConsumptionStats(consumed = 8, wasted = 2),
                nowEpochMillis = 1749470828000L,
                displayDate = "Rabu, 11 Jun 2026",
                displayTime = "09:15:32",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Expiry · Skeleton (Light)", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpirySkeletonPreview() {
    TickrTheme {
        ExpiryScreen(
            state = ExpiryUiState(
                loadState = ExpiryLoadState.Loading,
                nowEpochMillis = 1749470828000L,
                displayDate = "Rabu, 11 Jun 2026",
                displayTime = "09:15:32",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Expiry · Skeleton (Dark)", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun ExpirySkeletonDarkPreview() {
    TickrTheme(darkTheme = true) {
        ExpiryScreen(
            state = ExpiryUiState(
                loadState = ExpiryLoadState.Loading,
                nowEpochMillis = 1749470828000L,
                displayDate = "Rabu, 11 Jun 2026",
                displayTime = "09:15:32",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Expiry · Empty (Light)", showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpiryEmptyPreview() {
    TickrTheme {
        ExpiryScreen(
            state = ExpiryUiState(
                loadState = ExpiryLoadState.Empty,
                stats = ConsumptionStats(consumed = 0, wasted = 0),
                nowEpochMillis = 1749470828000L,
                displayDate = "Rabu, 11 Jun 2026",
                displayTime = "09:15:32",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Expiry · Empty (Dark)", showBackground = true, backgroundColor = 0xFF101713)
@Composable
private fun ExpiryEmptyDarkPreview() {
    TickrTheme(darkTheme = true) {
        ExpiryScreen(
            state = ExpiryUiState(
                loadState = ExpiryLoadState.Empty,
                nowEpochMillis = 1749470828000L,
                displayDate = "Rabu, 11 Jun 2026",
                displayTime = "09:15:32",
            ),
            onAction = {},
        )
    }
}
