package com.project.tickr.domain.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.ProductInfo

/** Pencarian produk di database publik lewat internet. Satu-satunya fitur Tickr yang butuh online. */
interface ProductLookupRepository {
    /** Sukses dengan `null` bila barcode tidak ada di database. */
    suspend fun lookup(barcode: String): DataResult<ProductInfo?>

    /** Mengunduh gambar dari [url]. */
    suspend fun downloadImage(url: String): DataResult<ByteArray>
}
