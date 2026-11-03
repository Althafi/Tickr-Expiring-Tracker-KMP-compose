package com.project.tickr.presentation.additem

import com.project.tickr.presentation.common.mvi.UiEvent
import com.project.tickr.presentation.common.UiText

sealed interface AddItemEvent : UiEvent {
    data object Saved : AddItemEvent
    data object Dismissed : AddItemEvent
    data class ShowError(val message: UiText) : AddItemEvent
}
