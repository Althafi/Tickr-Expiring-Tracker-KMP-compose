package com.project.tickr.presentation.item.list

import com.project.tickr.domain.model.Item

data class ItemListUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()
}
