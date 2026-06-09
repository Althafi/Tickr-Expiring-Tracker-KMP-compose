package com.project.tickr.data.repository

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.AuthUser
import com.project.tickr.domain.model.UserSession
import com.project.tickr.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// In-memory fake untuk mode slicing (tanpa DB/Supabase).
// TODO(user): ganti ke AuthRepositoryImpl di DataModule saat Supabase siap.
class FakeAuthRepository : AuthRepository {

    private val _session = MutableStateFlow<UserSession?>(null)

    // === Existing interface ===

    override suspend fun signUp(email: String, password: String): DataResult<AuthUser> =
        DataResult.Error(AppError.Unknown("Gunakan register() untuk FakeAuthRepository."))

    override suspend fun signIn(email: String, password: String): DataResult<AuthUser> =
        DataResult.Error(AppError.Unknown("Gunakan login() untuk FakeAuthRepository."))

    override suspend fun signOut(): DataResult<Unit> {
        _session.value = null
        return DataResult.Success(Unit)
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> =
        DataResult.Error(AppError.Unknown("Reset password tidak diimplementasikan di FakeAuthRepository."))

    override fun observeSession(): Flow<AuthUser?> =
        _session.map { session -> session?.let { AuthUser(it.userId, it.email) } }

    override fun currentUserId(): String? = _session.value?.userId

    // === Phase 3.5 Auth API ===

    // TODO(user): ganti aturan demo saat Supabase masuk.
    // Demo: identifier "demo@tickr.app" atau "Demo User" + password ≥8 char → sukses.
    override suspend fun login(identifier: String, password: String): DataResult<UserSession> {
        delay(1200)
        val isDemoUser = identifier.equals("demo@tickr.app", ignoreCase = true) ||
            identifier.equals("Demo User", ignoreCase = true)
        return if (isDemoUser && password.length >= 8) {
            val session = UserSession(
                userId = "demo-001",
                email = "demo@tickr.app",
                fullName = "Demo User",
                accessToken = "fake-token-login",
            )
            _session.value = session
            DataResult.Success(session)
        } else {
            DataResult.Error(AppError.Unauthorized)
        }
    }

    // TODO(user): ganti logika saat Supabase masuk.
    // Demo: email "taken@tickr.app" → gagal EmailTaken; lainnya → sukses.
    override suspend fun register(fullName: String, email: String, password: String): DataResult<UserSession> {
        delay(1200)
        return if (email.equals("taken@tickr.app", ignoreCase = true)) {
            DataResult.Error(AppError.Validation("email", "Email ini sudah terdaftar."))
        } else {
            val session = UserSession(
                userId = "user-${email.hashCode()}",
                email = email,
                fullName = fullName,
                accessToken = "fake-token-register",
            )
            _session.value = session
            DataResult.Success(session)
        }
    }

    // TODO(user): implementasi Google/OAuth (Supabase) — jangan panggil apa pun
    override suspend fun loginWithGoogle(): DataResult<UserSession> =
        DataResult.Error(AppError.Unknown("Google OAuth belum diimplementasikan."))
}
