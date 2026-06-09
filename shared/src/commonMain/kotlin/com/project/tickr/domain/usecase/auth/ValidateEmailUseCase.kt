package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.validation.ValidationResult

class ValidateEmailUseCase {
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult.Invalid("email", "Email tidak boleh kosong.")
        val regex = Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
        return if (regex.matches(email)) ValidationResult.Valid
        else ValidationResult.Invalid("email", "Format email tidak valid.")
    }
}
