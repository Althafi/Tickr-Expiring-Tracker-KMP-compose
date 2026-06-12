package com.project.tickr.presentation.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.auth.ChangePasswordUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    private val changePassword: ChangePasswordUseCase,
    private val errorMessages: ErrorMessageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    private val _events = Channel<ChangePasswordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ChangePasswordAction) {
        when (action) {
            is ChangePasswordAction.CurrentChanged -> _state.update { it.copy(currentPassword = action.value) }
            is ChangePasswordAction.NewChanged -> _state.update { it.copy(newPassword = action.value) }
            is ChangePasswordAction.ConfirmChanged -> _state.update { it.copy(confirmPassword = action.value) }
            ChangePasswordAction.ToggleCurrentVisibility -> _state.update { it.copy(currentVisible = !it.currentVisible) }
            ChangePasswordAction.ToggleNewVisibility -> _state.update { it.copy(newVisible = !it.newVisible) }
            ChangePasswordAction.ToggleConfirmVisibility -> _state.update { it.copy(confirmVisible = !it.confirmVisible) }
            ChangePasswordAction.Submit -> submit()
            ChangePasswordAction.Retry -> _state.update {
                it.copy(step = ChangePasswordStep.Form, errorMessage = null)
            }
            ChangePasswordAction.BackToProfile -> viewModelScope.launch {
                _events.send(ChangePasswordEvent.NavigateBackToProfile)
            }
            ChangePasswordAction.Back -> viewModelScope.launch {
                _events.send(ChangePasswordEvent.NavigateBack)
            }
        }
    }

    private fun submit() {
        val s = _state.value
        val error = when {
            s.newPassword.length < 8 ->
                "Kata sandi minimal 8 karakter" // TODO(user): gunakan string resource
            !s.newPassword.any { it.isLetter() } || !s.newPassword.any { it.isDigit() } ->
                "Kata sandi harus mengandung huruf dan angka" // TODO(user): gunakan string resource
            s.newPassword != s.confirmPassword ->
                "Konfirmasi kata sandi tidak sesuai" // TODO(user): gunakan string resource
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(step = ChangePasswordStep.Failure, errorMessage = error) }
            return
        }

        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            changePassword(s.newPassword)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false, step = ChangePasswordStep.Success) }
                }
                .onError { e ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            step = ChangePasswordStep.Failure,
                            errorMessage = errorMessages.provide(e),
                        )
                    }
                }
        }
    }
}
