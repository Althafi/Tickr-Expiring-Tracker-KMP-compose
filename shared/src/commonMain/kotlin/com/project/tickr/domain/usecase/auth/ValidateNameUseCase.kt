package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.validation.ValidationResult

class ValidateNameUseCase {
    operator fun invoke(name: String): ValidationResult {
        if (name.isBlank()) return ValidationResult.Invalid("name", "Nama tidak boleh kosong.")
        if (name.trim().length < 2) return ValidationResult.Invalid("name", "Nama minimal 2 karakter.")
        return ValidationResult.Valid
    }
}
