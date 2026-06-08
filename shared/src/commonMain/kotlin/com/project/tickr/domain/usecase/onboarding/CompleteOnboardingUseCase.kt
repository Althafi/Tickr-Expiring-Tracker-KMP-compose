package com.project.tickr.domain.usecase.onboarding

import com.project.tickr.domain.repository.OnboardingPreferencesRepository

class CompleteOnboardingUseCase(
    private val repository: OnboardingPreferencesRepository
) {
    suspend operator fun invoke() = repository.setSeen()
}
