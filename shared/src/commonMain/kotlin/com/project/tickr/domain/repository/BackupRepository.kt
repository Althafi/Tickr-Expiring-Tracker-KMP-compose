package com.project.tickr.domain.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.BackupExport
import com.project.tickr.domain.model.BackupSummary

/** Ekspor dan impor seluruh data (barang + foto) sebagai satu file teks. */
interface BackupRepository {
    suspend fun export(): DataResult<BackupExport>

    /**
     * Mengganti SEMUA barang saat ini dengan isi [content]. Atomik: bila file tidak valid atau penulisan gagal,
     * data yang ada tidak berubah.
     */
    suspend fun import(content: String): DataResult<BackupSummary>
}
