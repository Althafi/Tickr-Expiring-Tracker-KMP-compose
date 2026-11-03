package com.project.tickr.presentation.expiry

import com.project.tickr.presentation.common.UiText

sealed interface ExpiryEvent {
    data class NavigateToDetail(val id: Long) : ExpiryEvent
    object NavigateToAddItem : ExpiryEvent
    data class ShowError(val message: UiText) : ExpiryEvent
}
