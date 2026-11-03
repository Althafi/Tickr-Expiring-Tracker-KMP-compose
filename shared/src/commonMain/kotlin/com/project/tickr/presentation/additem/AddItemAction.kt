package com.project.tickr.presentation.additem

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface AddItemAction : UiAction {
    data object Reset : AddItemAction
    data class NameChanged(val value: String) : AddItemAction
    data class CategorySelected(val id: Long?, val name: String) : AddItemAction
    data class QtyChanged(val value: Int) : AddItemAction
    data class UnitChanged(val value: String) : AddItemAction
    /** rawDigits = hanya angka, max 8 karakter (DDMMYYYY) */
    data class RawDateChanged(val rawDigits: String) : AddItemAction
    /** dipanggil dari calendar picker native */
    data class ExpiryPicked(val millis: Long) : AddItemAction
    data class PhotoPicked(val path: String?) : AddItemAction
    /** Tombol scan ditekan: cek koneksi dulu, baru buka pemindai. */
    data object ScanClicked : AddItemAction
    data object ScannerLaunched : AddItemAction
    data class BarcodeScanned(val code: String) : AddItemAction
    data object ScanFailed : AddItemAction
    data object ClearBarcode : AddItemAction
    data object DismissNotice : AddItemAction
    data object Save : AddItemAction
    data object Dismiss : AddItemAction
}
