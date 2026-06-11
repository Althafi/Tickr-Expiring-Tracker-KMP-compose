package com.project.tickr.presentation.expiry

import com.project.tickr.domain.model.ConsumptionStats
import com.project.tickr.domain.model.ExpiryFilter
import com.project.tickr.domain.model.ExpiringItem
import com.project.tickr.domain.model.TimeRange
import com.project.tickr.domain.model.Urgency

data class ExpiryUiState(
    val loadState: ExpiryLoadState = ExpiryLoadState.Loading,
    val items: List<ExpiringItem> = emptyList(),
    val stats: ConsumptionStats? = null,
    val activeFilter: ExpiryFilter = ExpiryFilter(),
    val pendingUrgencies: Set<Urgency> = emptySet(),
    val pendingTimeRange: TimeRange? = null,
    val isRefreshing: Boolean = false,
    val isFilterSheetVisible: Boolean = false,
    val nowEpochMillis: Long = 0L,
    val displayDate: String = "",
    val displayTime: String = "",
)
