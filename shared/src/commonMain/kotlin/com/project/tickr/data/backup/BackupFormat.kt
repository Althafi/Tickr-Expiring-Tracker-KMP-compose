package com.project.tickr.data.backup

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.core.validation.Validators
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Format file cadangan (JSON, satu file, foto di-embed sebagai base64).
 * Naikkan [BackupFile.CURRENT_VERSION] dan tulis konversi bila bentuknya berubah.
 */
@Serializable
internal data class BackupFile(
    val app: String = APP_ID,
    val version: Int = CURRENT_VERSION,
    val exportedAt: String,
    val categories: List<BackupCategory>,
    val items: List<BackupItem>,
) {
    companion object {
        const val APP_ID = "tickr"
        const val CURRENT_VERSION = 1
    }
}

@Serializable
internal data class BackupCategory(
    val id: Long,
    val key: String? = null,
    val name: String,
    val iconName: String,
    val colorHex: String,
)

@Serializable
internal data class BackupItem(
    val categoryId: Long? = null,
    val name: String,
    val barcode: String? = null,
    val expiryDate: String,
    val notes: String? = null,
    val isConsumed: Boolean = false,
    val quantity: Int = 1,
    val unit: String,
    val createdAt: String,
    val updatedAt: String,
    val imageBase64: String? = null,
)

internal object BackupCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(file: BackupFile): String = json.encodeToString(BackupFile.serializer(), file)

    /** Membaca dan memvalidasi. Tidak menyentuh database atau penyimpanan. */
    fun parse(text: String): DataResult<BackupFile> {
        val root: JsonObject = try {
            json.parseToJsonElement(text).jsonObject
        } catch (e: SerializationException) {
            return invalid()
        } catch (e: IllegalArgumentException) {
            return invalid()
        }

        // Cek identitas dan versi SEBELUM mendekode isi, supaya file dari versi lebih baru
        // dilaporkan "terlalu baru", bukan "rusak" ketika bentuknya sudah berubah.
        val app = root["app"]?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
        if (app != BackupFile.APP_ID) return invalid()
        val version = root["version"]?.let { runCatching { it.jsonPrimitive.int }.getOrNull() } ?: return invalid()
        if (version > BackupFile.CURRENT_VERSION) return DataResult.Error(AppError.Validation(NEWER_VERSION, "version $version"))
        if (version < 1) return invalid()

        val file = try {
            json.decodeFromJsonElement(BackupFile.serializer(), root)
        } catch (e: SerializationException) {
            return invalid()
        } catch (e: IllegalArgumentException) {
            return invalid()
        }

        file.items.forEach { item ->
            if (item.name.isBlank()) return invalid()
            if (Validators.requireIsoDate(item.expiryDate, "expiry_date") != null) return invalid()
            if (item.quantity < 1) return invalid()
            if (item.unit.isBlank()) return invalid()
        }
        file.categories.forEach { if (it.name.isBlank()) return invalid() }
        return DataResult.Success(file)
    }

    const val INVALID = "backup"
    const val NEWER_VERSION = "backup_version"

    private fun invalid(): DataResult.Error = DataResult.Error(AppError.Validation(INVALID, "not a valid Tickr backup"))
}
