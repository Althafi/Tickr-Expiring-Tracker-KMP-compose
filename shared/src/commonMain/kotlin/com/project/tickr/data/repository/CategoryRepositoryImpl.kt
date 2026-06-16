package com.project.tickr.data.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.mapData
import com.project.tickr.data.mapper.toDomain
import com.project.tickr.data.mapper.toDto
import com.project.tickr.data.remote.datasource.CategoryRemoteDataSource
import com.project.tickr.domain.model.Category
import com.project.tickr.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val remote: CategoryRemoteDataSource
) : CategoryRepository {

    override suspend fun getCategories(): DataResult<List<Category>> =
        remote.getAll().mapData { list -> list.map { it.toDomain() }.sortedBy { it.id } }

    override suspend fun getCategoriesByUser(userId: String): DataResult<List<Category>> =
        remote.getByUserId(userId).mapData { list -> list.map { it.toDomain() } }

    override suspend fun getCategory(id: Long): DataResult<Category> =
        remote.getById(id).mapData { it.toDomain() }

    override suspend fun createCategory(category: Category): DataResult<Category> =
        remote.insert(category.toDto()).mapData { it.toDomain() }

    override suspend fun updateCategory(category: Category): DataResult<Category> =
        remote.update(category.id, category.toDto()).mapData { it.toDomain() }

    override suspend fun deleteCategory(id: Long): DataResult<Unit> =
        remote.delete(id)
}
