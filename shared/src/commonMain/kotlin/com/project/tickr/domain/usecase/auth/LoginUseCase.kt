package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.UserSession
import com.project.tickr.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(identifier: String, password: String): DataResult<UserSession> {
        if (identifier.isBlank()) return DataResult.Error(AppError.Validation("identifier", "Identifier tidak boleh kosong."))
        if (password.isBlank()) return DataResult.Error(AppError.Validation("password", "Password tidak boleh kosong."))
        return repository.login(identifier, password)
    }
}
