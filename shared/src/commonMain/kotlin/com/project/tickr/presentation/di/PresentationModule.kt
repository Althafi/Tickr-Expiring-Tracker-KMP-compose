package com.project.tickr.presentation.di

import com.project.tickr.presentation.additem.AddItemViewModel
import com.project.tickr.presentation.expiry.ExpiryViewModel
import com.project.tickr.presentation.expiry.detail.ExpiryDetailViewModel
import com.project.tickr.presentation.home.HomeViewModel
import com.project.tickr.presentation.onboarding.OnboardingViewModel
import com.project.tickr.presentation.help.HelpViewModel
import com.project.tickr.presentation.consumed.ConsumedItemsViewModel
import com.project.tickr.presentation.about.AboutViewModel
import com.project.tickr.presentation.settings.SettingsViewModel
import com.project.tickr.presentation.common.AppMessenger
import com.project.tickr.presentation.common.ErrorMessageProvider
import org.koin.dsl.module
import com.project.tickr.presentation.navigation.MainShellViewModel
import com.project.tickr.presentation.notifications.NotificationsViewModel

val presentationModule = module {
    single { ErrorMessageProvider() }
    single { AppMessenger() }
    factory { OnboardingViewModel(get()) }
    factory { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    factory { AddItemViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { ExpiryViewModel(get(), get(), get(), get(), get()) }
    factory { ExpiryDetailViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { HelpViewModel() }
    factory { MainShellViewModel(get(), get(), get()) }
    factory { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { AboutViewModel(get()) }
    factory { NotificationsViewModel(get(), get()) }
    factory { ConsumedItemsViewModel(get(), get(), get()) }
}
