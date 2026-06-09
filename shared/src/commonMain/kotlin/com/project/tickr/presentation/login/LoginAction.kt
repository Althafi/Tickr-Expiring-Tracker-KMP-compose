package com.project.tickr.presentation.login

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface LoginAction : UiAction {
    data class IdentifierChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object TogglePasswordVisibility : LoginAction
    data object Submit : LoginAction
    data object GoogleClicked : LoginAction
    data object NavigateToRegister : LoginAction
}
