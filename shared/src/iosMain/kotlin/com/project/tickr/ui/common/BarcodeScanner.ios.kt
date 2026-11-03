package com.project.tickr.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberBarcodeScanner(onResult: (BarcodeScanResult) -> Unit): () -> Unit {
    val latest = rememberUpdatedState(onResult)
    // TODO(user): pemindai iOS (AVCaptureMetadataOutput lewat UIViewController) belum dibuat, sama seperti
    // pemilih foto iOS. Sementara dilaporkan gagal agar pengguna iOS diarahkan mengisi manual.
    return remember { { latest.value(BarcodeScanResult.Failed) } }
}
