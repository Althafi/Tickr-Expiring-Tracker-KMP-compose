package com.project.tickr.domain.reminder

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus

/** Satu jadwal pengingat: [daysBefore] hari sebelum kedaluwarsa, pada pukul [ReminderRules.HOUR] waktu lokal. */
data class ReminderSlot(val daysBefore: Int, val at: LocalDateTime)

/**
 * Aturan pengingat: H-7, H-3, H-1, dan hari-H pukul 08:00 waktu lokal.
 * Selaras dengan ambang `Urgency` (≤3 hari kritis, 4–7 waspada).
 */
object ReminderRules {
    val DAYS_BEFORE = listOf(7, 3, 1, 0)
    const val HOUR = 8

    fun slots(expiry: LocalDate): List<ReminderSlot> = DAYS_BEFORE.map { days ->
        ReminderSlot(days, LocalDateTime(expiry.minus(days, DateTimeUnit.DAY), LocalTime(HOUR, 0)))
    }

    /** Pengingat yang waktunya BELUM lewat (strictly setelah [now]). */
    fun upcoming(expiry: LocalDate, now: LocalDateTime): List<ReminderSlot> =
        slots(expiry).filter { it.at > now }

    /**
     * Pengingat yang sudah jatuh tempo ([now] sudah melewati waktunya) tapi dibuat SETELAH barang dicatat
     * ([createdAt]); pengingat yang waktunya mendahului pencatatan tidak pernah berlaku untuk barang itu.
     */
    fun due(expiry: LocalDate, createdAt: LocalDateTime, now: LocalDateTime): List<ReminderSlot> =
        slots(expiry).filter { it.at <= now && it.at >= createdAt }
}
