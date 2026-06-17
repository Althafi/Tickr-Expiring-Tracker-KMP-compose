package com.project.tickr.presentation.consumed

import com.project.tickr.domain.model.ExpiringItem

data class ConsumedItemsUiState(
    val loadState: ConsumedItemsLoadState = ConsumedItemsLoadState.Loading,
    val items: List<ExpiringItem> = emptyList(),
    val isRefreshing: Boolean = false,
)
