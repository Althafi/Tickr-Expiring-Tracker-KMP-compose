package com.project.tickr.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
actual fun rememberBarcodeScanner(onResult: (BarcodeScanResult) -> Unit): () -> Unit {
    val context = LocalContext.current
    val latest = rememberUpdatedState(onResult)
    return remember(context) {
        {
            // Google code scanner: UI pemindai dan izin kamera ditangani Google Play services.
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                )
                .enableAutoZoom()
                .build()
            GmsBarcodeScanning.getClient(context, options).startScan()
                .addOnSuccessListener { barcode ->
                    val code = barcode.rawValue
                    latest.value(if (code.isNullOrBlank()) BarcodeScanResult.Failed else BarcodeScanResult.Success(code))
                }
                .addOnCanceledListener { latest.value(BarcodeScanResult.Cancelled) }
                .addOnFailureListener { latest.value(BarcodeScanResult.Failed) }
        }
    }
}
