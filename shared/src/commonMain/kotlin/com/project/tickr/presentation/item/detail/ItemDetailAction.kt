package com.project.tickr.presentation.item.detail

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface ItemDetailAction : UiAction {
    data class Load(val id: Long) : ItemDetailAction
    data object Delete : ItemDetailAction
    data object MarkConsumed : ItemDetailAction
    data object ClickEdit : ItemDetailAction
}
