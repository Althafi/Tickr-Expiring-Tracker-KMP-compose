package com.project.tickr.presentation.changepassword

data class ChangePasswordUiState(
    val step: ChangePasswordStep = ChangePasswordStep.Form,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentVisible: Boolean = false,
    val newVisible: Boolean = false,
    val confirmVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)
