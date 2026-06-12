package com.project.tickr.presentation.editprofile

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface EditProfileEvent : UiEvent {
    data object NavigateBack : EditProfileEvent
    data object NavigateToChangePassword : EditProfileEvent
    data class ShowSnackbar(val message: String) : EditProfileEvent
}
