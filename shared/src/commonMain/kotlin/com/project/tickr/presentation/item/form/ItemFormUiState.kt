package com.project.tickr.presentation.item.form

data class ItemFormUiState(
    val name: String = "",
    val expiryDate: String = "",
    val categoryId: Long? = null,
    val notes: String = "",
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val expiryDateError: String? = null,
    val generalError: String? = null
) {
    val canSubmit: Boolean get() = name.isNotBlank() && expiryDate.isNotBlank() && !isSaving
}
