package com.project.tickr.data.local.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.data.repository.AndroidContextHolder
import com.project.tickr.domain.storage.ImageStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.UUID

actual fun createLocalImageStorage(): ImageStorage = AndroidImageStorage()

internal class AndroidImageStorage : ImageStorage {

    private val filesDir: File get() = AndroidContextHolder.getContext().filesDir

    override suspend fun saveProductImage(sourcePath: String): DataResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = decodeScaled(sourcePath)
                    ?: return@withContext DataResult.Error(AppError.Unknown("Gagal membaca file gambar"))
                val relative = "${ImageScaling.DIRECTORY}/${UUID.randomUUID()}.jpg"
                val target = File(filesDir, relative).also { it.parentFile?.mkdirs() }
                try {
                    target.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, ImageScaling.JPEG_QUALITY, out)
                    }
                } finally {
                    bitmap.recycle()
                }
                DataResult.Success(relative)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataResult.Error(AppError.Unknown("Gagal menyimpan gambar: ${e.message}"))
            }
        }

    override suspend fun saveBytes(bytes: ByteArray): DataResult<String> = withContext(Dispatchers.IO) {
        try {
            val relative = "${ImageScaling.DIRECTORY}/${UUID.randomUUID()}.jpg"
            File(filesDir, relative).also { it.parentFile?.mkdirs() }.writeBytes(bytes)
            DataResult.Success(relative)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataResult.Error(AppError.Unknown("Gagal menyimpan gambar: ${e.message}"))
        }
    }

    override suspend fun readBytes(path: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            if (!ImageScaling.isManagedPath(path)) return@withContext null
            File(filesDir, path).takeIf { it.isFile }?.readBytes()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun delete(path: String): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            if (ImageScaling.isManagedPath(path)) File(filesDir, path).delete()
            DataResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataResult.Error(AppError.Unknown("Gagal menghapus gambar: ${e.message}"))
        }
    }

    override fun resolve(path: String): String =
        if (ImageScaling.isManagedPath(path)) File(filesDir, path).absolutePath else path

    private fun openSource(sourcePath: String): InputStream? {
        val uri = Uri.parse(sourcePath)
        return if (uri.scheme.isNullOrEmpty()) {
            File(sourcePath).takeIf { it.exists() }?.let { FileInputStream(it) }
        } else {
            AndroidContextHolder.getContext().contentResolver.openInputStream(uri)
        }
    }

    private fun decodeScaled(sourcePath: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openSource(sourcePath)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = ImageScaling.sampleSize(bounds.outWidth, bounds.outHeight)
        }
        val decoded = openSource(sourcePath)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null

        val orientation = openSource(sourcePath)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL

        val oriented = applyOrientation(decoded, orientation)
        val (w, h) = ImageScaling.targetSize(oriented.width, oriented.height)
        if (w == oriented.width && h == oriented.height) return oriented
        return Bitmap.createScaledBitmap(oriented, w, h, true).also {
            if (it !== oriented) oriented.recycle()
        }
    }

    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it !== bitmap) bitmap.recycle()
        }
    }
}
