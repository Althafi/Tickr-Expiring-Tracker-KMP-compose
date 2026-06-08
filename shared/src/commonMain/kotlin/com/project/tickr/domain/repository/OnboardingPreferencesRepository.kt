package com.project.tickr.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingPreferencesRepository {
    fun hasSeenOnboarding(): Flow<Boolean>
    suspend fun setSeen()
}
