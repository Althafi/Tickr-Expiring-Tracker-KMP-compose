package com.project.tickr.ui.screen.consumed

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.ExpiringItem
import com.project.tickr.domain.model.Urgency
import com.project.tickr.presentation.consumed.ConsumedItemsAction
import com.project.tickr.presentation.consumed.ConsumedItemsLoadState
import com.project.tickr.presentation.consumed.ConsumedItemsUiState
import com.project.tickr.ui.screen.expiry.components.ExpiryItemCard
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumedItemsScreen(
    state: ConsumedItemsUiState,
    onAction: (ConsumedItemsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Produk Dikonsumsi",
                        style = typography.sectionTitle,
                        color = colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(ConsumedItemsAction.Back) }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Kembali",
                            tint = colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                ),
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(ConsumedItemsAction.Refresh) },
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            indicator = {},
        ) {
            when (state.loadState) {
                ConsumedItemsLoadState.Loading -> {
                    ConsumedItemsSkeletonContent(
                        modifier = Modifier.padding(horizontal = spacing.screen),
                    )
                }

                is ConsumedItemsLoadState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(spacing.screen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.loadState.message,
                            style = typography.body,
                            color = colors.critical,
                        )
                    }
                }

                ConsumedItemsLoadState.Content -> {
                    if (state.items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(spacing.screen),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Belum ada produk yang dikonsumsi",
                                style = typography.body,
                                color = colors.textSecondary,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = spacing.screen),
                            verticalArrangement = Arrangement.spacedBy(spacing.md),
                        ) {
                            item { Spacer(Modifier.height(spacing.sm)) }
                            items(items = state.items, key = { it.id }) { item ->
                                ExpiryItemCard(
                                    item = item,
                                    onClick = {},
                                    consumed = true,
                                )
                            }
                            item { Spacer(Modifier.height(spacing.xxl)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumedItemsSkeletonContent(modifier: Modifier = Modifier) {
    val base = TickrTheme.colors.textSecondary
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing

    val transition = rememberInfiniteTransition(label = "consumed_shimmer")
    val translateX by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "consumed_shimmer_x",
    )
    val shimmer = Brush.linearGradient(
        colors = listOf(base.copy(0.07f), base.copy(0.20f), base.copy(0.07f)),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 600f, 0f),
    )

    Column(modifier = modifier) {
        Spacer(Modifier.height(spacing.sm))
        repeat(6) { idx ->
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
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(TickrCornerRadius.thumbnail))
                        .background(shimmer),
                )
                Column(Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(shimmer),
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.50f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(shimmer),
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.60f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(shimmer),
                    )
                }
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(TickrCornerRadius.pill))
                        .background(shimmer),
                )
            }
            if (idx < 5) Spacer(Modifier.height(spacing.md))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ConsumedItemsScreenPreview() {
    TickrTheme {
        ConsumedItemsScreen(
            state = ConsumedItemsUiState(
                loadState = ConsumedItemsLoadState.Content,
                items = listOf(
                    ExpiringItem(1, "Susu UHT", 1, "Makanan & Minuman", "#0D6759", "2026-06-01", null, 2, "Pcs", -5, Urgency.CRITICAL),
                    ExpiringItem(2, "Vitamin C", 2, "Obat & Vitamin", "#FA9A08", "2026-06-10", null, 1, "Botol", 3, Urgency.WARNING),
                ),
            ),
            onAction = {},
        )
    }
}
