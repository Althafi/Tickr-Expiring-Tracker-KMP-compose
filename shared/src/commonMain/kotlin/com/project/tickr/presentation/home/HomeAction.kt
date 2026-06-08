package com.project.tickr.presentation.home

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface HomeAction : UiAction {
    data object Load : HomeAction
    data object Refresh : HomeAction
    data class ClickItem(val id: Long) : HomeAction
}
