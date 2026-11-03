package com.project.tickr.domain

import com.project.tickr.domain.insight.InsightCalculator
import com.project.tickr.domain.model.AchievementId
import com.project.tickr.domain.model.Category
import com.project.tickr.domain.model.InsightLevel
import com.project.tickr.domain.model.InsightTip
import com.project.tickr.domain.model.Item
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InsightCalculatorTest {

    // Hari ini = Rabu, 15 Juli 2026. Zona UTC supaya tanggal dari timestamp tidak bergeser.
    private val today = LocalDate(2026, 7, 15)
    private val utc = TimeZone.UTC

    private val food = Category(1, "Makanan", "ic", "#0D6759", "", key = "food")
    private val beauty = Category(2, "Kecantikan", "ic", "#FA9A08", "", key = "beauty")
    private var nextId = 1L

    private fun item(
        expiry: String,
        consumed: Boolean = false,
        categoryId: Long? = 1,
        created: String = "2026-07-01T08:00:00Z",
        updated: String = created,
        name: String = "Barang ${nextId}",
    ) = Item(
        id = nextId++, categoryId = categoryId, name = name, barcode = null, expiryDate = expiry, imageUrl = null,
        notes = null, isConsumed = consumed, createdAt = created, updatedAt = updated,
    )

    private fun compute(vararg items: Item) =
        InsightCalculator.compute(items.toList(), listOf(food, beauty), today, utc)

    @Test
    fun no_items_gives_empty_insight() {
        val data = compute()
        assertTrue(data.isEmpty)
        assertNull(data.saveScore)
    }

    @Test
    fun counts_and_save_score() {
        val data = compute(
            item("2026-07-20", consumed = true),
            item("2026-07-10", consumed = true),
            item("2026-07-12", consumed = true),                  // dikonsumsi
            item("2026-07-05"),                                   // terbuang (lewat, belum dikonsumsi)
            item("2026-07-30"),                                   // aktif
        )
        assertEquals(5, data.totalTracked)
        assertEquals(3, data.consumed)
        assertEquals(1, data.wasted)
        assertEquals(1, data.active)
        assertEquals(75, data.saveScore) // 3 / (3 + 1)
        assertEquals(InsightLevel.GOOD, data.level)
    }

    @Test
    fun score_is_null_until_something_is_finished() {
        val data = compute(item("2026-08-30"))
        assertNull(data.saveScore)
        assertNull(data.level)
    }

    @Test
    fun level_thresholds() {
        assertEquals(InsightLevel.MASTER, InsightCalculator.levelFor(90))
        assertEquals(InsightLevel.GOOD, InsightCalculator.levelFor(89))
        assertEquals(InsightLevel.GOOD, InsightCalculator.levelFor(70))
        assertEquals(InsightLevel.FAIR, InsightCalculator.levelFor(69))
        assertEquals(InsightLevel.FAIR, InsightCalculator.levelFor(40))
        assertEquals(InsightLevel.NEEDS_WORK, InsightCalculator.levelFor(39))
    }

    @Test
    fun item_expiring_today_is_still_active_not_wasted() {
        val data = compute(item("2026-07-15"))
        assertEquals(1, data.active)
        assertEquals(0, data.wasted)
    }

    @Test
    fun monthly_buckets_cover_six_months_ending_this_month() {
        val data = compute(
            item("2026-07-30", consumed = true, updated = "2026-07-10T09:00:00Z"), // dikonsumsi Juli
            item("2026-06-20", consumed = true, updated = "2026-06-18T09:00:00Z"), // dikonsumsi Juni
            item("2026-06-28"),                                                    // terbuang Juni
            item("2026-02-14"),                                                    // terbuang Februari (bulan paling awal di jendela 6 bulan)
        )
        assertEquals(listOf(2, 3, 4, 5, 6, 7), data.monthly.map { it.month })
        assertEquals(2026, data.monthly.first().year)
        val june = data.monthly[4]
        val july = data.monthly[5]
        assertEquals(1, june.consumed); assertEquals(1, june.wasted)
        assertEquals(1, july.consumed); assertEquals(0, july.wasted)
        assertEquals(1, data.monthly.first().wasted)
        assertEquals(1, data.lastMonthWasted)
        assertEquals(0, data.thisMonthWasted)
    }

    @Test
    fun monthly_window_wraps_over_year_boundary() {
        val data = InsightCalculator.compute(emptyList<Item>() + item("2026-01-10"), listOf(food), LocalDate(2026, 2, 3), utc)
        assertEquals(listOf(9, 10, 11, 12, 1, 2), data.monthly.map { it.month })
        assertEquals(listOf(2025, 2025, 2025, 2025, 2026, 2026), data.monthly.map { it.year })
    }

    @Test
    fun streak_counts_days_since_last_wasted_item() {
        val data = compute(item("2026-07-05"), item("2026-06-01"))
        assertEquals(10, data.streakDays) // sejak 5 Juli
        assertTrue(data.hasEverWasted)
    }

    @Test
    fun streak_without_any_waste_counts_from_first_tracked_day() {
        val data = compute(item("2026-08-30", created = "2026-07-05T08:00:00Z"))
        assertEquals(10, data.streakDays)
        assertEquals(false, data.hasEverWasted)
    }

    @Test
    fun habit_averages() {
        val data = compute(
            // dibuat 1 Jul, habis 5 Jul (4 hari), kedaluwarsa 9 Jul (4 hari sebelumnya)
            item("2026-07-09", consumed = true, created = "2026-07-01T08:00:00Z", updated = "2026-07-05T08:00:00Z"),
            // dibuat 1 Jul, habis 7 Jul (6 hari), kedaluwarsa 8 Jul (1 hari sebelumnya)
            item("2026-07-08", consumed = true, created = "2026-07-01T08:00:00Z", updated = "2026-07-07T08:00:00Z"),
        )
        assertEquals(5, data.avgDaysToConsume)
        assertEquals(3, data.avgDaysBeforeExpiry) // (4 + 1) / 2 = 2,5 → 3
    }

    @Test
    fun consuming_after_expiry_counts_as_zero_days_before() {
        val data = compute(item("2026-07-05", consumed = true, updated = "2026-07-08T08:00:00Z"))
        assertEquals(0, data.avgDaysBeforeExpiry)
        assertEquals(1, data.consumed)
        assertEquals(0, data.wasted)
    }

    @Test
    fun urgency_breakdown_and_upcoming_weeks() {
        val data = compute(
            item("2026-07-10"),                 // lewat
            item("2026-07-15"), item("2026-07-18"), // kritis: hari ini & +3
            item("2026-07-19"), item("2026-07-22"), // peringatan: +4 & +7
            item("2026-07-23"),                 // aman: +8
            item("2026-08-05"),                 // aman: +21 → minggu ke-4
            item("2026-09-30"),                 // aman, di luar 4 minggu
        )
        assertEquals(1, data.urgency.overdue)
        assertEquals(2, data.urgency.critical)
        assertEquals(2, data.urgency.warning)
        assertEquals(3, data.urgency.safe)
        assertEquals(listOf(3, 2, 0, 1), data.upcomingWeeks)
    }

    @Test
    fun urgent_items_are_the_three_soonest_within_a_week() {
        val data = compute(
            item("2026-07-22", name = "D"), item("2026-07-16", name = "B"),
            item("2026-07-15", name = "A"), item("2026-07-18", name = "C"),
            item("2026-07-30", name = "Jauh"),
        )
        assertEquals(listOf("A", "B", "C"), data.urgentItems.map { it.name })
        assertEquals(listOf(0, 1, 3), data.urgentItems.map { it.daysLeft })
    }

    @Test
    fun category_stats_and_waste_rate() {
        val data = compute(
            item("2026-07-30", consumed = true, categoryId = 1),
            item("2026-07-05", categoryId = 1),            // terbuang
            item("2026-07-05", categoryId = 1),            // terbuang
            item("2026-07-30", categoryId = 2),            // aktif
            item("2026-07-30", categoryId = null),         // tanpa kategori: tidak masuk daftar kategori
        )
        assertEquals(2, data.categories.size)
        val foodStats = data.categories.first { it.categoryId == 1L }
        assertEquals(1, foodStats.consumed); assertEquals(2, foodStats.wasted)
        assertEquals(66, foodStats.wasteRate) // 2 / 3
        assertNull(data.categories.first { it.categoryId == 2L }.wasteRate)
    }

    @Test
    fun weekday_adds_and_top_weekday_need_enough_data() {
        // 6 Jul 2026 = Senin. Lima barang dicatat di Senin → hari teratas.
        val mondays = List(5) { item("2026-08-30", created = "2026-07-06T08:00:00Z") }
        val data = InsightCalculator.compute(mondays + item("2026-08-30", created = "2026-07-08T08:00:00Z"), listOf(food), today, utc)
        assertEquals(listOf(5, 0, 1, 0, 0, 0, 0), data.weekdayAdds)
        assertEquals(1, data.topWeekday)

        val few = compute(item("2026-08-30", created = "2026-07-06T08:00:00Z"))
        assertNull(few.topWeekday)
    }

    @Test
    fun tips_flag_due_soon_and_problem_category() {
        val data = compute(
            item("2026-07-16"),                                      // kritis
            item("2026-07-30", consumed = true, categoryId = 2),
            item("2026-07-05", categoryId = 2), item("2026-07-06", categoryId = 2), // beauty 2 dari 3 terbuang
        )
        assertTrue(data.tips.any { it is InsightTip.DueSoon && it.count == 1 })
        val top = data.tips.filterIsInstance<InsightTip.TopWasteCategory>().single()
        assertEquals("beauty", top.categoryKey)
        assertEquals(66, top.wasteRate)
    }

    @Test
    fun tips_compare_waste_with_last_month() {
        val worse = compute(
            item("2026-06-10"),                          // terbuang Juni (1)
            item("2026-07-01"), item("2026-07-02"),      // terbuang Juli (2)
        )
        assertEquals(100, worse.tips.filterIsInstance<InsightTip.WasteRising>().single().percent)

        val better = compute(
            item("2026-06-10"), item("2026-06-11"), item("2026-06-12"), item("2026-06-13"), // Juni 4
            item("2026-07-01"),                                                              // Juli 1
        )
        assertEquals(75, better.tips.filterIsInstance<InsightTip.WasteFalling>().single().percent)
    }

    @Test
    fun there_is_always_at_least_one_tip() {
        // Dibuat hari ini → streak 0, jadi tidak ada saran khusus.
        assertEquals(listOf(InsightTip.KeepGoing), compute(item("2026-12-30", created = "2026-07-15T08:00:00Z")).tips)
    }

    @Test
    fun achievements_unlock_from_progress() {
        val data = compute(
            item("2026-07-30", consumed = true, categoryId = 1),
            item("2026-07-30", consumed = true, categoryId = 2),
            item("2026-07-30", consumed = true, categoryId = 1),
        )
        val byId = data.achievements.associateBy { it.id }
        assertTrue(byId.getValue(AchievementId.FIRST_ITEM).unlocked)
        assertTrue(!byId.getValue(AchievementId.TRACKER_10).unlocked)
        assertEquals(3, byId.getValue(AchievementId.SAVER_10).progress)
        assertTrue(byId.getValue(AchievementId.ZERO_WASTE_MONTH).unlocked) // 3 dikonsumsi bulan ini, 0 terbuang
        assertEquals(2, byId.getValue(AchievementId.ORGANIZER).progress)
    }

    @Test
    fun zero_waste_month_resets_when_something_wasted_this_month() {
        val data = compute(
            item("2026-07-30", consumed = true), item("2026-07-30", consumed = true),
            item("2026-07-30", consumed = true), item("2026-07-05"),
        )
        assertEquals(0, data.achievements.first { it.id == AchievementId.ZERO_WASTE_MONTH }.progress)
    }

    @Test
    fun items_with_unparseable_expiry_are_ignored() {
        val data = compute(item("bukan-tanggal"), item("2026-07-30"))
        assertEquals(1, data.totalTracked)
    }
}
