package com.project.tickr.domain.usecase.auth

import com.project.tickr.domain.repository.AuthRepository

class GetCurrentUserIdUseCase(private val repository: AuthRepository) {
    operator fun invoke(): String? = repository.currentUserId()
}
