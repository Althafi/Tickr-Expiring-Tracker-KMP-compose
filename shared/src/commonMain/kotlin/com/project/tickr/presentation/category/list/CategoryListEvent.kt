package com.project.tickr.presentation.category.list

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface CategoryListEvent : UiEvent {
    data class NavigateToForm(val id: Long?) : CategoryListEvent
    data class ShowSnackbar(val message: String) : CategoryListEvent
}
