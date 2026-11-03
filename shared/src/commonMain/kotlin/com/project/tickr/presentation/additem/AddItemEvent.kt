package com.project.tickr.presentation.additem

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface AddItemEvent : UiEvent {
    data object Saved : AddItemEvent
    data object Dismissed : AddItemEvent
}
