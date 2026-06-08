package com.project.tickr.presentation.category.list

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface CategoryListAction : UiAction {
    data object Load : CategoryListAction
    data class Delete(val id: Long) : CategoryListAction
    data object ClickAdd : CategoryListAction
    data class ClickCategory(val id: Long) : CategoryListAction
}
