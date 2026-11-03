package com.project.tickr.domain.usecase.auth

import com.project.tickr.core.user.LOCAL_USER_ID

/**
 * Offline-first: tidak ada akun, jadi selalu mengembalikan [LOCAL_USER_ID].
 * Dihapus di Fase 9 bersama seluruh parameter `userId`.
 */
class GetCurrentUserIdUseCase {
    operator fun invoke(): String = LOCAL_USER_ID
}
