package com.project.tickr.presentation.help

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface HelpAction : UiAction {
    data class ToggleFaq(val id: String) : HelpAction
    data object ContactEmail : HelpAction
    data object ContactWhatsApp : HelpAction
    data object Back : HelpAction
}
