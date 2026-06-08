package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.validation.Validators
import com.project.tickr.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): DataResult<Unit> {
        val emailError = Validators.requireEmail(email, "email")
        if (emailError != null) return DataResult.Error(emailError)

        return repository.resetPassword(email)
    }
}
