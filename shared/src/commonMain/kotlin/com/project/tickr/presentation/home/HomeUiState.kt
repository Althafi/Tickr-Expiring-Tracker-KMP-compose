package com.project.tickr.presentation.home

import com.project.tickr.domain.model.Item

data class HomeUiState(
    val isLoading: Boolean = false,
    val expiringItems: List<Item> = emptyList(),
    val expiringCount: Int = 0,
    val error: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && expiringItems.isEmpty()
}
