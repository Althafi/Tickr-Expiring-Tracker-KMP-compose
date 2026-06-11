package com.project.tickr.presentation.expiry

sealed interface ExpiryLoadState {
    object Loading : ExpiryLoadState
    object Content : ExpiryLoadState
    object Empty : ExpiryLoadState
    data class Error(val message: String) : ExpiryLoadState
}
