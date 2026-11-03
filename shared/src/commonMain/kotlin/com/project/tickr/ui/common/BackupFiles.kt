package com.project.tickr.ui.common

import androidx.compose.runtime.Composable

/** Hasil operasi file yang melibatkan pemilih file milik sistem. */
sealed interface BackupFileResult {
    /** [text] terisi untuk operasi baca. */
    data class Success(val text: String? = null) : BackupFileResult
    data object Cancelled : BackupFileResult
    data object Failed : BackupFileResult
}

/** Penyimpan/pembaca file cadangan lewat UI sistem (Storage Access Framework di Android, share sheet dan document picker di iOS). */
interface BackupFiles {
    /** Meminta pengguna memilih lokasi, lalu menulis [content] (UTF-8). */
    fun save(fileName: String, content: String, onResult: (BackupFileResult) -> Unit)

    /** Meminta pengguna memilih file cadangan, lalu membacanya sebagai teks UTF-8. */
    fun open(onResult: (BackupFileResult) -> Unit)
}

@Composable
expect fun rememberBackupFiles(): BackupFiles
