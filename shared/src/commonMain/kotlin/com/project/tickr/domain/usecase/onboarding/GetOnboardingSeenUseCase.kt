package com.project.tickr.domain.usecase.onboarding

import com.project.tickr.domain.repository.OnboardingPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetOnboardingSeenUseCase(
    private val repository: OnboardingPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.hasSeenOnboarding()
}
