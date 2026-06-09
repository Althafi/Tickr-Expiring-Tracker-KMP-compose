package com.project.tickr.domain.model

data class UserSession(
    val userId: String,
    val email: String,
    val fullName: String,
    val accessToken: String,
)
