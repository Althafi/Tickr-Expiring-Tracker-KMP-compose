package com.project.tickr.presentation.auth

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface AuthAction : UiAction {
    data class EmailChanged(val value: String) : AuthAction
    data class PasswordChanged(val value: String) : AuthAction
    data object ToggleMode : AuthAction
    data object Submit : AuthAction
    data object SignOut : AuthAction
}
