package com.project.tickr.presentation.expiry.detail

sealed interface ExpiryDetailEvent {
    object NavigateBack : ExpiryDetailEvent
    object ItemRemoved : ExpiryDetailEvent
    data class ShowError(val message: String) : ExpiryDetailEvent
}
