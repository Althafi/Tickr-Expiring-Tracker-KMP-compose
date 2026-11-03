package com.project.tickr.domain.usecase.item

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.storage.ImageStorage

/** Menyimpan foto produk ke penyimpanan lokal dan mengembalikan path relatifnya. */
class SaveProductImageUseCase(private val imageStorage: ImageStorage) {
    suspend operator fun invoke(sourcePath: String): DataResult<String> =
        imageStorage.saveProductImage(sourcePath)
}
