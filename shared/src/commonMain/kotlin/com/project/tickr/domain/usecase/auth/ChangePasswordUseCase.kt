package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.repository.AuthRepository

class ChangePasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(newPassword: String): DataResult<Unit> =
        authRepository.changePassword(newPassword)
}
