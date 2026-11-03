package com.project.tickr.domain.usecase.home

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.mapData
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.model.WasteTrend
import com.project.tickr.domain.repository.ItemRepository

class GetWasteTrendUseCase(
    private val itemRepository: ItemRepository,
    private val dateTime: DateTimeUtil,
) {
    suspend operator fun invoke(): DataResult<WasteTrend?> =
        itemRepository.getItems().mapData { items ->
            val today = dateTime.today() // "YYYY-MM-DD"
            val parts = today.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val thisMonthPrefix = "$year-${month.toString().padStart(2, '0')}"
            val (lastYear, lastMonth) = if (month == 1) (year - 1) to 12 else year to (month - 1)
            val lastMonthPrefix = "$lastYear-${lastMonth.toString().padStart(2, '0')}"

            // "Wasted" = expired without being consumed
            val wasted = items.filter { !it.isConsumed && dateTime.isPast(it.expiryDate) }
            val thisCount = wasted.count { it.expiryDate.startsWith(thisMonthPrefix) }
            val lastCount = wasted.count { it.expiryDate.startsWith(lastMonthPrefix) }

            if (lastCount == 0 && thisCount == 0) null
            else {
                val deltaPct = if (lastCount == 0) 0
                else ((thisCount - lastCount) * 100 / lastCount)
                WasteTrend(
                    deltaPercent = kotlin.math.abs(deltaPct),
                    isImproving = deltaPct <= 0,
                )
            }
        }
}
