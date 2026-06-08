package com.project.tickr.presentation.profile

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface ProfileAction : UiAction {
    data object Load : ProfileAction
    data class FullNameChanged(val value: String) : ProfileAction
    data object Save : ProfileAction
    data object SignOut : ProfileAction
}
