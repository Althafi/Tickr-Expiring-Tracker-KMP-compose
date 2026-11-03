package com.project.tickr.domain.usecase.home

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.core.validation.Validators
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.repository.ItemRepository

data class AddItemParams(
    val name: String,
    val categoryId: Long?,
    val quantity: Int,
    val unit: String,
    val expiryDateMillis: Long?,
    val imageUrl: String?,
    val notes: String? = null,
    val barcode: String? = null,
)

class AddItemUseCase(
    private val itemRepository: ItemRepository,
    private val dateTime: DateTimeUtil,
) {
    suspend operator fun invoke(params: AddItemParams): DataResult<Unit> {
        Validators.requireNotBlank(params.name, "name")?.let { return DataResult.Error(it) }
        if (params.expiryDateMillis == null) {
            return DataResult.Error(AppError.Validation("expiry_date", "must not be empty"))
        }
        val isoDate = dateTime.isoDateFromMillis(params.expiryDateMillis)
        Validators.requireIsoDate(isoDate, "expiry_date")?.let { return DataResult.Error(it) }
        if (params.quantity < 1) {
            return DataResult.Error(AppError.Validation("quantity", "must be at least 1"))
        }
        Validators.requireNotBlank(params.unit, "unit")?.let { return DataResult.Error(it) }

        val item = Item(
            id = 0L,
            categoryId = params.categoryId,
            name = params.name.trim(),
            barcode = params.barcode?.trim()?.takeIf { it.isNotEmpty() },
            expiryDate = isoDate,
            imageUrl = params.imageUrl,
            notes = params.notes,
            isConsumed = false,
            quantity = params.quantity,
            unit = params.unit,
            createdAt = "",
            updatedAt = "",
        )
        return when (val result = itemRepository.createItem(item)) {
            is DataResult.Success -> DataResult.Success(Unit)
            is DataResult.Error -> result
        }
    }
}
