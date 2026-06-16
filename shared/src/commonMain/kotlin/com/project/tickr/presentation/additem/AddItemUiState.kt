package com.project.tickr.presentation.additem

import com.project.tickr.domain.model.Category

val UNIT_OPTIONS = listOf("Pcs", "Gram", "Kilogram", "Liter", "Mililiter", "Botol", "Bungkus", "Lusin")

/** Kategori default tampil jika DB kosong */
data class StaticCategory(val id: Long?, val name: String, val emoji: String)

val DEFAULT_CATEGORIES = listOf(
    StaticCategory(null, "Makanan & Minuman", "🍎"),
    StaticCategory(null, "Kecantikan", "💄"),
    StaticCategory(null, "Obat & Vitamin", "💊"),
    StaticCategory(null, "Lainnya", "📦"),
)

data class AddItemUiState(
    val name: String = "",
    val dbCategories: List<Category> = emptyList(),
    val isLoadingCategories: Boolean = true,
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String? = null,
    val quantity: Int = 0,
    val unit: String = "Pcs",
    /** Hanya digit 0-8 karakter: DDMMYYYY */
    val rawDateDigits: String = "",
    val expiryDateMillis: Long? = null,
    val expiryIsoDate: String = "",
    val photoPath: String? = null,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val expiryError: String? = null,
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
