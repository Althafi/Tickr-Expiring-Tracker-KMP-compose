package com.project.tickr.core.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

expect fun getTodayString(): String
expect fun getNowEpochMillis(): Long

class DateTimeUtilImpl : DateTimeUtil {

    override fun isWithinDays(isoDate: String, days: Int): Boolean = try {
        val date = LocalDate.parse(isoDate)
        val today = LocalDate.parse(getTodayString())
        val endDate = today.plus(DatePeriod(days = days))
        date >= today && date <= endDate
    } catch (_: Exception) { false }

    override fun isPast(isoDate: String): Boolean = try {
        val date = LocalDate.parse(isoDate)
        val today = LocalDate.parse(getTodayString())
        date < today
    } catch (_: Exception) { false }

    override fun today(): String = getTodayString()

    override fun daysUntil(isoDate: String): Int = try {
        val date = LocalDate.parse(isoDate)
        val today = LocalDate.parse(getTodayString())
        (date.toEpochDays() - today.toEpochDays()).toInt()
    } catch (_: Exception) { 0 }

    override fun nowEpochMillis(): Long = getNowEpochMillis()

    override fun dateParts(epochMillis: Long): DateParts {
        val date = Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        return date.toParts()
    }

    override fun datePartsFromIso(isoDate: String): DateParts? = try {
        LocalDate.parse(isoDate).toParts()
    } catch (_: Exception) { null }

    private fun LocalDate.toParts() = DateParts(
        dayOfWeek = dayOfWeek.isoDayNumber,
        day = day,
        month = month.number,
        year = year,
    )

    override fun formatDisplayTime(epochMillis: Long): String = try {
        val ldt = Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        "${ldt.hour.toString().padStart(2, '0')}:" +
            "${ldt.minute.toString().padStart(2, '0')}:" +
            ldt.second.toString().padStart(2, '0')
    } catch (_: Exception) { "--:--:--" }

    override fun isoDateFromMillis(epochMillis: Long): String = try {
        Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
    } catch (_: Exception) { "" }

    override fun isoFromDMY(day: Int, month: Int, year: Int): String? = try {
        LocalDate(year, month, day).toString()
    } catch (_: Exception) { null }

    override fun millisFromDMY(day: Int, month: Int, year: Int): Long? = try {
        LocalDate(year, month, day).toEpochDays().toLong() * 86_400_000L
    } catch (_: Exception) { null }
}
