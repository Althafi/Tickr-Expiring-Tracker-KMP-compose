package com.project.tickr.domain.usecase.item

import com.project.tickr.domain.model.Item
import com.project.tickr.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

/** Stream of all items; emits again whenever an item is added, edited, consumed, or deleted. */
class ObserveItemsUseCase(
    private val repository: ItemRepository
) {
    operator fun invoke(): Flow<List<Item>> = repository.observeItems()
}
