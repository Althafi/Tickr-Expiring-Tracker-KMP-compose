package com.project.tickr.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

private const val CAMERA_DIR = "camera"

@Composable
actual fun rememberCameraLauncher(
    onPhotoTaken: (uri: String?) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    // rememberSaveable: aktivitas bisa dibuat ulang (rotasi/memori rendah) saat aplikasi kamera terbuka.
    var pendingUri by rememberSaveable { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = pendingUri
        pendingUri = null
        onPhotoTaken(if (saved) uri else null)
    }
    // TakePicture memakai intent kamera sistem, jadi tidak perlu izin CAMERA di aplikasi ini.
    return remember(context, launcher) {
        {
            val uri = runCatching {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newPhotoFile(context))
            }.getOrNull()
            if (uri == null) {
                onPhotoTaken(null)
            } else {
                pendingUri = uri.toString()
                try {
                    launcher.launch(uri)
                } catch (_: ActivityNotFoundException) {
                    pendingUri = null
                    onPhotoTaken(null) // tidak ada aplikasi kamera
                }
            }
        }
    }
}

/** File sementara di cache; foto sebelumnya sudah disalin ke penyimpanan app, jadi aman dibersihkan. */
private fun newPhotoFile(context: Context): File {
    val dir = File(context.cacheDir, CAMERA_DIR).apply { mkdirs() }
    dir.listFiles()?.forEach { it.delete() }
    return File.createTempFile("photo_", ".jpg", dir)
}
