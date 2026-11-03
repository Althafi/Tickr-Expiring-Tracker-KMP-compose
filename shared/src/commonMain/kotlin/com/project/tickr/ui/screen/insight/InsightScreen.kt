package com.project.tickr.ui.screen.insight

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.tickr.presentation.insight.InsightAction
import com.project.tickr.presentation.insight.InsightUiState
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import org.jetbrains.compose.resources.stringResource
import tickrapp.shared.generated.resources.*

@Composable
fun InsightScreen(
    state: InsightUiState,
    onAction: (InsightAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = TickrTheme.spacing
    val data = state.data

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screen),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        InsightHeader()

        when {
            state.isLoading -> InsightSkeleton()
            data.isEmpty -> InsightEmptyState(onAddClick = { onAction(InsightAction.AddItemClicked) })
            else -> {
                ScoreHeroCard(data)
                StatsGrid(data)
                TrendCard(data)
                StreakCard(data)
                UrgencyCard(data)
                UrgentItemsCard(data.urgentItems, onItemClick = { onAction(InsightAction.ItemClicked(it)) })
                if (data.categories.isNotEmpty()) CategoryCard(data.categories)
                HabitsCard(data)
                TipsCard(data.tips)
                AchievementsCard(data.achievements)
                Text(
                    text = stringResource(Res.string.insight_footer),
                    style = TickrTheme.typography.caption,
                    color = TickrTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = spacing.sm),
                )
            }
        }
        Spacer(Modifier.height(spacing.xxl))
    }
}

@Composable
private fun InsightHeader() {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = stringResource(Res.string.insight_title), style = typography.greetingTitle, color = colors.textPrimary)
        Text(text = stringResource(Res.string.insight_subtitle), style = typography.body, color = colors.textSecondary)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(TickrCornerRadius.pill))
                .background(colors.safeContainer)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Outlined.OfflinePin, contentDescription = null, tint = colors.safe, modifier = Modifier.size(14.dp))
            Text(
                text = stringResource(Res.string.insight_offline_badge),
                style = typography.caption,
                color = colors.safe,
            )
        }
    }
}

@Composable
private fun InsightSkeleton() {
    val colors = TickrTheme.colors
    listOf(190.dp, 150.dp, 240.dp).forEach { h ->
        Box(
            Modifier
                .fillMaxWidth()
                .height(h)
                .clip(RoundedCornerShape(TickrCornerRadius.card))
                .background(colors.textSecondary.copy(alpha = 0.08f)),
        )
    }
}

@Composable
private fun InsightEmptyState(onAddClick: () -> Unit) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.BarChart, contentDescription = null, tint = colors.textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(72.dp))
        Text(
            text = stringResource(Res.string.insight_empty_title),
            style = typography.sectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.insight_empty_body),
            style = typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand, contentColor = colors.onBrand),
        ) {
            Text(text = stringResource(Res.string.additem_title), style = typography.body)
        }
    }
}
