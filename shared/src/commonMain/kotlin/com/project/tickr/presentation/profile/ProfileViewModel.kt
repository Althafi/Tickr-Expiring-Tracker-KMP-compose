package com.project.tickr.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.auth.ObserveSessionUseCase
import com.project.tickr.domain.usecase.auth.SignOutUseCase
import com.project.tickr.domain.usecase.profile.GetProfileUseCase
import com.project.tickr.domain.usecase.profile.UpsertProfileUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfile: GetProfileUseCase,
    private val upsertProfile: UpsertProfileUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val errorMessages: ErrorMessageProvider,
    private val observeSession: ObserveSessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.Load -> load()
            is ProfileAction.FullNameChanged -> _state.update { it.copy(fullName = action.value) }
            ProfileAction.Save -> save()
            ProfileAction.SignOut -> signOut()
            ProfileAction.EditProfile -> viewModelScope.launch {
                _events.send(ProfileEvent.NavigateToEditProfile)
            }
            ProfileAction.OpenHelp -> viewModelScope.launch {
                _events.send(ProfileEvent.NavigateToHelp)
            }
        }
    }

    private fun load() {
        val userId = getCurrentUserId() ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val email = try {
                observeSession().filterNotNull().first().email ?: ""
            } catch (_: Exception) { "" }

            getProfile(userId)
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            fullName = profile.fullName ?: "",
                            email = email,
                            // Avatar seed = userId agar konsisten dengan pilihan di EditProfile
                            avatarSeed = it.avatarSeed.ifBlank { userId },
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(isLoading = false, email = email, avatarSeed = it.avatarSeed.ifBlank { userId }, error = errorMessages.provide(error))
                    }
                }
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
                .onSuccess { _events.send(ProfileEvent.NavigateToAuth) }
                .onError { e -> _events.send(ProfileEvent.ShowSnackbar(errorMessages.provide(e))) }
        }
    }
}
