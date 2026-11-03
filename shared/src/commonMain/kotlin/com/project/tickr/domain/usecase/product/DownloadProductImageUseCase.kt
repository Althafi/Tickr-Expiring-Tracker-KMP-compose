package com.project.tickr.domain.usecase.product

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.flatMap
import com.project.tickr.domain.repository.ProductLookupRepository
import com.project.tickr.domain.storage.ImageStorage

/** Mengunduh foto produk dari internet lalu menyimpannya ke penyimpanan lokal. Mengembalikan path relatif. */
class DownloadProductImageUseCase(
    private val repository: ProductLookupRepository,
    private val imageStorage: ImageStorage,
) {
    suspend operator fun invoke(url: String): DataResult<String> =
        repository.downloadImage(url).flatMap { imageStorage.saveBytes(it) }
}
