package com.project.tickr.presentation.consumed

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface ConsumedItemsEvent : UiEvent {
    data object NavigateBack : ConsumedItemsEvent
}
