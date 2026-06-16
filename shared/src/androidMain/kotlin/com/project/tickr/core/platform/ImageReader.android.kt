package com.project.tickr.core.platform

import android.net.Uri
import com.project.tickr.data.repository.AndroidContextHolder

actual suspend fun readImageBytes(uriPath: String): ByteArray? =
    try {
        val uri = Uri.parse(uriPath)
        AndroidContextHolder.getContext()
            .contentResolver
            .openInputStream(uri)
            ?.use { it.readBytes() }
    } catch (e: Exception) {
        null
    }
