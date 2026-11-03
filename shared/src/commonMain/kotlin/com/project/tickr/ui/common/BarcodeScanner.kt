package com.project.tickr.ui.common

import androidx.compose.runtime.Composable

sealed interface BarcodeScanResult {
    /** [code] adalah isi barcode apa adanya (biasanya hanya angka). */
    data class Success(val code: String) : BarcodeScanResult
    /** Pengguna menutup pemindai tanpa memindai apa pun. */
    data object Cancelled : BarcodeScanResult
    /** Pemindai tidak bisa dibuka (mis. izin kamera ditolak, layanan Google belum siap). */
    data object Failed : BarcodeScanResult
}

/** Mengembalikan fungsi yang membuka pemindai barcode layar penuh milik platform. */
@Composable
expect fun rememberBarcodeScanner(onResult: (BarcodeScanResult) -> Unit): () -> Unit
