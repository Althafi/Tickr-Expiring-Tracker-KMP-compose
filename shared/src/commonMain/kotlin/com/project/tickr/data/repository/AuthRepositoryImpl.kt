package com.project.tickr.data.repository

import com.project.tickr.core.network.SupabaseProvider
import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.AuthUser
import com.project.tickr.domain.model.UserSession
import com.project.tickr.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class AuthRepositoryImpl : AuthRepository {

    private val client: SupabaseClient by lazy { SupabaseProvider.client }

    // Supabase-kt v3.x throws IllegalStateException instead of returning null
    // when no session key exists in local storage yet.
    private fun safeCurrentSession() = try {
        client.auth.currentSessionOrNull()
    } catch (e: Exception) {
        null
    }

    private fun safeCurrentUser() = try {
        client.auth.currentUserOrNull()
    } catch (e: Exception) {
        null
    }

    override suspend fun signUp(email: String, password: String): DataResult<AuthUser> = try {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val user = safeCurrentUser()
        DataResult.Success(AuthUser(user?.id ?: "", user?.email ?: email))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Error(AppError.Unknown(debugMessage(e)))
    }

    override suspend fun signIn(email: String, password: String): DataResult<AuthUser> = try {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val user = safeCurrentUser()
        DataResult.Success(AuthUser(user?.id ?: "", user?.email ?: email))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Error(AppError.Unknown(debugMessage(e)))
    }

    override suspend fun signOut(): DataResult<Unit> = try {
        client.auth.signOut()
        DataResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Error(AppError.Network)
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> = try {
        client.auth.resetPasswordForEmail(email)
        DataResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Error(AppError.Network)
    }

    // Use sessionStatus instead of currentUserOrNull() so that App.kt's .first() waits
    // until Supabase finishes loading the persisted token from storage (Initializing phase).
    // Without this, a cold app start would race the storage load and return null,
    // incorrectly routing the user to Login after an app kill.
    override fun observeSession(): Flow<AuthUser?> =
        client.auth.sessionStatus
            .filterNot { it is SessionStatus.Initializing }
            .map { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        user?.let { AuthUser(it.id, it.email ?: "") }
                    }
                    else -> null
                }
            }

    override fun currentUserId(): String? = safeCurrentUser()?.id

    override suspend fun login(identifier: String, password: String): DataResult<UserSession> {
        return try {
            client.auth.signInWith(Email) {
                this.email = identifier
                this.password = password
            }
            val supaSession = safeCurrentSession()
                ?: return DataResult.Error(AppError.Unknown("Login gagal: session null setelah signIn"))
            val user = supaSession.user
                ?: return DataResult.Error(AppError.Unknown("Login gagal: user null di session"))

            DataResult.Success(
                UserSession(
                    userId = user.id,
                    email = user.email ?: identifier,
                    fullName = user.userMetadata
                        ?.get("full_name")?.jsonPrimitive?.contentOrNull ?: "",
                    accessToken = supaSession.accessToken,
                )
            )
        } catch (e: RestException) {
            when {
                e.message?.contains("Invalid login credentials", ignoreCase = true) == true ||
                e.message?.contains("invalid_grant", ignoreCase = true) == true ||
                e.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                    DataResult.Error(AppError.Unauthorized)
                else -> DataResult.Error(AppError.Unknown("Login RestException: ${e.message}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataResult.Error(AppError.Unknown(debugMessage(e)))
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
    ): DataResult<UserSession> {
        return try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject { put("full_name", fullName) }
            }

            var supaSession = safeCurrentSession()

            // If no session (email confirmation ON), try signing in immediately.
            // Succeeds if Supabase doesn't require confirmation.
            if (supaSession == null) {
                try {
                    client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    supaSession = safeCurrentSession()
                } catch (_: Exception) {
                    // signIn failure = email confirmation still required; proceed without session
                }
            }

            if (supaSession != null) {
                val user = supaSession.user
                DataResult.Success(
                    UserSession(
                        userId = user?.id ?: "",
                        email = user?.email ?: email,
                        fullName = user?.userMetadata
                            ?.get("full_name")?.jsonPrimitive?.contentOrNull ?: fullName,
                        accessToken = supaSession.accessToken,
                    )
                )
            } else {
                // Registration succeeded but session pending email confirmation
                DataResult.Success(
                    UserSession(userId = "", email = email, fullName = fullName, accessToken = "")
                )
            }
        } catch (e: RestException) {
            when {
                e.message?.contains("already registered", ignoreCase = true) == true ||
                e.message?.contains("User already registered", ignoreCase = true) == true ->
                    DataResult.Error(AppError.Validation("email", "Email ini sudah terdaftar."))
                else ->
                    DataResult.Error(AppError.Unknown("Register RestException: ${e.message}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataResult.Error(AppError.Unknown(debugMessage(e)))
        }
    }

    override suspend fun loginWithGoogle(): DataResult<UserSession> =
        DataResult.Error(AppError.Unknown("Google OAuth not implemented"))

    override suspend fun changePassword(newPassword: String): DataResult<Unit> = try {
        client.auth.updateUser { password = newPassword }
        DataResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Error(AppError.Unknown(debugMessage(e)))
    }

    private fun debugMessage(e: Exception): String =
        "[${e::class.simpleName}] ${e.message ?: "no message"}"
}
