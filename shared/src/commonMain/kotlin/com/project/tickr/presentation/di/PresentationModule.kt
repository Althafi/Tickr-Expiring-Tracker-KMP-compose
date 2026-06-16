package com.project.tickr.presentation.di

import com.project.tickr.presentation.additem.AddItemViewModel
import com.project.tickr.presentation.auth.AuthViewModel
import com.project.tickr.presentation.common.AuthErrorStore
import com.project.tickr.presentation.login.LoginViewModel
import com.project.tickr.presentation.register.RegisterViewModel
import com.project.tickr.presentation.category.form.CategoryFormViewModel
import com.project.tickr.presentation.category.list.CategoryListViewModel
import com.project.tickr.presentation.expiry.ExpiryViewModel
import com.project.tickr.presentation.expiry.detail.ExpiryDetailViewModel
import com.project.tickr.presentation.home.HomeViewModel
import com.project.tickr.presentation.item.detail.ItemDetailViewModel
import com.project.tickr.presentation.item.form.ItemFormViewModel
import com.project.tickr.presentation.item.list.ItemListViewModel
import com.project.tickr.presentation.onboarding.OnboardingViewModel
import com.project.tickr.presentation.profile.ProfileViewModel
import com.project.tickr.presentation.editprofile.EditProfileViewModel
import com.project.tickr.presentation.changepassword.ChangePasswordViewModel
import com.project.tickr.presentation.help.HelpViewModel
import org.koin.dsl.module

val presentationModule = module {
    single { AuthErrorStore() }
    factory { AuthViewModel(get(), get(), get(), get(), get()) }
    factory { LoginViewModel(get()) }
    factory { RegisterViewModel(get(), get(), get(), get(), get(), get()) }
    factory { OnboardingViewModel(get()) }
    factory { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    factory { AddItemViewModel(get(), get(), get(), get(), get()) }
    factory { ExpiryViewModel(get(), get(), get(), get()) }
    factory { ExpiryDetailViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { ItemListViewModel(get(), get(), get(), get(), get()) }
    factory { ItemDetailViewModel(get(), get(), get(), get()) }
    factory { ItemFormViewModel(get(), get(), get(), get(), get()) }
    factory { CategoryListViewModel(get(), get(), get(), get()) }
    factory { CategoryFormViewModel(get(), get(), get(), get()) }
    factory { ProfileViewModel(get(), get(), get(), get(), get(), get()) }
    factory { EditProfileViewModel(get(), get(), get(), get(), get()) }
    factory { ChangePasswordViewModel(get(), get()) }
    factory { HelpViewModel() }
}
