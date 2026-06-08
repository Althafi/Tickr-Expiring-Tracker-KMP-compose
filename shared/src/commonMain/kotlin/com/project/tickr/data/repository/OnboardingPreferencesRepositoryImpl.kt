package com.project.tickr.data.repository

import com.project.tickr.domain.repository.OnboardingPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

expect class OnboardingPreferencesRepositoryImpl() : OnboardingPreferencesRepository {
    override fun hasSeenOnboarding(): Flow<Boolean>
    override suspend fun setSeen()
}
