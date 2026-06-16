package com.project.tickr.domain.usecase.home

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.model.CategoryGroup
import com.project.tickr.domain.model.ExpiringItem
import com.project.tickr.domain.model.Urgency
import com.project.tickr.domain.model.daysToUrgency
import com.project.tickr.domain.repository.CategoryRepository
import com.project.tickr.domain.repository.ItemRepository

class GetExpiringItemsGroupedUseCase(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val dateTime: DateTimeUtil,
) {
    suspend operator fun invoke(userId: String): DataResult<List<CategoryGroup>> {
        val itemsResult = itemRepository.getItemsByUser(userId)
        if (itemsResult is DataResult.Error) return itemsResult

        val categoryMap = when (val r = categoryRepository.getCategories()) {
            is DataResult.Success -> r.data.associateBy { it.id }
            is DataResult.Error -> emptyMap()
        }

        val items = (itemsResult as DataResult.Success).data.filter { !it.isConsumed }

        val expiringItems = items.map { item ->
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
        }
            .filter { it.urgency != Urgency.SAFE }
            .sortedBy { it.daysUntilExpiry }

        val groups = expiringItems
            .groupBy { it.categoryName }
            .map { (catName, groupItems) ->
                CategoryGroup(
                    categoryName = catName,
                    colorHex = groupItems.first().categoryColorHex,
                    items = groupItems,
                )
            }

        return DataResult.Success(groups)
    }
}
