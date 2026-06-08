package com.project.tickr.presentation.category.form

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface CategoryFormEvent : UiEvent {
    data object Saved : CategoryFormEvent
    data class ShowError(val message: String) : CategoryFormEvent
}
