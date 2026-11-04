package com.project.tickr.ui.screen.insight

import com.project.tickr.ui.common.TickrIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.Achievement
import com.project.tickr.domain.model.AchievementId
import com.project.tickr.domain.model.CategoryInsight
import com.project.tickr.domain.model.InsightData
import com.project.tickr.domain.model.InsightLevel
import com.project.tickr.domain.model.InsightTip
import com.project.tickr.domain.model.Urgency
import com.project.tickr.domain.model.UrgentItem
import com.project.tickr.domain.model.daysToUrgency
import com.project.tickr.ui.i18n.categoryLabel
import com.project.tickr.ui.i18n.countdownText
import com.project.tickr.ui.i18n.weekdayLabel
import com.project.tickr.ui.theme.DonutCategoryColors
import com.project.tickr.ui.theme.TickrColors
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tickrapp.shared.generated.resources.*

// ——— Kerangka kartu ———

@Composable
internal fun InsightCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.card))
            .background(colors.surface)
            .border(1.dp, colors.textSecondary.copy(alpha = 0.12f), RoundedCornerShape(TickrCornerRadius.card))
            .padding(spacing.cardInner),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        if (title != null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = typography.sectionTitle, color = colors.textPrimary, modifier = Modifier.weight(1f))
                trailing?.invoke()
            }
        }
        content()
    }
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color, background: Color, size: Int = 36) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.55f).dp))
    }
}

@Composable
private fun Dot(color: Color) {
    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
}

@Composable
private fun LegendItem(color: Color, label: String, value: Int? = null) {
    val typography = TickrTheme.typography
    val colors = TickrTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Dot(color)
        Text(
            text = if (value != null) "$label · $value" else label,
            style = typography.caption,
            color = colors.textSecondary,
        )
    }
}

// ——— 1. Skor hemat ———

@Composable
internal fun ScoreHeroCard(data: InsightData, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val score = data.saveScore
    val levelText = data.level?.let { stringResource(levelLabel(it)) }
    val ringColor = when (data.level) {
        InsightLevel.MASTER, InsightLevel.GOOD -> colors.safe
        InsightLevel.FAIR -> colors.warning
        InsightLevel.NEEDS_WORK -> colors.critical
        null -> colors.textSecondary
    }
    val description = if (score != null && levelText != null) {
        stringResource(Res.string.insight_cd_score, score, levelText)
    } else stringResource(Res.string.insight_score_none)

    InsightCard(modifier = modifier.semantics { contentDescription = description }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ScoreRing(score = score ?: 0, color = ringColor) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = score?.toString() ?: "–",
                        style = typography.displayDate.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.textPrimary,
                    )
                    Text(text = "/ 100", style = typography.caption, color = colors.textSecondary)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(Res.string.insight_score_title),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
                Text(
                    text = levelText ?: stringResource(Res.string.insight_level_none),
                    style = typography.sectionTitle,
                    color = if (score != null) ringColor else colors.textPrimary,
                )
                Text(
                    text = if (score != null) {
                        stringResource(Res.string.insight_score_summary, data.consumed, data.wasted)
                    } else stringResource(Res.string.insight_score_none),
                    style = typography.body,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

private fun levelLabel(level: InsightLevel): StringResource = when (level) {
    InsightLevel.MASTER -> Res.string.insight_level_master
    InsightLevel.GOOD -> Res.string.insight_level_good
    InsightLevel.FAIR -> Res.string.insight_level_fair
    InsightLevel.NEEDS_WORK -> Res.string.insight_level_needs_work
}

// ——— 2. Ringkasan angka ———

@Composable
internal fun StatsGrid(data: InsightData, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                value = data.totalTracked, label = stringResource(Res.string.insight_stat_tracked),
                icon = TickrIcons.Inventory2, tint = colors.primaryBrand, container = colors.primaryBrand.copy(alpha = 0.1f),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = data.active, label = stringResource(Res.string.insight_stat_active),
                icon = TickrIcons.Schedule, tint = colors.warning, container = colors.warningContainer,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                value = data.consumed, label = stringResource(Res.string.insight_stat_consumed),
                icon = TickrIcons.Check, tint = colors.safe, container = colors.safeContainer,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = data.wasted, label = stringResource(Res.string.insight_stat_wasted),
                icon = TickrIcons.Delete, tint = colors.critical, container = colors.criticalContainer,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatTile(
    value: Int,
    label: String,
    icon: ImageVector,
    tint: Color,
    container: Color,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(TickrCornerRadius.card))
            .background(colors.surface)
            .border(1.dp, colors.textSecondary.copy(alpha = 0.12f), RoundedCornerShape(TickrCornerRadius.card))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconBadge(icon, tint, container, size = 40)
        Column {
            Text(text = value.toString(), style = typography.donutTotalNum, color = colors.textPrimary)
            Text(text = label, style = typography.caption, color = colors.textSecondary)
        }
    }
}

// ——— 3. Tren bulanan ———

@Composable
internal fun TrendCard(data: InsightData, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val chartDescription = stringResource(Res.string.insight_cd_trend)
    InsightCard(modifier = modifier, title = stringResource(Res.string.insight_trend_title)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(colors.safe, stringResource(Res.string.insight_stat_consumed))
            LegendItem(colors.critical, stringResource(Res.string.insight_stat_wasted))
        }
        MonthlyTrendChart(
            months = data.monthly,
            consumedColor = colors.safe,
            wastedColor = colors.critical,
            modifier = Modifier.semantics { contentDescription = chartDescription },
        )
        TrendMessage(data)
    }
}

