package com.project.tickr.domain.usecase.insight

import com.project.tickr.core.result.getOrNull
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.insight.InsightCalculator
import com.project.tickr.domain.model.InsightData
import com.project.tickr.domain.repository.CategoryRepository
import com.project.tickr.domain.repository.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/** Wawasan yang selalu mengikuti perubahan data barang. Murni lokal, jalan tanpa internet. */
class ObserveInsightsUseCase(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val dateTime: DateTimeUtil,
) {
    operator fun invoke(): Flow<InsightData> =
        itemRepository.observeItems()
            .map { items ->
                val categories = categoryRepository.getCategories().getOrNull().orEmpty()
                InsightCalculator.compute(items, categories, LocalDate.parse(dateTime.today()))
            }
            .flowOn(Dispatchers.Default)
}
