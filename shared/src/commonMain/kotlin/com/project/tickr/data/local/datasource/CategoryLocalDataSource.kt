package com.project.tickr.data.local.datasource

import com.project.tickr.core.result.DataResult
import com.project.tickr.db.Category as CategoryRow
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.model.Category

internal class CategoryLocalDataSource(
    private val db: TickrDatabase,
    private val now: () -> String = ::nowIso,
) {
    private val queries get() = db.categoryQueries

    suspend fun getAll(): DataResult<List<CategoryRow>> =
        safeDbCall { queries.selectAll().executeAsList() }

    suspend fun getById(id: Long): DataResult<CategoryRow> =
        safeDbCall { queries.selectById(id).executeAsOneOrNull() ?: throw EntityNotFoundException("category") }

    suspend fun insert(category: Category): DataResult<CategoryRow> = safeDbCall {
        db.transactionWithResult {
            queries.insert(
                key = category.key,
                name = category.name,
                icon_name = category.iconName,
                color_hex = category.colorHex,
                created_at = now(),
            )
            queries.selectById(queries.lastInsertRowId().executeAsOne()).executeAsOne()
        }
    }

    suspend fun update(category: Category): DataResult<CategoryRow> = safeDbCall {
        db.transactionWithResult {
            queries.selectById(category.id).executeAsOneOrNull() ?: throw EntityNotFoundException("category")
            queries.update(
                name = category.name,
                icon_name = category.iconName,
                color_hex = category.colorHex,
                id = category.id,
            )
            queries.selectById(category.id).executeAsOne()
        }
    }

    suspend fun delete(id: Long): DataResult<Unit> =
        safeDbCall { queries.delete(id) }
}
