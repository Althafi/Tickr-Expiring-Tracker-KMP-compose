package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.validation.Validators
import com.project.tickr.domain.model.AuthUser
import com.project.tickr.domain.repository.AuthRepository

class SignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): DataResult<AuthUser> {
        val emailError = Validators.requireNotBlank(email, "email")
        if (emailError != null) return DataResult.Error(emailError)

        val passwordError = Validators.requireNotBlank(password, "password")
        if (passwordError != null) return DataResult.Error(passwordError)

        return repository.signIn(email, password)
    }
}
