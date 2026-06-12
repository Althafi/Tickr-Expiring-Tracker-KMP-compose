package com.project.tickr.data.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.mapData
import com.project.tickr.data.mapper.toDomain
import com.project.tickr.data.mapper.toCreateDto
import com.project.tickr.data.mapper.toDto
import com.project.tickr.data.remote.datasource.ItemRemoteDataSource
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.repository.ItemRepository

class ItemRepositoryImpl(
    private val remote: ItemRemoteDataSource
) : ItemRepository {

    override suspend fun getItems(): DataResult<List<Item>> =
        remote.getAll().mapData { list -> list.map { it.toDomain() } }

    override suspend fun getItemsByUser(userId: String): DataResult<List<Item>> =
        remote.getByUserId(userId).mapData { list -> list.map { it.toDomain() } }

    override suspend fun getItem(id: Long): DataResult<Item> =
        remote.getById(id).mapData { it.toDomain() }

    override suspend fun createItem(item: Item): DataResult<Item> =
        remote.insert(item.toCreateDto()).mapData { it.toDomain() }

    override suspend fun updateItem(item: Item): DataResult<Item> =
        remote.update(item.id, item.toDto()).mapData { it.toDomain() }

    override suspend fun deleteItem(id: Long): DataResult<Unit> =
        remote.delete(id)
}
