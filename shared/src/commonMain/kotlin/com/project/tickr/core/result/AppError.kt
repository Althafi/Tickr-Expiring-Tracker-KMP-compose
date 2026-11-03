package com.project.tickr.core.result

sealed class AppError(open val message: String) {
    data class NotFound(val entity: String) : AppError("$entity not found")
    data class Validation(val field: String, val reason: String) : AppError("Validation failed on '$field': $reason")
    /** Perangkat tidak punya koneksi internet; fitur online tidak bisa dipakai. */
    data object Offline : AppError("No internet connection")
    /** Ada koneksi, tapi permintaan ke server gagal (timeout, server error, dsb). */
    data class Network(val detail: String) : AppError("Network request failed: $detail")
    data class Unknown(val detail: String) : AppError(detail)
}
