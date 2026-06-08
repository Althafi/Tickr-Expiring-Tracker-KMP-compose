package com.project.tickr.presentation.item.list

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface ItemListAction : UiAction {
    data object Load : ItemListAction
    data object Refresh : ItemListAction
    data class Delete(val id: Long) : ItemListAction
    data class MarkConsumed(val id: Long) : ItemListAction
    data class ClickItem(val id: Long) : ItemListAction
    data object ClickAdd : ItemListAction
}
