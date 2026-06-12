package com.project.tickr.presentation.editprofile

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface EditProfileAction : UiAction {
    data class NameChanged(val value: String) : EditProfileAction
    data object ShowAvatarPicker : EditProfileAction
    data class AvatarSelected(val seed: String) : EditProfileAction
    data object DismissAvatarPicker : EditProfileAction
    data object OpenChangePassword : EditProfileAction
    data object Save : EditProfileAction
    data object Back : EditProfileAction
}
