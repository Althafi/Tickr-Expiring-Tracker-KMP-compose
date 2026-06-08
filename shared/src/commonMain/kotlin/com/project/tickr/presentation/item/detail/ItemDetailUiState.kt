package com.project.tickr.presentation.item.detail

import com.project.tickr.domain.model.Item

data class ItemDetailUiState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val error: String? = null
)
