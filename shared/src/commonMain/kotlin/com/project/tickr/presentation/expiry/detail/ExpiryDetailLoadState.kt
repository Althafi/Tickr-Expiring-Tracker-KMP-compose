package com.project.tickr.presentation.expiry.detail

sealed interface ExpiryDetailLoadState {
    object Loading : ExpiryDetailLoadState
    object Content : ExpiryDetailLoadState
    data class Error(val message: String) : ExpiryDetailLoadState
}
