package com.project.tickr.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateItemDto(
    @SerialName("user_id") val userId: String,
    @SerialName("category_id") val categoryId: Long? = null,
    @SerialName("name") val name: String,
    @SerialName("barcode") val barcode: String? = null,
    @SerialName("expiry_date") val expiryDate: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("is_consumed") val isConsumed: Boolean,
    @SerialName("quantity") val quantity: Int = 1,
    @SerialName("unit") val unit: String = "Pcs",
)
