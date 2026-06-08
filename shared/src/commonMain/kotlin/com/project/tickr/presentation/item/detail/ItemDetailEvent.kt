package com.project.tickr.presentation.item.detail

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface ItemDetailEvent : UiEvent {
    data object NavigateBack : ItemDetailEvent
    data class NavigateToEdit(val id: Long) : ItemDetailEvent
    data class ShowSnackbar(val message: String) : ItemDetailEvent
}
