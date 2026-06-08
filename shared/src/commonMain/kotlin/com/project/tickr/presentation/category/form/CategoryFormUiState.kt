package com.project.tickr.presentation.category.form

data class CategoryFormUiState(
    val name: String = "",
    val iconName: String = "",
    val colorHex: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val canSubmit: Boolean get() = name.isNotBlank() && !isSaving
}
