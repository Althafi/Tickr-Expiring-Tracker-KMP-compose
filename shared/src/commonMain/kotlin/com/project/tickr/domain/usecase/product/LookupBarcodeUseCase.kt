package com.project.tickr.domain.usecase.product

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.ProductInfo
import com.project.tickr.domain.provider.ConnectivityProvider
import com.project.tickr.domain.repository.ProductLookupRepository

/** Mencari produk dari hasil scan. Gagal dengan [AppError.Offline] bila perangkat tidak terhubung internet. */
class LookupBarcodeUseCase(
    private val connectivity: ConnectivityProvider,
    private val repository: ProductLookupRepository,
) {
    suspend operator fun invoke(barcode: String): DataResult<ProductInfo?> {
        val code = barcode.trim()
        if (!isValidBarcode(code)) {
            return DataResult.Error(AppError.Validation("barcode", "must be 8-14 digits"))
        }
        if (!connectivity.isOnline()) return DataResult.Error(AppError.Offline)
        return repository.lookup(code)
    }

    companion object {
        /** EAN-8, UPC-A (12), EAN-13, ITF-14: hanya angka, 8–14 digit. */
        fun isValidBarcode(code: String): Boolean = code.length in 8..14 && code.all { it in '0'..'9' }
    }
}
