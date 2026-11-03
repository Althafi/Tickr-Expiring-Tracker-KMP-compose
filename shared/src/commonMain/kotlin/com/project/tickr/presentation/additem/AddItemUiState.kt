package com.project.tickr.presentation.additem

import com.project.tickr.domain.model.Category
import com.project.tickr.presentation.common.UiText
import com.project.tickr.domain.model.ItemUnits

val UNIT_OPTIONS = ItemUnits.ALL

data class AddItemUiState(
    val name: String = "",
    val dbCategories: List<Category> = emptyList(),
    val isLoadingCategories: Boolean = true,
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String? = null,
    val quantity: Int = 0,
    val unit: String = ItemUnits.DEFAULT,
    /** Hanya digit 0-8 karakter: DDMMYYYY */
    val rawDateDigits: String = "",
    val expiryDateMillis: Long? = null,
    val expiryIsoDate: String = "",
    val photoPath: String? = null,
    val isSaving: Boolean = false,
    val nameError: UiText? = null,
    val expiryError: UiText? = null,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && expiryDateMillis != null && quantity >= 1 &&
                selectedCategoryId != null && !isSaving

    /** DD / MM / YYYY untuk display (derivasi dari expiryIsoDate) */
    val expiryDateDisplay: String
        get() = if (expiryIsoDate.length == 10) {
            val p = expiryIsoDate.split("-")
            "${p[2]} / ${p[1]} / ${p[0]}"
        } else ""
}
