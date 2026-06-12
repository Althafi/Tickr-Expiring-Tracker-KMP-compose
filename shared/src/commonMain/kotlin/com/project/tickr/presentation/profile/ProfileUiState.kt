package com.project.tickr.presentation.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val avatarSeed: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)
