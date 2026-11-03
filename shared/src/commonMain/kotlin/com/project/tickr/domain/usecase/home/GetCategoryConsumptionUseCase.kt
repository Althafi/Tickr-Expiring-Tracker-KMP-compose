package com.project.tickr.domain.usecase.home

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.CategorySlice
import com.project.tickr.domain.repository.CategoryRepository
import com.project.tickr.domain.repository.ItemRepository

class GetCategoryConsumptionUseCase(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(): DataResult<List<CategorySlice>> {
        val itemsResult = itemRepository.getItems()
        if (itemsResult is DataResult.Error) return itemsResult

        val categoriesResult = categoryRepository.getCategories()
        if (categoriesResult is DataResult.Error) return categoriesResult

        val consumedItems = (itemsResult as DataResult.Success).data.filter { it.isConsumed }
        val categories = (categoriesResult as DataResult.Success).data
        val total = consumedItems.size

        if (total == 0) return DataResult.Success(emptyList())

        val grouped = consumedItems.groupBy { it.categoryId }
        val slices = categories.mapNotNull { cat ->
            val count = grouped[cat.id]?.size ?: 0
            if (count == 0) null
            else CategorySlice(
                categoryId = cat.id,
                name = cat.name,
                colorHex = cat.colorHex,
                count = count,
                percentage = (count * 100 / total),
                key = cat.key,
            )
        }.sortedByDescending { it.count }

        return DataResult.Success(slices)
    }
}
