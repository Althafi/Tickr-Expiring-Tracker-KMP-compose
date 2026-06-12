package com.project.tickr.presentation.changepassword

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface ChangePasswordAction : UiAction {
    data class CurrentChanged(val value: String) : ChangePasswordAction
    data class NewChanged(val value: String) : ChangePasswordAction
    data class ConfirmChanged(val value: String) : ChangePasswordAction
    data object ToggleCurrentVisibility : ChangePasswordAction
    data object ToggleNewVisibility : ChangePasswordAction
    data object ToggleConfirmVisibility : ChangePasswordAction
    data object Submit : ChangePasswordAction
    data object Retry : ChangePasswordAction
    data object BackToProfile : ChangePasswordAction
    data object Back : ChangePasswordAction
}
