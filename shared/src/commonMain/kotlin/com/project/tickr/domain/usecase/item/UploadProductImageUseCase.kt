package com.project.tickr.domain.usecase.item

import com.project.tickr.core.platform.readImageBytes
import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.data.remote.datasource.StorageRemoteDataSource

class UploadProductImageUseCase(
    private val storageRemoteDataSource: StorageRemoteDataSource,
) {
    suspend operator fun invoke(userId: String, localPath: String): DataResult<String> {
        val bytes = readImageBytes(localPath)
            ?: return DataResult.Error(AppError.Unknown("Gagal membaca file gambar"))
        return storageRemoteDataSource.uploadProductImage(userId, bytes)
    }
}
