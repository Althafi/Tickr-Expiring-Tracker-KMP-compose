package com.project.tickr.presentation.onboarding

import com.project.tickr.presentation.common.mvi.UiEvent

sealed interface OnboardingEvent : UiEvent {
    data object NavigateToAuth : OnboardingEvent
}
