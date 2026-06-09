package com.project.tickr.domain.usecase.auth

import com.project.tickr.domain.model.PasswordRequirements
import com.project.tickr.domain.model.PasswordStrength
import com.project.tickr.domain.model.PasswordValidationResult

class ValidatePasswordUseCase {
    operator fun invoke(password: String): PasswordValidationResult {
        val hasMinLength = password.length >= 8
        val hasUpperAndLower = password.any { it.isUpperCase() } && password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { it in "!@#\$%^&*()_+-=[]{}|;':\",./<>?" }

        val requirements = PasswordRequirements(hasMinLength, hasUpperAndLower, hasDigit, hasSymbol)

        val metCount = listOf(hasMinLength, hasUpperAndLower, hasDigit, hasSymbol).count { it }
        val strength = when {
            password.isEmpty() -> PasswordStrength.None
            metCount == 1 -> PasswordStrength.Weak
            metCount == 2 -> PasswordStrength.Fair
            metCount == 3 -> PasswordStrength.Good
            else -> PasswordStrength.Strong
        }

        return PasswordValidationResult(strength, requirements)
    }
}
