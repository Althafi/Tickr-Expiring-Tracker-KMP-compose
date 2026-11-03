package com.project.tickr.domain.model

/** Seluruh angka layar Wawasan. Dihitung dari data lokal saja, tanpa internet. */
data class InsightData(
    val totalTracked: Int,
    val active: Int,
    val consumed: Int,
    val wasted: Int,
    /** Persen barang yang habis dikonsumsi dibanding yang terbuang (0–100); null bila belum ada keduanya. */
    val saveScore: Int?,
    val level: InsightLevel?,
    /** 6 bulan terakhir, terlama dulu; bulan ini terakhir. */
    val monthly: List<MonthBucket>,
    val thisMonthConsumed: Int,
    val thisMonthWasted: Int,
    val lastMonthWasted: Int,
    /** Hari berturut-turut tanpa barang terbuang; null bila belum ada barang sama sekali. */
    val streakDays: Int?,
    val hasEverWasted: Boolean,
    val avgDaysToConsume: Int?,
    /** Rata-rata sisa hari sebelum kedaluwarsa saat barang dikonsumsi (0 = tepat di hari-H). */
    val avgDaysBeforeExpiry: Int?,
    val urgency: UrgencyBreakdown,
    /** Jumlah barang aktif yang kedaluwarsa di minggu ke-1 sampai ke-4 dari hari ini. */
    val upcomingWeeks: List<Int>,
    val urgentItems: List<UrgentItem>,
    val categories: List<CategoryInsight>,
    /** Jumlah barang yang dicatat per hari, indeks 0 = Senin … 6 = Minggu. */
    val weekdayAdds: List<Int>,
    val topWeekday: Int?,
    val tips: List<InsightTip>,
    val achievements: List<Achievement>,
) {
    val isEmpty: Boolean get() = totalTracked == 0

    companion object {
        val EMPTY = InsightData(
            totalTracked = 0, active = 0, consumed = 0, wasted = 0, saveScore = null, level = null,
            monthly = emptyList(), thisMonthConsumed = 0, thisMonthWasted = 0, lastMonthWasted = 0,
            streakDays = null, hasEverWasted = false, avgDaysToConsume = null, avgDaysBeforeExpiry = null,
            urgency = UrgencyBreakdown(0, 0, 0, 0), upcomingWeeks = listOf(0, 0, 0, 0), urgentItems = emptyList(),
            categories = emptyList(), weekdayAdds = List(7) { 0 }, topWeekday = null,
            tips = emptyList(), achievements = emptyList(),
        )
    }
}

enum class InsightLevel { MASTER, GOOD, FAIR, NEEDS_WORK }

data class MonthBucket(val year: Int, val month: Int, val consumed: Int, val wasted: Int)

/** Sebaran barang aktif menurut sisa waktunya, ditambah yang sudah lewat tanpa dikonsumsi. */
data class UrgencyBreakdown(val overdue: Int, val critical: Int, val warning: Int, val safe: Int) {
    val total: Int get() = overdue + critical + warning + safe
}

data class UrgentItem(val id: Long, val name: String, val daysLeft: Int)

data class CategoryInsight(
    val categoryId: Long,
    val key: String?,
    val name: String,
    val colorHex: String,
    val consumed: Int,
    val wasted: Int,
    val active: Int,
) {
    val total: Int get() = consumed + wasted + active
    /** Persen terbuang dari barang yang sudah selesai (dikonsumsi atau terbuang); null bila belum ada. */
    val wasteRate: Int? get() = (consumed + wasted).takeIf { it > 0 }?.let { wasted * 100 / it }
}

sealed interface InsightTip {
    data object StartTracking : InsightTip
    data class DueSoon(val count: Int) : InsightTip
    data class ManyExpiringThisWeek(val count: Int) : InsightTip
    data class TopWasteCategory(val categoryKey: String?, val categoryName: String, val wasteRate: Int) : InsightTip
    data class WasteRising(val percent: Int) : InsightTip
    data class WasteFalling(val percent: Int) : InsightTip
    data class ZeroWasteStreak(val days: Int) : InsightTip
    data class EatEarlier(val days: Int) : InsightTip
    /** [weekday] 1 = Senin … 7 = Minggu. */
    data class ShopDay(val weekday: Int) : InsightTip
    data object KeepGoing : InsightTip
}

enum class AchievementId {
    FIRST_ITEM, TRACKER_10, SAVER_10, SAVER_50, ZERO_WASTE_MONTH, STREAK_7, STREAK_30, ORGANIZER,
}

data class Achievement(val id: AchievementId, val progress: Int, val target: Int) {
    val unlocked: Boolean get() = progress >= target
}
