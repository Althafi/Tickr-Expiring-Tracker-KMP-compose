package com.project.tickr.ui.common

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MIME_JSON = "application/json"

/** Satu operasi yang sedang menunggu hasil dari pemilih file. */
private class Pending(
    val content: String? = null,
    val onResult: (BackupFileResult) -> Unit,
)

@Composable
actual fun rememberBackupFiles(): BackupFiles {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Hasil pemilih file kembali lewat callback launcher. Operasi yang menunggu disimpan di level proses,
    // bukan di `remember`, supaya tetap sampai setelah Activity dibuat ulang (mis. layar diputar).
    val holder = PendingHolder

    val createLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(MIME_JSON)) { uri ->
        val pending = holder.take() ?: return@rememberLauncherForActivityResult
        if (uri == null) pending.onResult(BackupFileResult.Cancelled)
        else scope.launch { pending.onResult(write(context, uri, pending.content.orEmpty())) }
    }
    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val pending = holder.take() ?: return@rememberLauncherForActivityResult
        if (uri == null) pending.onResult(BackupFileResult.Cancelled)
        else scope.launch { pending.onResult(read(context, uri)) }
    }

    return remember(createLauncher, openLauncher) {
        object : BackupFiles {
            override fun save(fileName: String, content: String, onResult: (BackupFileResult) -> Unit) {
                holder.pending = Pending(content, onResult)
                createLauncher.launch(fileName)
            }

            override fun open(onResult: (BackupFileResult) -> Unit) {
                holder.pending = Pending(null, onResult)
                // Banyak penyedia file melaporkan JSON sebagai text/plain atau octet-stream, jadi jangan dipersempit.
                openLauncher.launch(arrayOf("*/*"))
            }
        }
    }
}

private object PendingHolder {
    var pending: Pending? = null
    fun take(): Pending? = pending.also { pending = null }
}

private suspend fun write(context: Context, uri: Uri, content: String): BackupFileResult =
    withContext(Dispatchers.IO) {
        try {
            // "wt" memotong file lama bila pengguna menimpa file yang sudah ada.
            val stream = context.contentResolver.openOutputStream(uri, "wt") ?: return@withContext BackupFileResult.Failed
            stream.use { it.write(content.encodeToByteArray()) }
            BackupFileResult.Success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            BackupFileResult.Failed
        }
    }

private suspend fun read(context: Context, uri: Uri): BackupFileResult =
    withContext(Dispatchers.IO) {
        try {
            val stream = context.contentResolver.openInputStream(uri) ?: return@withContext BackupFileResult.Failed
            BackupFileResult.Success(stream.use { it.readBytes().decodeToString() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            BackupFileResult.Failed
        }
    }
