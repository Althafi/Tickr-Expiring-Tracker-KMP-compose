package com.project.tickr.presentation.additem

import com.project.tickr.domain.model.Category
import com.project.tickr.presentation.common.UiText
import com.project.tickr.domain.model.ItemUnits

val UNIT_OPTIONS = ItemUnits.ALL

enum class NoticeKind { INFO, SUCCESS, WARNING, OFFLINE }

/** Pesan di dalam sheet (snackbar global tertutup sheet). Hilang sendiri setelah beberapa detik. */
data class AddItemNotice(val text: UiText, val kind: NoticeKind)

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
    /** Path lokal hasil pilihan galeri, atau URL `https://` foto produk dari pencarian barcode (diunduh saat disimpan). */
    val photoPath: String? = null,
    val barcode: String? = null,
    val isLookingUp: Boolean = false,
    /** Diset true oleh ViewModel saat pemindai boleh dibuka; UI membukanya lalu mengirim [AddItemAction.ScannerLaunched]. */
    val requestScanner: Boolean = false,
    val notice: AddItemNotice? = null,
    val isSaving: Boolean = false,
    val nameError: UiText? = null,
    val expiryError: UiText? = null,
    /** Gagal menyimpan; ditampilkan di dalam sheet karena snackbar tertutup sheet. */
    val saveError: UiText? = null,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && expiryDateMillis != null && quantity >= 1 &&
                selectedCategoryId != null && !isSaving && !isLookingUp

    /** DD / MM / YYYY untuk display (derivasi dari expiryIsoDate) */
    val expiryDateDisplay: String
        get() = if (expiryIsoDate.length == 10) {
            val p = expiryIsoDate.split("-")
            "${p[2]} / ${p[1]} / ${p[0]}"
        } else ""
}
