package com.project.tickr.data.di

import com.project.tickr.data.repository.OnboardingPreferencesRepositoryImpl
import com.project.tickr.domain.repository.OnboardingPreferencesRepository
import org.koin.dsl.module

val dataModule = module {
    single<OnboardingPreferencesRepository> { OnboardingPreferencesRepositoryImpl() }
}
