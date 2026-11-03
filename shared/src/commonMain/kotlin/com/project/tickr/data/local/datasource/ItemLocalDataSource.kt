package com.project.tickr.data.local.datasource

import com.project.tickr.core.result.DataResult
import com.project.tickr.db.Item as ItemRow
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.model.Item

internal class ItemLocalDataSource(
    private val db: TickrDatabase,
    private val now: () -> String = ::nowIso,
) {
    private val queries get() = db.itemQueries

    suspend fun getAll(): DataResult<List<ItemRow>> =
        safeDbCall { queries.selectAll().executeAsList() }

    suspend fun getById(id: Long): DataResult<ItemRow> =
        safeDbCall { queries.selectById(id).executeAsOneOrNull() ?: throw EntityNotFoundException("item") }

    suspend fun insert(item: Item): DataResult<ItemRow> = safeDbCall {
        db.transactionWithResult {
            val timestamp = now()
            queries.insert(
                category_id = item.categoryId,
                name = item.name,
                barcode = item.barcode,
                expiry_date = item.expiryDate,
                image_url = item.imageUrl,
                notes = item.notes,
                is_consumed = if (item.isConsumed) 1L else 0L,
                quantity = item.quantity.toLong(),
                unit = item.unit,
                created_at = timestamp,
                updated_at = timestamp,
            )
            val id = queries.lastInsertRowId().executeAsOne()
            queries.selectById(id).executeAsOne()
        }
    }

    suspend fun update(item: Item): DataResult<ItemRow> = safeDbCall {
        db.transactionWithResult {
            queries.selectById(item.id).executeAsOneOrNull() ?: throw EntityNotFoundException("item")
            queries.update(
                category_id = item.categoryId,
                name = item.name,
                barcode = item.barcode,
                expiry_date = item.expiryDate,
                image_url = item.imageUrl,
                notes = item.notes,
                is_consumed = if (item.isConsumed) 1L else 0L,
                quantity = item.quantity.toLong(),
                unit = item.unit,
                updated_at = now(),
                id = item.id,
            )
            queries.selectById(item.id).executeAsOne()
        }
    }

    suspend fun delete(id: Long): DataResult<Unit> =
        safeDbCall { queries.delete(id) }
}
