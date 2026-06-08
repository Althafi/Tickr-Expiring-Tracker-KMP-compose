package com.project.tickr.presentation.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)
