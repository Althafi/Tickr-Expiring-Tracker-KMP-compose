package com.project.tickr.presentation.help

data class FaqItem(val id: String, val question: String, val answer: String)

data class HelpUiState(
    val faqs: List<FaqItem> = emptyList(),
    val expandedId: String? = null,
)
