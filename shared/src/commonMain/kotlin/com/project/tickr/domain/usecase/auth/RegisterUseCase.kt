package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.UserSession
import com.project.tickr.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
    ): DataResult<UserSession> {
        if (fullName.isBlank()) return DataResult.Error(AppError.Validation("fullName", "Nama tidak boleh kosong."))
        if (email.isBlank()) return DataResult.Error(AppError.Validation("email", "Email tidak boleh kosong."))
        if (password.isBlank()) return DataResult.Error(AppError.Validation("password", "Password tidak boleh kosong."))
        return repository.register(fullName, email, password)
    }
}
