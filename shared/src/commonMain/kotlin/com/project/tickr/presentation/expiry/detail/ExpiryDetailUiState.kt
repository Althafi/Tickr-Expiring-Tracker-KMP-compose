package com.project.tickr.presentation.expiry.detail

import com.project.tickr.domain.model.Item
import com.project.tickr.domain.model.Urgency

data class ExpiryDetailUiState(
    val loadState: ExpiryDetailLoadState = ExpiryDetailLoadState.Loading,
    val item: Item? = null,
    val categoryName: String = "",
    val daysUntilExpiry: Int = 0,
    val urgency: Urgency = Urgency.SAFE,
    val isProcessing: Boolean = false,
    val showDeleteDialog: Boolean = false,
)
