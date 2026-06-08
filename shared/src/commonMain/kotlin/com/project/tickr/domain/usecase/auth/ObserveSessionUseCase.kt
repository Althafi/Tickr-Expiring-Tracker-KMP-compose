package com.project.tickr.domain.usecase.auth

import com.project.tickr.domain.model.AuthUser
import com.project.tickr.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<AuthUser?> = repository.observeSession()
}
