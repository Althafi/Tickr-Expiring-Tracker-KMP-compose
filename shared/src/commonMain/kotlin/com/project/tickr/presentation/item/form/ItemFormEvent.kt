package com.project.tickr.presentation.item.form

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface ItemFormEvent : UiEvent {
    data object Saved : ItemFormEvent
    data class ShowError(val message: String) : ItemFormEvent
}
