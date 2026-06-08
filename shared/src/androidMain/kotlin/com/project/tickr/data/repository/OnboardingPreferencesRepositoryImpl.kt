package com.project.tickr.data.repository

import android.content.Context
import com.project.tickr.domain.repository.OnboardingPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class OnboardingPreferencesRepositoryImpl : OnboardingPreferencesRepository {
    private val context: Context by lazy { AndroidContextHolder.getContext() }
    private val prefs by lazy { context.getSharedPreferences("tickr_prefs", Context.MODE_PRIVATE) }

    actual override fun hasSeenOnboarding(): Flow<Boolean> = flowOf(
        prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
    )

    actual override suspend fun setSeen() {
        prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, true).apply()
    }

    companion object {
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
    }
}

internal object AndroidContextHolder {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun getContext(): Context = appContext ?: throw IllegalStateException("AndroidContextHolder not initialized")
}
