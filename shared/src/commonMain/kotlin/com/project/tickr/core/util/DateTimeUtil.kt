package com.project.tickr.core.util

interface DateTimeUtil {
    fun isWithinDays(isoDate: String, days: Int): Boolean
    fun isPast(isoDate: String): Boolean
    fun today(): String
    fun daysUntil(isoDate: String): Int
    fun nowEpochMillis(): Long
    /** Komponen tanggal lokal (tanpa teks); pemformatan/terjemahan dilakukan di lapisan UI. */
    fun dateParts(epochMillis: Long): DateParts
    /** Komponen dari tanggal ISO "YYYY-MM-DD", atau null bila tidak valid. */
    fun datePartsFromIso(isoDate: String): DateParts?
    fun formatDisplayTime(epochMillis: Long): String
    fun isoDateFromMillis(epochMillis: Long): String
    /** Returns ISO "YYYY-MM-DD" if valid date, null if invalid (e.g. 31/02) */
    fun isoFromDMY(day: Int, month: Int, year: Int): String?
    /** Returns epoch millis from DMY, null if invalid */
    fun millisFromDMY(day: Int, month: Int, year: Int): Long?
}