@Composable
private fun TrendMessage(data: InsightData) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val this_ = data.thisMonthWasted
    val last = data.lastMonthWasted
    val improving = this_ <= last
    val text = when {
        this_ == 0 && last == 0 -> stringResource(Res.string.insight_trend_clean)
        last == 0 -> stringResource(Res.string.insight_trend_first_waste, this_)
        this_ == 0 -> stringResource(Res.string.insight_trend_cleaner, last)
        this_ == last -> stringResource(Res.string.insight_trend_same)
        this_ < last -> stringResource(Res.string.insight_trend_better, "${(last - this_) * 100 / last}%")
        else -> stringResource(Res.string.insight_trend_worse, "${(this_ - last) * 100 / last}%")
    }
    val tint = if (improving) colors.safe else colors.critical
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.button))
            .background(if (improving) colors.safeContainer else colors.criticalContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            if (improving) TickrIcons.TrendingDown else TickrIcons.TrendingUp,
            contentDescription = null, tint = tint, modifier = Modifier.size(20.dp),
        )
        Text(text = text, style = typography.body, color = colors.textPrimary)
    }
}

// ——— 4. Streak ———

private val STREAK_GOALS = listOf(3, 7, 14, 30, 60, 100, 365)

@Composable
internal fun StreakCard(data: InsightData, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val days = data.streakDays ?: 0
    val goal = STREAK_GOALS.firstOrNull { it > days } ?: (days + 1)
    val accent = colors.secondaryAccent

    InsightCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconBadge(TickrIcons.LocalFireDepartment, accent, accent.copy(alpha = 0.15f), size = 52)
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.insight_streak_title),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
                Text(
                    text = pluralStringResource(Res.plurals.countdown_days, days, days),
                    style = typography.greetingTitle,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(
                        if (data.hasEverWasted) Res.string.insight_streak_since else Res.string.insight_streak_never,
                    ),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
        val animated = rememberReveal(days)
        LinearProgressIndicator(
            progress = { (days.toFloat() / goal).coerceIn(0f, 1f) * animated },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
            color = accent,
            trackColor = colors.textSecondary.copy(alpha = 0.12f),
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        Text(
            text = stringResource(Res.string.insight_streak_next, goal),
            style = typography.caption,
            color = colors.textSecondary,
        )
    }
}

// ——— 5. Kondisi barang aktif ———

