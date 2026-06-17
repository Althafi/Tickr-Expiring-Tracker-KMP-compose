package com.project.tickr.presentation.consumed

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface ConsumedItemsAction : UiAction {
    data object Load : ConsumedItemsAction
    data object Refresh : ConsumedItemsAction
    data object Back : ConsumedItemsAction
}
