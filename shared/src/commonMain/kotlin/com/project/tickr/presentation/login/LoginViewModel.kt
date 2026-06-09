package com.project.tickr.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.usecase.auth.LoginUseCase
import com.project.tickr.presentation.common.UiText
import com.project.tickr.presentation.common.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.IdentifierChanged -> {
                _state.update { it.copy(identifier = action.value, identifierError = null) }
            }
            is LoginAction.PasswordChanged -> {
                _state.update { it.copy(password = action.value, passwordError = null) }
            }
            LoginAction.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginAction.Submit -> submit()
            LoginAction.NavigateToRegister -> {
                viewModelScope.launch { _events.send(LoginEvent.NavigateToRegister) }
            }
            LoginAction.GoogleClicked -> {
                // TODO(user): implementasi Google/OAuth (Supabase) — jangan panggil apa pun
            }
        }
    }

    private fun submit() {
        val s = _state.value
        var hasError = false

        if (s.identifier.isBlank()) {
            _state.update { it.copy(identifierError = "Nama atau email tidak boleh kosong.".toUiText()) }
            hasError = true
        }
        if (s.password.isBlank()) {
            _state.update { it.copy(passwordError = "Password tidak boleh kosong.".toUiText()) }
            hasError = true
        }
        if (hasError) return

        _state.update { it.copy(isLoading = true, identifierError = null, passwordError = null) }

        viewModelScope.launch {
            loginUseCase(s.identifier, s.password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.NavigateToHome)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    val message = errorMessage(error)
                    _events.send(LoginEvent.ShowError(message.toUiText()))
                }
        }
    }

    private fun errorMessage(error: AppError): String = when (error) {
        is AppError.Unauthorized -> "Email/nama atau kata sandi salah." // TODO(user): sesuaikan
        is AppError.Network -> "Koneksi gagal. Periksa internet Anda."
        is AppError.Validation -> error.reason
        else -> "Terjadi kesalahan. Coba lagi."
    }
}