@Composable
internal fun UrgencyCard(data: InsightData, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val u = data.urgency
    InsightCard(modifier = modifier, title = stringResource(Res.string.insight_urgency_title)) {
        SegmentedBar(
            segments = listOf(
                u.overdue to colors.critical.copy(alpha = 0.55f),
                u.critical to colors.critical,
                u.warning to colors.warning,
                u.safe to colors.safe,
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                LegendItem(colors.critical.copy(alpha = 0.55f), stringResource(Res.string.insight_urgency_overdue), u.overdue)
                LegendItem(colors.critical, stringResource(Res.string.insight_urgency_critical), u.critical)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                LegendItem(colors.warning, stringResource(Res.string.insight_urgency_warning), u.warning)
                LegendItem(colors.safe, stringResource(Res.string.insight_urgency_safe), u.safe)
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.insight_upcoming_title),
            style = typography.body,
            color = colors.textPrimary,
        )
        val maxWeek = data.upcomingWeeks.maxOrNull()?.coerceAtLeast(1) ?: 1
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.upcomingWeeks.forEachIndexed { index, count ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(Res.string.insight_week_label, index + 1),
                        style = typography.caption,
                        color = colors.textSecondary,
                        modifier = Modifier.width(44.dp),
                    )
                    Box(Modifier.weight(1f)) {
                        SegmentedBar(
                            segments = listOf(count to (if (index == 0) colors.warning else colors.primaryBrand)),
                            barHeight = 8.dp,
                            modifier = Modifier.fillMaxWidth(if (count == 0) 1f else (count.toFloat() / maxWeek).coerceAtLeast(0.08f)),
                        )
                    }
                    Text(
                        text = count.toString(),
                        style = typography.caption,
                        color = colors.textPrimary,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

// ——— 6. Perlu segera dipakai ———

@Composable
internal fun UrgentItemsCard(
    items: List<UrgentItem>,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    InsightCard(modifier = modifier, title = stringResource(Res.string.insight_urgent_title)) {
        if (items.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconBadge(TickrIcons.EventAvailable, colors.safe, colors.safeContainer)
                Text(
                    text = stringResource(Res.string.insight_urgent_none),
                    style = typography.body,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            items.forEach { item ->
                val (tint, container) = when (daysToUrgency(item.daysLeft)) {
                    Urgency.CRITICAL -> colors.critical to colors.criticalContainer
                    Urgency.WARNING -> colors.warning to colors.warningContainer
                    Urgency.SAFE -> colors.safe to colors.safeContainer
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TickrCornerRadius.button))
                        .clickable { onItemClick(item.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IconBadge(TickrIcons.Timer, tint, container)
                    Text(
                        text = item.name,
                        style = typography.body,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = countdownText(item.daysLeft),
                        style = typography.countdown,
                        color = tint,
                        modifier = Modifier
                            .clip(RoundedCornerShape(TickrCornerRadius.pill))
                            .background(container)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

// ——— 7. Per kategori ———

@Composable
internal fun CategoryCard(categories: List<CategoryInsight>, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    InsightCard(modifier = modifier, title = stringResource(Res.string.insight_category_title)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(colors.safe, stringResource(Res.string.insight_stat_consumed))
            LegendItem(colors.critical, stringResource(Res.string.insight_stat_wasted))
            LegendItem(colors.textSecondary.copy(alpha = 0.45f), stringResource(Res.string.insight_stat_active))
        }
        categories.forEach { cat ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Dot(DonutCategoryColors.fromHex(cat.colorHex))
                    Text(
                        text = categoryLabel(cat.key, cat.name),
                        style = typography.body,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    val rate = cat.wasteRate
                    Text(
                        text = if (rate != null) stringResource(Res.string.insight_category_waste_rate, "$rate%")
                        else stringResource(Res.string.insight_category_no_finished),
                        style = typography.caption,
                        color = when {
                            rate == null -> colors.textSecondary
                            rate >= 50 -> colors.critical
                            rate >= 25 -> colors.warning
                            else -> colors.safe
                        },
                    )
                }
                SegmentedBar(
                    segments = listOf(
                        cat.consumed to colors.safe,
                        cat.wasted to colors.critical,
                        cat.active to colors.textSecondary.copy(alpha = 0.45f),
                    ),
                    barHeight = 10.dp,
                )
                Text(
                    text = stringResource(Res.string.insight_category_summary, cat.consumed, cat.wasted, cat.active),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

// ——— 8. Kebiasaan ———

@Composable
internal fun HabitsCard(data: InsightData, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    InsightCard(modifier = modifier, title = stringResource(Res.string.insight_habits_title)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HabitTile(
                icon = TickrIcons.Timer,
                label = stringResource(Res.string.insight_habit_finish_time),
                value = data.avgDaysToConsume?.let { pluralStringResource(Res.plurals.countdown_days, it, it) },
                modifier = Modifier.weight(1f),
            )
            HabitTile(
                icon = TickrIcons.EventAvailable,
                label = stringResource(Res.string.insight_habit_before_expiry),
                value = data.avgDaysBeforeExpiry?.let {
                    if (it == 0) stringResource(Res.string.insight_habit_on_the_day)
                    else pluralStringResource(Res.plurals.countdown_days, it, it)
                },
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = stringResource(Res.string.insight_weekday_title),
            style = typography.body,
            color = colors.textPrimary,
        )
        WeekdayChart(counts = data.weekdayAdds, highlightIndex = data.topWeekday?.minus(1))
        data.topWeekday?.let { day ->
            Text(
                text = stringResource(Res.string.insight_weekday_hint, weekdayLabel(day)),
                style = typography.caption,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun HabitTile(icon: ImageVector, label: String, value: String?, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(TickrCornerRadius.button))
            .background(colors.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(20.dp))
        Text(text = value ?: "–", style = typography.sectionTitle, color = colors.textPrimary)
        Text(text = label, style = typography.caption, color = colors.textSecondary)
    }
}

// ——— 9. Saran ———

@Composable
internal fun TipsCard(tips: List<InsightTip>, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    InsightCard(modifier = modifier, title = stringResource(Res.string.insight_tips_title)) {
        tips.forEach { tip ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                IconBadge(TickrIcons.Lightbulb, colors.secondaryAccent, colors.secondaryAccent.copy(alpha = 0.15f))
                Text(
                    text = tipText(tip),
                    style = typography.body,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f).padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun tipText(tip: InsightTip): String = when (tip) {
    InsightTip.StartTracking -> stringResource(Res.string.insight_tip_start)
    InsightTip.KeepGoing -> stringResource(Res.string.insight_tip_keep_going)
    is InsightTip.DueSoon -> stringResource(Res.string.insight_tip_due_soon, tip.count)
    is InsightTip.ManyExpiringThisWeek -> stringResource(Res.string.insight_tip_many_week, tip.count)
    is InsightTip.TopWasteCategory -> stringResource(
        Res.string.insight_tip_top_category, categoryLabel(tip.categoryKey, tip.categoryName), "${tip.wasteRate}%",
    )
    is InsightTip.WasteRising -> stringResource(Res.string.insight_tip_rising, "${tip.percent}%")
    is InsightTip.WasteFalling -> stringResource(Res.string.insight_tip_falling, "${tip.percent}%")
    is InsightTip.ZeroWasteStreak -> stringResource(Res.string.insight_tip_streak, tip.days)
    is InsightTip.EatEarlier -> stringResource(Res.string.insight_tip_eat_earlier)
    is InsightTip.ShopDay -> stringResource(Res.string.insight_tip_shop_day, weekdayLabel(tip.weekday))
}

// ——— 10. Pencapaian ———

@Composable
internal fun AchievementsCard(achievements: List<Achievement>, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val unlocked = achievements.count { it.unlocked }
    InsightCard(
        modifier = modifier,
        title = stringResource(Res.string.insight_achievements_title),
        trailing = {
            Text(
                text = stringResource(Res.string.insight_achievements_count, unlocked, achievements.size),
                style = typography.caption,
                color = colors.textSecondary,
            )
        },
    ) {
        achievements.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { AchievementTile(it, Modifier.weight(1f)) }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AchievementTile(achievement: Achievement, modifier: Modifier = Modifier) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val (name, description) = achievementText(achievement.id)
    val unlocked = achievement.unlocked
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(TickrCornerRadius.button))
            .background(if (unlocked) colors.secondaryAccent.copy(alpha = 0.12f) else colors.background)
            .border(
                1.dp,
                if (unlocked) colors.secondaryAccent.copy(alpha = 0.5f) else colors.textSecondary.copy(alpha = 0.12f),
                RoundedCornerShape(TickrCornerRadius.button),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IconBadge(
            icon = if (unlocked) TickrIcons.EmojiEvents else TickrIcons.Lock,
            tint = if (unlocked) colors.secondaryAccent else colors.textSecondary,
            background = if (unlocked) colors.secondaryAccent.copy(alpha = 0.2f) else colors.textSecondary.copy(alpha = 0.1f),
        )
        Text(
            text = name,
            style = typography.body,
            color = if (unlocked) colors.textPrimary else colors.textSecondary,
        )
        Text(text = description, style = typography.caption, color = colors.textSecondary)
        if (!unlocked) {
            LinearProgressIndicator(
                progress = { achievement.progress.toFloat() / achievement.target },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                color = colors.primaryBrand,
                trackColor = colors.textSecondary.copy(alpha = 0.12f),
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Text(
                text = stringResource(Res.string.insight_achievement_progress, achievement.progress, achievement.target),
                style = typography.caption,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun achievementText(id: AchievementId): Pair<String, String> {
    val (name, desc) = when (id) {
        AchievementId.FIRST_ITEM -> Res.string.ach_first_item to Res.string.ach_first_item_desc
        AchievementId.TRACKER_10 -> Res.string.ach_tracker_10 to Res.string.ach_tracker_10_desc
        AchievementId.SAVER_10 -> Res.string.ach_saver_10 to Res.string.ach_saver_10_desc
        AchievementId.SAVER_50 -> Res.string.ach_saver_50 to Res.string.ach_saver_50_desc
        AchievementId.ZERO_WASTE_MONTH -> Res.string.ach_zero_waste_month to Res.string.ach_zero_waste_month_desc
        AchievementId.STREAK_7 -> Res.string.ach_streak_7 to Res.string.ach_streak_7_desc
        AchievementId.STREAK_30 -> Res.string.ach_streak_30 to Res.string.ach_streak_30_desc
        AchievementId.ORGANIZER -> Res.string.ach_organizer to Res.string.ach_organizer_desc
    }
    return stringResource(name) to stringResource(desc)
}
