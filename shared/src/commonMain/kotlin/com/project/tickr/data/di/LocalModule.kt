package com.project.tickr.data.di

import app.cash.sqldelight.db.SqlDriver
import com.project.tickr.core.util.DateTimeUtilImpl
import com.project.tickr.data.local.db.DatabaseDriverFactory
import com.project.tickr.data.local.db.DatabaseSeeder
import com.project.tickr.data.local.image.createLocalImageStorage
import com.project.tickr.data.local.reminder.ReminderPreferencesImpl
import com.project.tickr.data.local.reminder.createReminderScheduler
import com.project.tickr.data.local.settings.KeyValueStore
import com.project.tickr.data.local.settings.createAppInfoProvider
import com.project.tickr.data.local.settings.createKeyValueStore
import com.project.tickr.data.repository.SettingsRepositoryImpl
import com.project.tickr.domain.provider.AppInfoProvider
import com.project.tickr.domain.repository.ReminderPreferences
import com.project.tickr.domain.repository.SettingsRepository
import com.project.tickr.domain.scheduler.ReminderScheduler
import com.project.tickr.domain.storage.ImageStorage
import com.project.tickr.data.local.datasource.CategoryLocalDataSource
import com.project.tickr.data.local.datasource.ItemLocalDataSource
import com.project.tickr.data.local.datasource.NotificationLocalDataSource
import com.project.tickr.data.repository.LocalBackupRepository
import com.project.tickr.data.repository.LocalCategoryRepository
import com.project.tickr.data.repository.LocalItemRepository
import com.project.tickr.data.repository.LocalNotificationRepository
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.repository.BackupRepository
import com.project.tickr.domain.repository.CategoryRepository
import com.project.tickr.domain.repository.ItemRepository
import com.project.tickr.domain.repository.NotificationRepository
import org.koin.dsl.module

val localModule = module {
    single<SqlDriver> { DatabaseDriverFactory().create() }
    single {
        TickrDatabase(get<SqlDriver>()).also {
            DatabaseSeeder.seedIfEmpty(it, createdAt = DateTimeUtilImpl().today())
        }
    }

    single { ItemLocalDataSource(get()) }
    single { CategoryLocalDataSource(get()) }
    single { NotificationLocalDataSource(get()) }

    single<ImageStorage> { createLocalImageStorage() }

    single<ItemRepository> { LocalItemRepository(get(), get()) }
    single<CategoryRepository> { LocalCategoryRepository(get()) }
    single<NotificationRepository> { LocalNotificationRepository(get()) }
    single<BackupRepository> { LocalBackupRepository(get(), get(), get()) }

    single<KeyValueStore> { createKeyValueStore() }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<AppInfoProvider> { createAppInfoProvider() }

    single<ReminderScheduler> { createReminderScheduler(get()) }
    single<ReminderPreferences> { ReminderPreferencesImpl(get()) }
}
