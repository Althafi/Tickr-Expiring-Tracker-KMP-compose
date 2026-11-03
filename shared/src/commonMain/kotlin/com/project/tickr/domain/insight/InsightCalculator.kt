package com.project.tickr.domain.insight

import com.project.tickr.domain.model.Achievement
import com.project.tickr.domain.model.AchievementId
import com.project.tickr.domain.model.Category
import com.project.tickr.domain.model.CategoryInsight
import com.project.tickr.domain.model.InsightData
import com.project.tickr.domain.model.InsightLevel
import com.project.tickr.domain.model.InsightTip
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.model.MonthBucket
import com.project.tickr.domain.model.UrgencyBreakdown
import com.project.tickr.domain.model.UrgentItem
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Menghitung semua angka layar Wawasan dari daftar barang. Fungsi murni: tanpa database, tanpa internet.
 *
 * Definisi:
 * - **Dikonsumsi**: `isConsumed`. Tanggalnya diambil dari `updatedAt` (saat ditandai habis).
 * - **Terbuang**: belum dikonsumsi dan tanggal kedaluwarsanya sudah lewat; jatuh di bulan kedaluwarsanya.
 * - **Aktif**: belum dikonsumsi dan belum lewat.
 */
object InsightCalculator {

    private const val MONTHS_SHOWN = 6

    fun compute(
        items: List<Item>,
        categories: List<Category>,
        today: LocalDate,
        zone: TimeZone = TimeZone.currentSystemDefault(),
    ): InsightData {
        val rows = items.mapNotNull { Row.from(it, today, zone) }
        if (rows.isEmpty()) return InsightData.EMPTY

        val consumedRows = rows.filter { it.item.isConsumed }
        val wastedRows = rows.filter { !it.item.isConsumed && it.daysLeft < 0 }
        val activeRows = rows.filter { !it.item.isConsumed && it.daysLeft >= 0 }

        val consumed = consumedRows.size
        val wasted = wastedRows.size
        val saveScore = (consumed + wasted).takeIf { it > 0 }?.let { consumed * 100 / it }

        // ── Tren bulanan ────────────────────────────────────────────
        val firstOfThisMonth = LocalDate(today.year, today.month.number, 1)
        val monthly = (MONTHS_SHOWN - 1 downTo 0).map { back ->
            val start = firstOfThisMonth.minus(back, DateTimeUnit.MONTH)
            MonthBucket(
                year = start.year,
                month = start.month.number,
                consumed = consumedRows.count { it.consumedOn.sameMonth(start) },
                wasted = wastedRows.count { it.expiry.sameMonth(start) },
            )
        }
        val thisMonth = monthly.last()
        val lastMonth = monthly[monthly.lastIndex - 1]

        // ── Streak tanpa barang terbuang ────────────────────────────
        val lastWasteDate = wastedRows.maxOfOrNull { it.expiry }
        val firstTrackedDate = rows.minOf { it.createdOn }
        val streakDays = (lastWasteDate ?: firstTrackedDate).daysUntil(today).coerceAtLeast(0)

        // ── Kebiasaan ───────────────────────────────────────────────
        val avgDaysToConsume = consumedRows
            .map { it.createdOn.daysUntil(it.consumedOn).coerceAtLeast(0) }
            .averageRounded()
        val avgDaysBeforeExpiry = consumedRows
            .map { it.consumedOn.daysUntil(it.expiry).coerceAtLeast(0) }
            .averageRounded()

        // ── Sisa waktu barang aktif ─────────────────────────────────
        val urgency = UrgencyBreakdown(
            overdue = wasted,
            critical = activeRows.count { it.daysLeft <= 3 },
            warning = activeRows.count { it.daysLeft in 4..7 },
            safe = activeRows.count { it.daysLeft > 7 },
        )
        val upcomingWeeks = List(4) { week -> activeRows.count { it.daysLeft in (week * 7)..(week * 7 + 6) } }
        val urgentItems = activeRows
            .filter { it.daysLeft <= 7 }
            .sortedWith(compareBy({ it.daysLeft }, { it.item.id }))
            .take(3)
            .map { UrgentItem(it.item.id, it.item.name, it.daysLeft) }

        // ── Per kategori ────────────────────────────────────────────
        val categoryInsights = categories.mapNotNull { cat ->
            val mine = rows.filter { it.item.categoryId == cat.id }
            if (mine.isEmpty()) null else CategoryInsight(
                categoryId = cat.id,
                key = cat.key,
                name = cat.name,
                colorHex = cat.colorHex,
                consumed = mine.count { it.item.isConsumed },
                wasted = mine.count { !it.item.isConsumed && it.daysLeft < 0 },
                active = mine.count { !it.item.isConsumed && it.daysLeft >= 0 },
            )
        }.sortedWith(compareByDescending<CategoryInsight> { it.total }.thenBy { it.categoryId })

        // ── Hari pencatatan ─────────────────────────────────────────
        val weekdayAdds = MutableList(7) { 0 }
        rows.forEach { weekdayAdds[it.createdOn.dayOfWeek.isoDayNumber - 1]++ }
        val topWeekday = weekdayAdds.max().takeIf { rows.size >= 5 && it > 0 }
            ?.let { max -> weekdayAdds.indexOf(max) + 1 }

        return InsightData(
            totalTracked = rows.size,
            active = activeRows.size,
            consumed = consumed,
            wasted = wasted,
            saveScore = saveScore,
            level = saveScore?.let(::levelFor),
            monthly = monthly,
            thisMonthConsumed = thisMonth.consumed,
            thisMonthWasted = thisMonth.wasted,
            lastMonthWasted = lastMonth.wasted,
            streakDays = streakDays,
            hasEverWasted = wasted > 0,
            avgDaysToConsume = avgDaysToConsume,
            avgDaysBeforeExpiry = avgDaysBeforeExpiry,
            urgency = urgency,
            upcomingWeeks = upcomingWeeks,
            urgentItems = urgentItems,
            categories = categoryInsights,
            weekdayAdds = weekdayAdds,
            topWeekday = topWeekday,
            tips = buildTips(
                urgency = urgency,
                dueThisWeek = upcomingWeeks[0],
                categories = categoryInsights,
                thisMonthWasted = thisMonth.wasted,
                lastMonthWasted = lastMonth.wasted,
                streakDays = streakDays,
                consumed = consumed,
                avgDaysBeforeExpiry = avgDaysBeforeExpiry,
                topWeekday = topWeekday,
            ),
            achievements = buildAchievements(
                total = rows.size,
                consumed = consumed,
                thisMonthConsumed = thisMonth.consumed,
                thisMonthWasted = thisMonth.wasted,
                streakDays = streakDays,
                categoriesUsed = categoryInsights.size,
            ),
        )
    }

