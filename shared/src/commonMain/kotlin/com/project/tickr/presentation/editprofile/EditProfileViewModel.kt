package com.project.tickr.presentation.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.model.Profile
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.auth.ObserveSessionUseCase
import com.project.tickr.domain.usecase.profile.GetProfileUseCase
import com.project.tickr.domain.usecase.profile.UpsertProfileUseCase
import com.project.tickr.presentation.common.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val getProfile: GetProfileUseCase,
    private val upsertProfile: UpsertProfileUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val observeSession: ObserveSessionUseCase,
    private val errorMessages: ErrorMessageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    fun onAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.NameChanged -> _state.update {
                it.copy(name = action.value, nameError = null)
            }
            EditProfileAction.ShowAvatarPicker -> _state.update {
                it.copy(showAvatarPicker = true)
            }
            is EditProfileAction.AvatarSelected -> _state.update {
                it.copy(avatarSeed = action.seed, showAvatarPicker = false)
            }
            EditProfileAction.DismissAvatarPicker -> _state.update {
                it.copy(showAvatarPicker = false)
            }
            EditProfileAction.OpenChangePassword -> viewModelScope.launch {
                _events.send(EditProfileEvent.NavigateToChangePassword)
            }
            EditProfileAction.Save -> save()
            EditProfileAction.Back -> viewModelScope.launch {
                _events.send(EditProfileEvent.NavigateBack)
            }
        }
    }

    private fun load() {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            val email = try {
                observeSession().filterNotNull().first().email ?: ""
            } catch (_: Exception) { "" }

            getProfile(userId)
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            name = profile.fullName ?: "",
                            email = email,
                            // Gunakan userId sebagai seed default — avatar konsisten per user
                            avatarSeed = it.avatarSeed.ifBlank { userId },
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(email = email, avatarSeed = it.avatarSeed.ifBlank { userId }) }
                    _events.send(EditProfileEvent.ShowSnackbar(errorMessages.provide(error)))
                }
        }
    }

    private fun save() {
        val userId = getCurrentUserId() ?: return
        val name = _state.value.name.trim()
        if (name.isBlank()) {
            _state.update { it.copy(nameError = "Nama tidak boleh kosong".toUiText()) }
            return
        }
        if (name.length > 100) {
            _state.update { it.copy(nameError = "Nama terlalu panjang (maks 100 karakter)".toUiText()) }
            return
        }
        _state.update { it.copy(isSaving = true, nameError = null) }
        viewModelScope.launch {
            upsertProfile(Profile(id = userId, fullName = name, updatedAt = ""))
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(EditProfileEvent.ShowSnackbar("Profil berhasil disimpan")) // TODO(user): gunakan string resource
                    _events.send(EditProfileEvent.NavigateBack)
                }
                .onError { error ->
                    _state.update { it.copy(isSaving = false) }
                    _events.send(EditProfileEvent.ShowSnackbar(errorMessages.provide(error)))
                }
        }
    }
}
