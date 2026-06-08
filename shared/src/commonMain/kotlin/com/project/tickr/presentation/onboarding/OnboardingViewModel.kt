package com.project.tickr.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.domain.usecase.onboarding.CompleteOnboardingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.PageChanged -> {
                val isLastPage = action.page == _state.value.pages.lastIndex
                _state.update { it.copy(currentPage = action.page, isLastPage = isLastPage) }
            }
            OnboardingAction.Next -> {
                val nextPage = _state.value.currentPage + 1
                if (nextPage < _state.value.pages.size) {
                    val isLastPage = nextPage == _state.value.pages.lastIndex
                    _state.update { it.copy(currentPage = nextPage, isLastPage = isLastPage) }
                }
            }
            OnboardingAction.Skip -> {
                finish()
            }
            OnboardingAction.Finish -> {
                finish()
            }
        }
    }

    private fun finish() {
        viewModelScope.launch {
            completeOnboardingUseCase()
            _events.send(OnboardingEvent.NavigateToAuth)
        }
    }
}
