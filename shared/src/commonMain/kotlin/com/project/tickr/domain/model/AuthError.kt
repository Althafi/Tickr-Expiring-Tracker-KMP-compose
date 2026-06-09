package com.project.tickr.domain.model

sealed class AuthError(override val message: String) : Exception(message) {
    data object InvalidCredentials : AuthError("Email/nama atau kata sandi salah.")
    data object EmailTaken : AuthError("Email ini sudah terdaftar.")
    data object Network : AuthError("Koneksi gagal. Periksa internet Anda.")
    data object Unknown : AuthError("Terjadi kesalahan. Coba lagi.")
}
