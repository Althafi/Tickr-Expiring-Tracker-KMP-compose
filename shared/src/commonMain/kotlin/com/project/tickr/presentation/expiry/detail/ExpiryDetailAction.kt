package com.project.tickr.presentation.expiry.detail

sealed interface ExpiryDetailAction {
    data class Load(val id: Long) : ExpiryDetailAction
    object Back : ExpiryDetailAction
    object Delete : ExpiryDetailAction
    object ConfirmDelete : ExpiryDetailAction
    object DismissDeleteDialog : ExpiryDetailAction
    object MarkConsumed : ExpiryDetailAction
}
