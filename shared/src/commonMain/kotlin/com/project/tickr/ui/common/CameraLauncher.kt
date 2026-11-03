package com.project.tickr.ui.common

import androidx.compose.runtime.Composable

/**
 * Mengembalikan lambda yang membuka kamera platform untuk memotret produk.
 * [onPhotoTaken] menerima URI foto, atau null bila pengguna membatalkan / kamera tidak tersedia.
 */
@Composable
expect fun rememberCameraLauncher(
    onPhotoTaken: (uri: String?) -> Unit,
): () -> Unit
