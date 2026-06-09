package com.project.tickr.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.usecase.auth.RegisterUseCase
import com.project.tickr.domain.usecase.auth.ValidateEmailUseCase
import com.project.tickr.domain.usecase.auth.ValidateNameUseCase
import com.project.tickr.domain.usecase.auth.ValidatePasswordUseCase
import com.project.tickr.core.validation.ValidationResult
import com.project.tickr.presentation.common.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val validateEmail: ValidateEmailUseCase,
    private val validatePassword: ValidatePasswordUseCase,
    private val validateName: ValidateNameUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.FullNameChanged -> {
                _state.update { it.copy(fullName = action.value, fullNameError = null) }
            }
            is RegisterAction.EmailChanged -> {
                _state.update { it.copy(email = action.value, emailError = null) }
            }
            is RegisterAction.PasswordChanged -> {
                val result = validatePassword(action.value)
                _state.update {
                    it.copy(
                        password = action.value,
                        passwordStrength = result.strength,
                        passwordRequirements = result.requirements,
                    )
                }
            }
            RegisterAction.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            RegisterAction.Submit -> submit()
            RegisterAction.NavigateToLogin -> {
                viewModelScope.launch { _events.send(RegisterEvent.NavigateToLogin) }
            }
            RegisterAction.GoogleClicked -> {
                // TODO(user): implementasi Google/OAuth (Supabase) — jangan panggil apa pun
            }
        }
    }

    private fun submit() {
        val s = _state.value
        var hasError = false

        val nameResult = validateName(s.fullName)
        if (nameResult is ValidationResult.Invalid) {
            _state.update { it.copy(fullNameError = nameResult.reason.toUiText()) }
            hasError = true
        }

        val emailResult = validateEmail(s.email)
        if (emailResult is ValidationResult.Invalid) {
            _state.update { it.copy(emailError = emailResult.reason.toUiText()) }
            hasError = true
        }

        if (hasError) return

        _state.update { it.copy(isLoading = true, fullNameError = null, emailError = null) }

        viewModelScope.launch {
            registerUseCase(s.fullName, s.email, s.password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(RegisterEvent.NavigateToSuccess)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    when {
                        error is AppError.Validation && error.field == "email" -> {
                            _state.update { it.copy(emailError = error.reason.toUiText()) }
                        }
                        else -> {
                            _events.send(RegisterEvent.ShowError(errorMessage(error).toUiText()))
                        }
                    }
                }
        }
    }

    private fun errorMessage(error: AppError): String = when (error) {
        is AppError.Network -> "Koneksi gagal. Periksa internet Anda."
        is AppError.Validation -> error.reason
        else -> "Terjadi kesalahan. Coba lagi."
    }
}
