package com.project.tickr.presentation.expiry.detail

sealed interface ExpiryDetailEvent {
    object NavigateBack : ExpiryDetailEvent
    object ItemRemoved : ExpiryDetailEvent
}
