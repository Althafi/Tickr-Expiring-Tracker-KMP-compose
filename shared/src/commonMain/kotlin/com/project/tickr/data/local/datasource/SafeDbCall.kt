package com.project.tickr.data.local.datasource

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock

internal class EntityNotFoundException(val entity: String) : Exception("$entity not found")

/** Menjalankan operasi database di luar main thread dan mengubah exception menjadi [DataResult.Error]. */
internal suspend fun <T> safeDbCall(block: () -> T): DataResult<T> =
    withContext(Dispatchers.Default) {
        try {
            DataResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: EntityNotFoundException) {
            DataResult.Error(AppError.NotFound(e.entity))
        } catch (e: Exception) {
            DataResult.Error(AppError.Unknown(e.message ?: e::class.simpleName ?: "database error"))
        }
    }

/** Timestamp ISO-8601 UTC, mis. `2026-11-03T09:15:30.123Z`. */
internal fun nowIso(): String = Clock.System.now().toString()
