package com.project.tickr.presentation.item.form

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface ItemFormAction : UiAction {
    data class NameChanged(val value: String) : ItemFormAction
    data class ExpiryDateChanged(val value: String) : ItemFormAction
    data class CategoryChanged(val categoryId: Long?) : ItemFormAction
    data class NotesChanged(val value: String) : ItemFormAction
    data object Submit : ItemFormAction
}
