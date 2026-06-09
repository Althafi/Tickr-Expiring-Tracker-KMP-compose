package com.project.tickr.data.repository

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.AuthUser
import com.project.tickr.domain.model.UserSession
import com.project.tickr.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// TODO(user): Aktifkan SupabaseAuthRepository ini saat Supabase Auth siap.
// Ganti FakeAuthRepository → SupabaseAuthRepository di DataModule (DI) saja;
// domain & presentation TIDAK berubah.
class AuthRepositoryImpl(
    private val client: SupabaseClient
) : AuthRepository {

    override suspend fun signUp(email: String, password: String): DataResult<AuthUser> =
        DataResult.Error(AppError.Unknown("Use register() — Supabase not yet wired"))

    override suspend fun signIn(email: String, password: String): DataResult<AuthUser> =
        DataResult.Error(AppError.Unknown("Use login() — Supabase not yet wired"))

    override suspend fun signOut(): DataResult<Unit> =
        DataResult.Error(AppError.Unknown("Supabase not yet wired"))

    override suspend fun resetPassword(email: String): DataResult<Unit> =
        DataResult.Error(AppError.Unknown("Supabase not yet wired"))

    override fun observeSession(): Flow<AuthUser?> = emptyFlow()

    override fun currentUserId(): String? = null

    // TODO(user): implementasi dengan gotrue (supabase-kt)
    override suspend fun login(identifier: String, password: String): DataResult<UserSession> =
        DataResult.Error(AppError.Unknown("Supabase not yet wired"))

    // TODO(user): implementasi dengan gotrue (supabase-kt)
    override suspend fun register(fullName: String, email: String, password: String): DataResult<UserSession> =
        DataResult.Error(AppError.Unknown("Supabase not yet wired"))

    // TODO(user): implementasi Google/OAuth (Supabase) — jangan panggil apa pun
    override suspend fun loginWithGoogle(): DataResult<UserSession> =
        DataResult.Error(AppError.Unknown("Google OAuth not implemented"))
}
