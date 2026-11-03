package com.project.tickr.presentation.consumed

import com.project.tickr.presentation.common.UiText

sealed interface ConsumedItemsLoadState {
    data object Loading : ConsumedItemsLoadState
    data object Content : ConsumedItemsLoadState
    data class Error(val message: UiText) : ConsumedItemsLoadState
}
