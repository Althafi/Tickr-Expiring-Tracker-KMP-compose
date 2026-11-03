package com.project.tickr.domain.model

data class CategoryGroup(
    val categoryName: String,
    val colorHex: String,
    val items: List<ExpiringItem>,
    val categoryKey: String? = null,
)
