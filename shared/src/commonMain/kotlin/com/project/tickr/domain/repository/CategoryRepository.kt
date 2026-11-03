package com.project.tickr.domain.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): DataResult<List<Category>>
    suspend fun getCategory(id: Long): DataResult<Category>
    suspend fun createCategory(category: Category): DataResult<Category>
    suspend fun updateCategory(category: Category): DataResult<Category>
    suspend fun deleteCategory(id: Long): DataResult<Unit>
}
