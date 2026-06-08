package com.project.tickr.presentation.onboarding

import com.project.tickr.presentation.common.mvi.UiAction

sealed interface OnboardingAction : UiAction {
    data class PageChanged(val page: Int) : OnboardingAction
    data object Next : OnboardingAction
    data object Skip : OnboardingAction
    data object Finish : OnboardingAction
}
