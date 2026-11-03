package com.project.tickr.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.project.tickr.data.local.image.toByteArray
import com.project.tickr.data.local.image.toNSData
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject

@Composable
actual fun rememberBackupFiles(): BackupFiles = remember { IosBackupFiles() }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosBackupFiles : BackupFiles {

    /** Delegate dipegang kuat-kuat: UIKit hanya menyimpan referensi lemah ke delegate-nya. */
    private var pickerDelegate: PickerDelegate? = null

    override fun save(fileName: String, content: String, onResult: (BackupFileResult) -> Unit) {
        val presenter = topViewController()
        if (presenter == null) {
            onResult(BackupFileResult.Failed)
            return
        }
        // Tulis ke folder sementara lalu tawarkan share sheet ("Simpan ke Berkas", AirDrop, dsb.).
        val path = NSTemporaryDirectory().trimEnd('/') + "/" + fileName
        if (!content.encodeToByteArray().toNSData().writeToFile(path, atomically = true)) {
            onResult(BackupFileResult.Failed)
            return
        }
        val sheet = UIActivityViewController(
            activityItems = listOf(NSURL.fileURLWithPath(path)),
            applicationActivities = null,
        )
        sheet.completionWithItemsHandler = { _, completed, _, error ->
            onResult(
                when {
                    error != null -> BackupFileResult.Failed
                    completed -> BackupFileResult.Success()
                    else -> BackupFileResult.Cancelled
                }
            )
        }
        // iPad: share sheet wajib punya jangkar popover, kalau tidak app crash.
        sheet.popoverPresentationController?.let { popover ->
            popover.sourceView = presenter.view
            popover.sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)
        }
        presenter.presentViewController(sheet, animated = true, completion = null)
    }

    override fun open(onResult: (BackupFileResult) -> Unit) {
        val presenter = topViewController()
        if (presenter == null) {
            onResult(BackupFileResult.Failed)
            return
        }
        @Suppress("DEPRECATION")
        val picker = UIDocumentPickerViewController(
            documentTypes = listOf("public.json", "public.text", "public.data"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
        )
        val delegate = PickerDelegate { url ->
            pickerDelegate = null
            onResult(
                when {
                    url == null -> BackupFileResult.Cancelled
                    else -> NSData.dataWithContentsOfURL(url)
                        ?.let { BackupFileResult.Success(it.toByteArray().decodeToString()) }
                        ?: BackupFileResult.Failed
                }
            )
        }
        pickerDelegate = delegate
        picker.delegate = delegate
        presenter.presentViewController(picker, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PickerDelegate(
    private val onPicked: (NSURL?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onPicked(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPicked(null)
    }
}

private fun topViewController(): UIViewController? {
    val window: UIWindow? = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { it.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { it.isKeyWindow() }
    var top = window?.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}
