package com.project.tickr.presentation.changepassword

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface ChangePasswordEvent : UiEvent {
    data object NavigateBackToProfile : ChangePasswordEvent
    data object NavigateBack : ChangePasswordEvent
}
