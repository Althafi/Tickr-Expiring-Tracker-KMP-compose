package com.project.tickr.presentation.login

import com.project.tickr.presentation.common.UiText

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val identifierError: UiText? = null,
    val passwordError: UiText? = null,
    val isLoading: Boolean = false,
) {
    val isSubmitEnabled: Boolean
        get() = identifier.isNotBlank() && password.isNotBlank() && !isLoading
}
