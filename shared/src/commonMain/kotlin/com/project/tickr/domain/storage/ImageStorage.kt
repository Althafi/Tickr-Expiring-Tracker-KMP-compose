package com.project.tickr.domain.storage

import com.project.tickr.core.result.DataResult

/** Penyimpanan foto produk di perangkat. Path yang disimpan di database selalu RELATIF. */
interface ImageStorage {
    /** Menyalin (dan mengecilkan) gambar dari [sourcePath] ke penyimpanan privat app. Mengembalikan path relatif. */
    suspend fun saveProductImage(sourcePath: String): DataResult<String>

    /** Menghapus file gambar. Aman dipanggil untuk path yang sudah tidak ada. */
    suspend fun delete(path: String): DataResult<Unit>

    /** Path relatif → path absolut di perangkat. URL `http(s)` dan path absolut dikembalikan apa adanya. */
    fun resolve(path: String): String
}
