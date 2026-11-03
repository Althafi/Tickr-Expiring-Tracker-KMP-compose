package com.project.tickr.data.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.mapData
import com.project.tickr.data.local.datasource.CategoryLocalDataSource
import com.project.tickr.data.mapper.toDomain
import com.project.tickr.domain.model.Category
import com.project.tickr.domain.repository.CategoryRepository

internal class LocalCategoryRepository(
    private val local: CategoryLocalDataSource,
) : CategoryRepository {

    override suspend fun getCategories(): DataResult<List<Category>> =
        local.getAll().mapData { rows -> rows.map { it.toDomain() } }

    override suspend fun getCategory(id: Long): DataResult<Category> =
        local.getById(id).mapData { it.toDomain() }

    override suspend fun createCategory(category: Category): DataResult<Category> =
        local.insert(category).mapData { it.toDomain() }

    override suspend fun updateCategory(category: Category): DataResult<Category> =
        local.update(category).mapData { it.toDomain() }

    override suspend fun deleteCategory(id: Long): DataResult<Unit> =
        local.delete(id)
}
