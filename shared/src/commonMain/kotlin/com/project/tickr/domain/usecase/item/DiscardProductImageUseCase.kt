package com.project.tickr.domain.usecase.item

import com.project.tickr.domain.storage.ImageStorage

/** Membuang file foto yang sudah tersimpan tapi tidak jadi dipakai (mis. penyimpanan barang gagal). */
class DiscardProductImageUseCase(private val imageStorage: ImageStorage) {
    suspend operator fun invoke(path: String) {
        imageStorage.delete(path)
    }
}
