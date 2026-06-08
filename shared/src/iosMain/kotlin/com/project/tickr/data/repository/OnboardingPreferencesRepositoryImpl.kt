package com.project.tickr.data.repository

import com.project.tickr.domain.repository.OnboardingPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import platform.Foundation.NSUserDefaults

actual class OnboardingPreferencesRepositoryImpl : OnboardingPreferencesRepository {
    private val defaults = NSUserDefaults.standardUserDefaults()

    override fun hasSeenOnboarding(): Flow<Boolean> = flowOf(
        defaults.boolForKey(KEY_HAS_SEEN_ONBOARDING)
    )

    override suspend fun setSeen() {
        defaults.setBool(true, forKey = KEY_HAS_SEEN_ONBOARDING)
    }

    companion object {
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
    }
}
