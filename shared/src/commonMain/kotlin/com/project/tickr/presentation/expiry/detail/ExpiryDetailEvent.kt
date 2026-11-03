package com.project.tickr.presentation.expiry.detail

import com.project.tickr.presentation.common.UiText

sealed interface ExpiryDetailEvent {
    object NavigateBack : ExpiryDetailEvent
    object ItemRemoved : ExpiryDetailEvent
    data class ShowError(val message: UiText) : ExpiryDetailEvent
}
