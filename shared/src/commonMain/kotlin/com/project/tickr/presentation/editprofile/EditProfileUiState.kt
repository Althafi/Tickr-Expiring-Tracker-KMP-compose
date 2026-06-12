package com.project.tickr.presentation.editprofile

import com.project.tickr.presentation.common.UiText

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val avatarSeed: String = "",
    val showAvatarPicker: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: UiText? = null,
)
