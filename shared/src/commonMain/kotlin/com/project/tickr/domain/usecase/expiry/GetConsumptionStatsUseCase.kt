package com.project.tickr.domain.usecase.expiry

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.mapData
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.model.ConsumptionStats
import com.project.tickr.domain.repository.ItemRepository

class GetConsumptionStatsUseCase(
    private val itemRepository: ItemRepository,
    private val dateTime: DateTimeUtil,
) {
    suspend operator fun invoke(userId: String): DataResult<ConsumptionStats> =
        itemRepository.getItemsByUser(userId).mapData { items ->
            ConsumptionStats(
                consumed = items.count { it.isConsumed },
                wasted = items.count { !it.isConsumed && dateTime.daysUntil(it.expiryDate) < 0 },
            )
        }
}
