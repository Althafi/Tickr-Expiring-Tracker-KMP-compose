package com.project.tickr.presentation.home

import com.project.tickr.domain.model.CategoryGroup
import com.project.tickr.domain.model.CategorySlice
import com.project.tickr.domain.model.WasteTrend
import com.project.tickr.core.util.DateParts
import com.project.tickr.presentation.common.UiText

data class HomeUiState(
    // true sejak awal: sebelum emisi pertama Flow, daftar kosong ≠ "belum ada barang" (hindari kilatan empty state).
    val isLoading: Boolean = true,
    val nowEpochMillis: Long = 0L,
    val date: DateParts? = null,
    val displayTime: String = "",
    val wasteTrend: WasteTrend? = null,
    val slices: List<CategorySlice> = emptyList(),
    val totalItems: Int = 0,
    val expiringGroups: List<CategoryGroup> = emptyList(),
    val criticalCount: Int = 0,
    val error: UiText? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && error == null && expiringGroups.isEmpty() && slices.isEmpty()
}
