package com.project.tickr.presentation.register

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface RegisterAction : UiAction {
    data class FullNameChanged(val value: String) : RegisterAction
    data class EmailChanged(val value: String) : RegisterAction
    data class PasswordChanged(val value: String) : RegisterAction
    data object TogglePasswordVisibility : RegisterAction
    data object Submit : RegisterAction
    data object GoogleClicked : RegisterAction
    data object NavigateToLogin : RegisterAction
}
