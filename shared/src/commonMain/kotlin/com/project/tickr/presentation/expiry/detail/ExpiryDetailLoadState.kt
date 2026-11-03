package com.project.tickr.presentation.expiry.detail

import com.project.tickr.presentation.common.UiText

sealed interface ExpiryDetailLoadState {
    object Loading : ExpiryDetailLoadState
    object Content : ExpiryDetailLoadState
    data class Error(val message: UiText) : ExpiryDetailLoadState
}
