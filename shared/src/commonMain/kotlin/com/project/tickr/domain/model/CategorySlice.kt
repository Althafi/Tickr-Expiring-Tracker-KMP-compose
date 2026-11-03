package com.project.tickr.domain.model

data class CategorySlice(
    val categoryId: Long,
    val name: String,
    val colorHex: String,
    val count: Int,
    val percentage: Int,
    val key: String? = null,
)
