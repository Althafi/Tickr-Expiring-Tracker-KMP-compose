package com.project.tickr.presentation.category.list

import com.project.tickr.domain.model.Category

data class CategoryListUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && categories.isEmpty()
}
