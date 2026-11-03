package com.project.tickr.domain.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    suspend fun getItems(): DataResult<List<Item>>

    /** Emits the current items right away, then again after every change to the item table. */
    fun observeItems(): Flow<List<Item>>

    suspend fun getItem(id: Long): DataResult<Item>
    suspend fun createItem(item: Item): DataResult<Item>
    suspend fun updateItem(item: Item): DataResult<Item>
    suspend fun deleteItem(id: Long): DataResult<Unit>
}
