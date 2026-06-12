package com.project.tickr.data.mapper

import com.project.tickr.data.remote.dto.CreateItemDto
import com.project.tickr.data.remote.dto.ItemDto
import com.project.tickr.domain.model.Item

fun ItemDto.toDomain() = Item(
    id = id,
    userId = userId,
    categoryId = categoryId,
    name = name,
    barcode = barcode,
    expiryDate = expiryDate,
    imageUrl = imageUrl,
    notes = notes,
    isConsumed = isConsumed,
    quantity = quantity,
    unit = unit,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Item.toDto() = ItemDto(
    id = id,
    userId = userId,
    categoryId = categoryId,
    name = name,
    barcode = barcode,
    expiryDate = expiryDate,
    imageUrl = imageUrl,
    notes = notes,
    isConsumed = isConsumed,
    quantity = quantity,
    unit = unit,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Item.toCreateDto() = CreateItemDto(
    userId = userId,
    categoryId = categoryId,
    name = name,
    barcode = barcode,
    expiryDate = expiryDate,
    imageUrl = imageUrl,
    notes = notes,
    isConsumed = isConsumed,
    quantity = quantity,
    unit = unit,
)