    fun levelFor(score: Int): InsightLevel = when {
        score >= 90 -> InsightLevel.MASTER
        score >= 70 -> InsightLevel.GOOD
        score >= 40 -> InsightLevel.FAIR
        else -> InsightLevel.NEEDS_WORK
    }

    private fun buildTips(
        urgency: UrgencyBreakdown,
        dueThisWeek: Int,
        categories: List<CategoryInsight>,
        thisMonthWasted: Int,
        lastMonthWasted: Int,
        streakDays: Int,
        consumed: Int,
        avgDaysBeforeExpiry: Int?,
        topWeekday: Int?,
    ): List<InsightTip> {
        val tips = mutableListOf<InsightTip>()
        if (urgency.critical > 0) tips += InsightTip.DueSoon(urgency.critical)
        else if (dueThisWeek >= 5) tips += InsightTip.ManyExpiringThisWeek(dueThisWeek)

        categories
            .filter { it.consumed + it.wasted >= 3 && (it.wasteRate ?: 0) >= 30 }
            .maxByOrNull { it.wasteRate ?: 0 }
            ?.let { tips += InsightTip.TopWasteCategory(it.key, it.name, it.wasteRate ?: 0) }

        if (lastMonthWasted > 0 && thisMonthWasted > lastMonthWasted) {
            tips += InsightTip.WasteRising((thisMonthWasted - lastMonthWasted) * 100 / lastMonthWasted)
        } else if (lastMonthWasted > 0 && thisMonthWasted < lastMonthWasted) {
            tips += InsightTip.WasteFalling((lastMonthWasted - thisMonthWasted) * 100 / lastMonthWasted)
        }

        if (streakDays >= 7) tips += InsightTip.ZeroWasteStreak(streakDays)
        if (consumed >= 3 && avgDaysBeforeExpiry != null && avgDaysBeforeExpiry <= 1) {
            tips += InsightTip.EatEarlier(avgDaysBeforeExpiry)
        }
        topWeekday?.let { tips += InsightTip.ShopDay(it) }

        return tips.take(4).ifEmpty { listOf(InsightTip.KeepGoing) }
    }

    private fun buildAchievements(
        total: Int,
        consumed: Int,
        thisMonthConsumed: Int,
        thisMonthWasted: Int,
        streakDays: Int,
        categoriesUsed: Int,
    ): List<Achievement> = listOf(
        Achievement(AchievementId.FIRST_ITEM, total.coerceAtMost(1), 1),
        Achievement(AchievementId.TRACKER_10, total.coerceAtMost(10), 10),
        Achievement(AchievementId.SAVER_10, consumed.coerceAtMost(10), 10),
        Achievement(AchievementId.SAVER_50, consumed.coerceAtMost(50), 50),
        Achievement(
            AchievementId.ZERO_WASTE_MONTH,
            if (thisMonthWasted > 0) 0 else thisMonthConsumed.coerceAtMost(3),
            3,
        ),
        Achievement(AchievementId.STREAK_7, streakDays.coerceAtMost(7), 7),
        Achievement(AchievementId.STREAK_30, streakDays.coerceAtMost(30), 30),
        Achievement(AchievementId.ORGANIZER, categoriesUsed.coerceAtMost(3), 3),
    )

    private fun LocalDate.sameMonth(other: LocalDate) = year == other.year && month == other.month

    private fun List<Int>.averageRounded(): Int? =
        if (isEmpty()) null else ((sum() * 2 + size) / (size * 2)) // pembulatan ke bilangan bulat terdekat

    private class Row(
        val item: Item,
        val expiry: LocalDate,
        val createdOn: LocalDate,
        val consumedOn: LocalDate,
        val daysLeft: Int,
    ) {
        companion object {
            fun from(item: Item, today: LocalDate, zone: TimeZone): Row? {
                val expiry = runCatching { LocalDate.parse(item.expiryDate) }.getOrNull() ?: return null
                val created = timestampToDate(item.createdAt, zone) ?: today
                val consumedOn = timestampToDate(item.updatedAt, zone) ?: created
                return Row(item, expiry, created, consumedOn, today.daysUntil(expiry))
            }

            /** Timestamp ISO-8601 UTC → tanggal lokal; format lain dibaca dari 10 karakter pertama. */
            private fun timestampToDate(value: String, zone: TimeZone): LocalDate? =
                runCatching { Instant.parse(value).toLocalDateTime(zone).date }.getOrNull()
                    ?: runCatching { LocalDate.parse(value.take(10)) }.getOrNull()
        }
    }
}
