package com.project.tickr.presentation.expiry

sealed interface ExpiryEvent {
    data class NavigateToDetail(val id: Long) : ExpiryEvent
    object NavigateToAddItem : ExpiryEvent
    data class ShowError(val message: String) : ExpiryEvent
}
