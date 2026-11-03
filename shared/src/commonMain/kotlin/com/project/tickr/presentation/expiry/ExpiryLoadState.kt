package com.project.tickr.presentation.expiry

import com.project.tickr.presentation.common.UiText

sealed interface ExpiryLoadState {
    object Loading : ExpiryLoadState
    object Content : ExpiryLoadState
    object Empty : ExpiryLoadState
    data class Error(val message: UiText) : ExpiryLoadState
}
