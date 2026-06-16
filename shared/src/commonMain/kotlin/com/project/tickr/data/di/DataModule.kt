package com.project.tickr.data.di

import com.project.tickr.core.network.SupabaseProvider
import com.project.tickr.data.remote.datasource.CategoryRemoteDataSource
import com.project.tickr.data.remote.datasource.StorageRemoteDataSource
import io.github.jan.supabase.SupabaseClient
import com.project.tickr.data.remote.datasource.ItemRemoteDataSource
import com.project.tickr.data.remote.datasource.NotificationRemoteDataSource
import com.project.tickr.data.remote.datasource.ProfileRemoteDataSource
import com.project.tickr.data.repository.AuthRepositoryImpl
import com.project.tickr.data.repository.CategoryRepositoryImpl
import com.project.tickr.data.repository.ItemRepositoryImpl
import com.project.tickr.data.repository.NotificationRepositoryImpl
import com.project.tickr.data.repository.OnboardingPreferencesRepositoryImpl
import com.project.tickr.data.repository.ProfileRepositoryImpl
import com.project.tickr.domain.repository.AuthRepository
import com.project.tickr.domain.repository.CategoryRepository
import com.project.tickr.domain.repository.ItemRepository
import com.project.tickr.domain.repository.NotificationRepository
import com.project.tickr.domain.repository.OnboardingPreferencesRepository
import com.project.tickr.domain.repository.ProfileRepository
import org.koin.dsl.module

val dataModule = module {
    single { SupabaseProvider.client }

    single { ProfileRemoteDataSource(get()) }
    single { CategoryRemoteDataSource(get()) }
    single { ItemRemoteDataSource(get()) }
    single { NotificationRemoteDataSource(get()) }
    single { StorageRemoteDataSource(get()) }

    single<AuthRepository> { AuthRepositoryImpl() }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<ItemRepository> { ItemRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<OnboardingPreferencesRepository> { OnboardingPreferencesRepositoryImpl() }
}
