package com.project.tickr.presentation.auth

data class AuthUiState(
    val mode: Mode = Mode.SignIn,
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
) {
    enum class Mode { SignIn, SignUp }
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isSubmitting
}
