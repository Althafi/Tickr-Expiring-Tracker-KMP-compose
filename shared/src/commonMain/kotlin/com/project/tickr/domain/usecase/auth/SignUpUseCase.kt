package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.core.validation.Validators
import com.project.tickr.domain.model.AuthUser
import com.project.tickr.domain.repository.AuthRepository

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): DataResult<AuthUser> {
        val emailError = Validators.requireEmail(email, "email")
        if (emailError != null) return DataResult.Error(emailError)

        val passwordError = Validators.requireMinLength(password, 6, "password")
        if (passwordError != null) return DataResult.Error(passwordError)

        return repository.signUp(email, password)
    }
}
