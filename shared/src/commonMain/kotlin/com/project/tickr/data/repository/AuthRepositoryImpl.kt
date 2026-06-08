package com.project.tickr.data.repository

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.AuthUser
import com.project.tickr.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class AuthRepositoryImpl(
    private val client: SupabaseClient
) : AuthRepository {

    override suspend fun signUp(email: String, password: String): DataResult<AuthUser> {
        return DataResult.Error(AppError.Unknown("Auth not yet fully implemented"))
    }

    override suspend fun signIn(email: String, password: String): DataResult<AuthUser> {
        return DataResult.Error(AppError.Unknown("Auth not yet fully implemented"))
    }

    override suspend fun signOut(): DataResult<Unit> {
        return DataResult.Error(AppError.Unknown("Auth not yet fully implemented"))
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> {
        return DataResult.Error(AppError.Unknown("Auth not yet fully implemented"))
    }

    override fun observeSession(): Flow<AuthUser?> = emptyFlow()

    override fun currentUserId(): String? = null
}
