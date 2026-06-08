package com.project.tickr.domain.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String): DataResult<AuthUser>
    suspend fun signIn(email: String, password: String): DataResult<AuthUser>
    suspend fun signOut(): DataResult<Unit>
    suspend fun resetPassword(email: String): DataResult<Unit>
    fun observeSession(): Flow<AuthUser?>
    fun currentUserId(): String?
}
