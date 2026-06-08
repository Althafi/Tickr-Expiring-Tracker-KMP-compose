package com.project.tickr.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.model.AuthUser
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.auth.ObserveSessionUseCase
import com.project.tickr.domain.usecase.auth.SignInUseCase
import com.project.tickr.domain.usecase.auth.SignOutUseCase
import com.project.tickr.domain.usecase.auth.SignUpUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val errorMessages: ErrorMessageProvider
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeSession()
    }

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.EmailChanged -> {
                _state.update { it.copy(email = action.value, emailError = null) }
            }
            is AuthAction.PasswordChanged -> {
                _state.update { it.copy(password = action.value, passwordError = null) }
            }
            AuthAction.ToggleMode -> {
                _state.update { it.copy(mode = if (it.mode == AuthUiState.Mode.SignIn) AuthUiState.Mode.SignUp else AuthUiState.Mode.SignIn) }
            }
            AuthAction.Submit -> submit()
            AuthAction.SignOut -> signOut()
        }
    }

    private fun submit() {
        val currentState = _state.value
        _state.update { it.copy(isSubmitting = true, emailError = null, passwordError = null, generalError = null) }

        viewModelScope.launch {
            val result = if (currentState.mode == AuthUiState.Mode.SignIn) {
                signInUseCase(currentState.email, currentState.password)
            } else {
                signUpUseCase(currentState.email, currentState.password)
            }

            result
                .onSuccess { user ->
                    _state.update { it.copy(isSubmitting = false) }
                    viewModelScope.launch { _events.send(AuthEvent.NavigateToHome) }
                }
                .onError { error ->
                    _state.update { state ->
                        state.copy(
                            isSubmitting = false,
                            emailError = if (error is AppError.Validation && error.field == "email") error.reason else null,
                            passwordError = if (error is AppError.Validation && error.field == "password") error.reason else null,
                            generalError = if (error !is AppError.Validation) errorMessages.provide(error) else null
                        )
                    }
                }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
                .onSuccess {
                    _state.update { AuthUiState() }
                    _events.send(AuthEvent.NavigateToAuth)
                }
                .onError { error ->
                    _events.send(AuthEvent.ShowError(errorMessages.provide(error)))
                }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            observeSessionUseCase().collect { user: AuthUser? ->
                if (user != null) {
                    _state.update { it.copy(email = user.email ?: "") }
                }
            }
        }
    }
}
