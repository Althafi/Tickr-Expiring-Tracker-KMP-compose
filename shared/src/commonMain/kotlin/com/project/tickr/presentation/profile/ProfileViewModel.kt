package com.project.tickr.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.auth.SignOutUseCase
import com.project.tickr.domain.usecase.profile.GetProfileUseCase
import com.project.tickr.domain.usecase.profile.UpsertProfileUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfile: GetProfileUseCase,
    private val upsertProfile: UpsertProfileUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val errorMessages: ErrorMessageProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.Load -> load()
            is ProfileAction.FullNameChanged -> _state.update { it.copy(fullName = action.value) }
            ProfileAction.Save -> save()
            ProfileAction.SignOut -> signOut()
        }
    }

    private fun load() {
        val userId = getCurrentUserId() ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getProfile(userId)
                .onSuccess { profile -> _state.update { it.copy(isLoading = false, fullName = profile.fullName ?: "") } }
                .onError { error -> _state.update { it.copy(isLoading = false, error = errorMessages.provide(error)) } }
        }
    }

    private fun save() {
        val userId = getCurrentUserId() ?: return
        _state.update { it.copy(isSaving = true) }
        // TODO: implement
        _state.update { it.copy(isSaving = false) }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
                .onSuccess { viewModelScope.launch { _events.send(ProfileEvent.NavigateToAuth) } }
                .onError { e -> _events.send(ProfileEvent.ShowSnackbar(errorMessages.provide(e))) }
        }
    }
}
