package com.project.tickr.presentation.help

import org.jetbrains.compose.resources.StringResource

data class FaqItem(val id: String, val question: StringResource, val answer: StringResource)

data class HelpUiState(
    val faqs: List<FaqItem> = emptyList(),
    val expandedId: String? = null,
)
