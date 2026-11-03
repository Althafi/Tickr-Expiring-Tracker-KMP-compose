package com.project.tickr.data.local.image

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.storage.ImageStorage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

actual fun createLocalImageStorage(): ImageStorage = IosImageStorage()

/**
 * Path yang disimpan di database RELATIF terhadap folder Application Support,
 * karena path absolut container iOS bisa berubah antar-update app.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosImageStorage : ImageStorage {

    private val fileManager = NSFileManager.defaultManager

    private val baseDir: String
        get() = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
            .firstOrNull() as? String ?: error("Application Support directory unavailable")

    override suspend fun saveProductImage(sourcePath: String): DataResult<String> =
        withContext(Dispatchers.Default) {
            try {
                val data = NSData.dataWithContentsOfFile(toFilePath(sourcePath))
                    ?: return@withContext DataResult.Error(AppError.Unknown("Gagal membaca file gambar"))
                val image = UIImage.imageWithData(data)
                    ?: return@withContext DataResult.Error(AppError.Unknown("Format gambar tidak didukung"))

                val (srcW, srcH) = image.size.useContents { width.toInt() to height.toInt() }
                val (w, h) = ImageScaling.targetSize(srcW, srcH)

                val format = UIGraphicsImageRendererFormat.defaultFormat().apply { scale = 1.0 }
                val renderer = UIGraphicsImageRenderer(size = CGSizeMake(w.toDouble(), h.toDouble()), format = format)
                val resized = renderer.imageWithActions { _ ->
                    image.drawInRect(CGRectMake(0.0, 0.0, w.toDouble(), h.toDouble()))
                }
                val jpeg = UIImageJPEGRepresentation(resized, ImageScaling.JPEG_QUALITY / 100.0)
                    ?: return@withContext DataResult.Error(AppError.Unknown("Gagal mengompres gambar"))

                val relative = "${ImageScaling.DIRECTORY}/${NSUUID().UUIDString}.jpg"
                fileManager.createDirectoryAtPath(
                    "$baseDir/${ImageScaling.DIRECTORY}", withIntermediateDirectories = true,
                    attributes = null, error = null,
                )
                if (!jpeg.writeToFile("$baseDir/$relative", atomically = true)) {
                    return@withContext DataResult.Error(AppError.Unknown("Gagal menulis file gambar"))
                }
                DataResult.Success(relative)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataResult.Error(AppError.Unknown("Gagal menyimpan gambar: ${e.message}"))
            }
        }

    override suspend fun delete(path: String): DataResult<Unit> = withContext(Dispatchers.Default) {
        try {
            if (ImageScaling.isManagedPath(path)) fileManager.removeItemAtPath("$baseDir/$path", error = null)
            DataResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataResult.Error(AppError.Unknown("Gagal menghapus gambar: ${e.message}"))
        }
    }

    override fun resolve(path: String): String =
        if (ImageScaling.isManagedPath(path)) "$baseDir/$path" else path

    private fun toFilePath(source: String): String =
        if (source.startsWith("file://")) NSURL.URLWithString(source)?.path ?: source.removePrefix("file://") else source
}
