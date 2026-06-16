package com.project.tickr.domain.usecase.expiry

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.model.ExpiryFilter
import com.project.tickr.domain.model.ExpiringItem
import com.project.tickr.domain.model.TimeRange
import com.project.tickr.domain.model.daysToUrgency
import com.project.tickr.domain.repository.CategoryRepository
import com.project.tickr.domain.repository.ItemRepository

class GetExpiryListItemsUseCase(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val dateTime: DateTimeUtil,
) {
    suspend operator fun invoke(
        userId: String,
        filter: ExpiryFilter = ExpiryFilter(),
    ): DataResult<List<ExpiringItem>> {
        val itemsResult = itemRepository.getItemsByUser(userId)
        if (itemsResult is DataResult.Error) return itemsResult

        val categoryMap = when (val r = categoryRepository.getCategories()) {
            is DataResult.Success -> r.data.associateBy { it.id }
            is DataResult.Error -> emptyMap()
        }

        val all = (itemsResult as DataResult.Success).data
            .filter { !it.isConsumed }
            .map { item ->
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
            .sortedBy { it.daysUntilExpiry }
            .filter { item ->
                (filter.urgencies.isEmpty() || item.urgency in filter.urgencies) &&
                    (filter.timeRange == null || matchesTimeRange(item.daysUntilExpiry, filter.timeRange)) &&
                    (filter.categoryName == null || item.categoryName.equals(filter.categoryName, ignoreCase = true)) &&
                    (filter.query.isBlank() || item.name.contains(filter.query, ignoreCase = true))
            }

        return DataResult.Success(all)
    }

    private fun matchesTimeRange(days: Int, range: TimeRange): Boolean = when (range) {
        TimeRange.GT_6M -> days > 180
        TimeRange.GT_3M -> days > 90
        TimeRange.GT_30D -> days > 30
        TimeRange.GT_7D -> days > 7
        TimeRange.D0 -> days <= 0
    }
}
