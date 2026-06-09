package com.project.tickr.domain.model

enum class PasswordStrength { None, Weak, Fair, Good, Strong }

data class PasswordRequirements(
    val hasMinLength: Boolean = false,
    val hasUpperAndLower: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSymbol: Boolean = false,
)

data class PasswordValidationResult(
    val strength: PasswordStrength,
    val requirements: PasswordRequirements,
)
