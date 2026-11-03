package com.project.tickr.domain.model

data class Item(
    val id: Long,
    val userId: String,
    val categoryId: Long?,
    val name: String,
    val barcode: String?,
    val expiryDate: String,
    val imageUrl: String?,
    val notes: String?,
    val isConsumed: Boolean,
    val quantity: Int = 1,
    val unit: String = ItemUnits.DEFAULT,
    val createdAt: String,
    val updatedAt: String
)
