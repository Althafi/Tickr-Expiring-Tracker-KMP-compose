package com.project.tickr.presentation.consumed

sealed interface ConsumedItemsLoadState {
    data object Loading : ConsumedItemsLoadState
    data object Content : ConsumedItemsLoadState
    data class Error(val message: String) : ConsumedItemsLoadState
}
