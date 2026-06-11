package com.project.tickr.domain.model

data class ExpiryFilter(
    val urgencies: Set<Urgency> = emptySet(),
    val timeRange: TimeRange? = null,
    val categoryName: String? = null,
    val query: String = "",
)

enum class TimeRange {
    GT_6M,   // daysLeft > 180
    GT_3M,   // daysLeft > 90
    GT_30D,  // daysLeft > 30
    GT_7D,   // daysLeft > 7
    D0,      // daysLeft <= 0 (hari ini / sudah lewat)
}
