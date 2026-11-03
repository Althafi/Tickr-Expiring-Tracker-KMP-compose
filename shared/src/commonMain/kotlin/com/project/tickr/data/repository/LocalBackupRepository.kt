package com.project.tickr.data.repository

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.data.backup.BackupCategory
import com.project.tickr.data.backup.BackupCodec
import com.project.tickr.data.backup.BackupFile
import com.project.tickr.data.backup.BackupItem
import com.project.tickr.data.local.datasource.nowIso
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.model.BackupExport
import com.project.tickr.domain.model.BackupSummary
import com.project.tickr.domain.repository.BackupRepository
import com.project.tickr.domain.storage.ImageStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64

internal class LocalBackupRepository(
    private val db: TickrDatabase,
    private val images: ImageStorage,
    private val dateTime: DateTimeUtil,
    private val now: () -> String = ::nowIso,
) : BackupRepository {

    override suspend fun export(): DataResult<BackupExport> = try {
        val (categories, items) = withContext(Dispatchers.Default) {
            db.categoryQueries.selectAll().executeAsList() to db.itemQueries.selectAll().executeAsList()
        }
        val today = dateTime.today()
        val file = BackupFile(
            exportedAt = today,
            categories = categories.map {
                BackupCategory(it.id, it.key, it.name, it.icon_name, it.color_hex)
            },
            items = items.map { row ->
                BackupItem(
                    categoryId = row.category_id,
                    name = row.name,
                    barcode = row.barcode,
                    expiryDate = row.expiry_date,
                    notes = row.notes,
                    isConsumed = row.is_consumed != 0L,
                    quantity = row.quantity.toInt(),
                    unit = row.unit,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                    imageBase64 = row.image_url?.let { images.readBytes(it) }?.let { Base64.encode(it) },
                )
            },
        )
        val content = withContext(Dispatchers.Default) { BackupCodec.encode(file) }
        DataResult.Success(BackupExport("tickr-backup-$today.json", content, items.size))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Error(AppError.Unknown(e.message ?: "export failed"))
    }

    override suspend fun import(content: String): DataResult<BackupSummary> {
        // 1. Validasi penuh sebelum menyentuh apa pun.
        val file = when (val parsed = BackupCodec.parse(content)) {
            is DataResult.Success -> parsed.data
            is DataResult.Error -> return parsed
        }
        val photos: List<ByteArray?> = try {
            file.items.map { item -> item.imageBase64?.let { Base64.decode(it) } }
        } catch (e: IllegalArgumentException) {
            return DataResult.Error(AppError.Validation(BackupCodec.INVALID, "corrupt image data"))
        }

        // 2. Tulis foto ke lokasi baru. Foto lama belum disentuh.
        val newPaths = mutableListOf<String?>()
        for (bytes in photos) {
            if (bytes == null) {
                newPaths += null
                continue
            }
            when (val saved = images.saveBytes(bytes)) {
                is DataResult.Success -> newPaths += saved.data
                is DataResult.Error -> {
                    discard(newPaths)
                    return saved
                }
            }
        }

        // 3. Ganti isi database dalam satu transaksi: gagal berarti kembali seperti semula.
        val oldPaths: List<String> = try {
            withContext(Dispatchers.Default) {
                db.transactionWithResult {
                    val previous = db.itemQueries.selectAll().executeAsList().mapNotNull { it.image_url }
                    db.notificationLogQueries.deleteAll()
                    db.itemQueries.deleteAll()
                    val categoryIds = mapCategories(file.categories)
                    file.items.forEachIndexed { index, item ->
                        db.itemQueries.insert(
                            category_id = item.categoryId?.let { categoryIds[it] },
                            name = item.name.trim(),
                            barcode = item.barcode,
                            expiry_date = item.expiryDate,
                            image_url = newPaths[index],
                            notes = item.notes,
                            is_consumed = if (item.isConsumed) 1L else 0L,
                            quantity = item.quantity.toLong(),
                            unit = item.unit,
                            created_at = item.createdAt,
                            updated_at = item.updatedAt,
                        )
                    }
                    previous
                }
            }
        } catch (e: CancellationException) {
            discard(newPaths)
            throw e
        } catch (e: Exception) {
            discard(newPaths)
            return DataResult.Error(AppError.Unknown(e.message ?: "import failed"))
        }

        // 4. Berhasil: foto lama tidak dibutuhkan lagi.
        oldPaths.forEach { images.delete(it) }
        return DataResult.Success(BackupSummary(file.items.size))
    }

    /** Mencocokkan kategori di file dengan yang ada di perangkat (by key, atau by nama untuk kategori kustom). */
    private fun mapCategories(fromFile: List<BackupCategory>): Map<Long, Long> {
        val existing = db.categoryQueries.selectAll().executeAsList()
        return fromFile.associate { c ->
            val match = existing.firstOrNull { c.key != null && it.key == c.key }
                ?: existing.firstOrNull { c.key == null && it.key == null && it.name == c.name }
            val localId = match?.id ?: run {
                db.categoryQueries.insert(c.key, c.name, c.iconName, c.colorHex, now())
                db.categoryQueries.lastInsertRowId().executeAsOne()
            }
            c.id to localId
        }
    }

    private suspend fun discard(paths: List<String?>) {
        paths.forEach { if (it != null) images.delete(it) }
    }
}
