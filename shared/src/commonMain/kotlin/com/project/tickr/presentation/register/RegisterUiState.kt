package com.project.tickr.presentation.register

import com.project.tickr.domain.model.PasswordRequirements
import com.project.tickr.domain.model.PasswordStrength
import com.project.tickr.presentation.common.UiText

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val fullNameError: UiText? = null,
    val emailError: UiText? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.None,
    val passwordRequirements: PasswordRequirements = PasswordRequirements(),
    val isLoading: Boolean = false,
) {
    // TODO(user): sesuaikan ambang minimum kekuatan password (sekarang Fair = ordinal ≥ 2).
    val isSubmitEnabled: Boolean
        get() = !isLoading &&
            fullName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            passwordStrength.ordinal >= PasswordStrength.Fair.ordinal
}
