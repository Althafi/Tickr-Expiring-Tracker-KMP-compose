package com.project.tickr.domain.usecase.item

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.mapData
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.repository.ItemRepository

class GetExpiringItemsUseCase(
    private val repository: ItemRepository,
    private val dateTime: DateTimeUtil
) {
    suspend operator fun invoke(withinDays: Int = 7): DataResult<List<Item>> =
        repository.getItems().mapData { items ->
            items.filter { !it.isConsumed && dateTime.isWithinDays(it.expiryDate, withinDays) }
                 .sortedBy { it.expiryDate }
        }
}
