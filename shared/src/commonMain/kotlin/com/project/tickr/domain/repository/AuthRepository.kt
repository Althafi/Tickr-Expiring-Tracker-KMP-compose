package com.project.tickr.domain.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.AuthUser
import com.project.tickr.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Existing API (kept for backward compat — AuthViewModel & routing gate)
    suspend fun signUp(email: String, password: String): DataResult<AuthUser>
    suspend fun signIn(email: String, password: String): DataResult<AuthUser>
    suspend fun signOut(): DataResult<Unit>
    suspend fun resetPassword(email: String): DataResult<Unit>
    fun observeSession(): Flow<AuthUser?>
    fun currentUserId(): String?

    // Phase 3.5 Auth API
    suspend fun login(identifier: String, password: String): DataResult<UserSession>
    suspend fun register(fullName: String, email: String, password: String): DataResult<UserSession>
    suspend fun loginWithGoogle(): DataResult<UserSession> // TODO(user): implementasi Google/OAuth (Supabase) — jangan panggil apa pun
    suspend fun changePassword(newPassword: String): DataResult<Unit>
}
