package com.project.tickr.domain.usecase.consumed

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.model.ExpiringItem
import com.project.tickr.domain.model.daysToUrgency
import com.project.tickr.domain.repository.CategoryRepository
import com.project.tickr.domain.repository.ItemRepository

class GetConsumedItemsUseCase(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val dateTime: DateTimeUtil,
) {
    suspend operator fun invoke(userId: String): DataResult<List<ExpiringItem>> {
        val itemsResult = itemRepository.getItemsByUser(userId)
        if (itemsResult is DataResult.Error) return itemsResult

        val categoryMap = when (val r = categoryRepository.getCategories()) {
            is DataResult.Success -> r.data.associateBy { it.id }
            is DataResult.Error -> emptyMap()
        }

        val consumed = (itemsResult as DataResult.Success).data.filter { it.isConsumed }

        val result = consumed.map { item ->
            val cat = categoryMap[item.categoryId]
            val days = dateTime.daysUntil(item.expiryDate)
            ExpiringItem(
                id = item.id,
                name = item.name,
                categoryId = item.categoryId,
                categoryName = cat?.name ?: "Lainnya",
                categoryColorHex = cat?.colorHex ?: "#6B7280",
                expiryDate = item.expiryDate,
                imageUrl = item.imageUrl,
                quantity = item.quantity,
                unit = item.unit,
                daysUntilExpiry = days,
                urgency = daysToUrgency(days),
            )
        }.sortedByDescending { it.id }

        return DataResult.Success(result)
    }
}
