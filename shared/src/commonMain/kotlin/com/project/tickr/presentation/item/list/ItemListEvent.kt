package com.project.tickr.presentation.item.list

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface ItemListEvent : UiEvent {
    data class NavigateToDetail(val id: Long) : ItemListEvent
    data class NavigateToCreate(val destination: String) : ItemListEvent
    data class ShowSnackbar(val message: String) : ItemListEvent
}
