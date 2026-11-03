package com.project.tickr.data.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.getOrNull
import com.project.tickr.core.result.mapData
import com.project.tickr.core.result.onSuccess
import com.project.tickr.data.local.datasource.ItemLocalDataSource
import com.project.tickr.data.mapper.toDomain
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.repository.ItemRepository
import com.project.tickr.domain.storage.ImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LocalItemRepository(
    private val local: ItemLocalDataSource,
    private val images: ImageStorage,
) : ItemRepository {

    override suspend fun getItems(): DataResult<List<Item>> =
        local.getAll().mapData { rows -> rows.map { it.toDomain() } }

    override fun observeItems(): Flow<List<Item>> =
        local.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getItem(id: Long): DataResult<Item> =
        local.getById(id).mapData { it.toDomain() }

    override suspend fun createItem(item: Item): DataResult<Item> =
        local.insert(item).mapData { it.toDomain() }

    override suspend fun updateItem(item: Item): DataResult<Item> {
        val previousImage = local.getById(item.id).getOrNull()?.image_url
        return local.update(item).mapData { it.toDomain() }.onSuccess {
            // Foto diganti atau dihapus: file lama tidak boleh tertinggal.
            if (previousImage != null && previousImage != it.imageUrl) images.delete(previousImage)
        }
    }

    override suspend fun deleteItem(id: Long): DataResult<Unit> {
        val image = local.getById(id).getOrNull()?.image_url
        return local.delete(id).onSuccess {
            if (image != null) images.delete(image)
        }
    }
}
