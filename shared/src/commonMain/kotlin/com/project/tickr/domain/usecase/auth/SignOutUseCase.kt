package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.repository.AuthRepository

class SignOutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): DataResult<Unit> = repository.signOut()
}
