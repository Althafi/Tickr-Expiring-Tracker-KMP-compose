package com.project.tickr.core.util

/** [dayOfWeek]: 1 = Senin … 7 = Minggu (ISO-8601). [month]: 1–12. */
data class DateParts(
    val dayOfWeek: Int,
    val day: Int,
    val month: Int,
    val year: Int,
)
