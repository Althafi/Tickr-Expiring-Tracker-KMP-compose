package com.project.tickr.presentation.home

import com.project.tickr.presentation.common.mvi.UiEvent
import com.project.tickr.presentation.navigation.Destination

sealed interface HomeEvent : UiEvent {
    data class NavigateToItemDetail(val destination: Destination) : HomeEvent
    data object NavigateToNotifications : HomeEvent
    data object ShowAddItemSheet : HomeEvent
    data object NavigateToConsumedItems : HomeEvent
    data class ShowError(val message: String) : HomeEvent
}
