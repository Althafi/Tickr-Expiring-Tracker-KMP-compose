package com.project.tickr.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.project.tickr.data.local.FakeImageStorage
import com.project.tickr.data.local.datasource.CategoryLocalDataSource
import com.project.tickr.data.local.datasource.ItemLocalDataSource
import com.project.tickr.data.local.datasource.NotificationLocalDataSource
import com.project.tickr.data.local.db.DatabaseSeeder
import com.project.tickr.data.local.settings.KeyValueStore
import com.project.tickr.data.repository.LocalBackupRepository
import com.project.tickr.data.repository.LocalCategoryRepository
import com.project.tickr.data.repository.LocalItemRepository
import com.project.tickr.data.repository.LocalNotificationRepository
import com.project.tickr.data.repository.SettingsRepositoryImpl
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.di.domainModule
import com.project.tickr.domain.model.AppInfo
import com.project.tickr.domain.model.DueReminder
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.model.ProductInfo
import com.project.tickr.domain.provider.ConnectivityProvider
import com.project.tickr.domain.repository.ProductLookupRepository
import com.project.tickr.presentation.insight.InsightViewModel
import com.project.tickr.domain.provider.AppInfoProvider
import com.project.tickr.domain.repository.BackupRepository
import com.project.tickr.domain.repository.CategoryRepository
import com.project.tickr.domain.repository.ItemRepository
import com.project.tickr.domain.repository.NotificationRepository
import com.project.tickr.domain.repository.OnboardingPreferencesRepository
import com.project.tickr.domain.repository.ReminderPreferences
import com.project.tickr.domain.repository.SettingsRepository
import com.project.tickr.domain.scheduler.ReminderScheduler
import com.project.tickr.domain.storage.ImageStorage
import com.project.tickr.presentation.about.AboutViewModel
import com.project.tickr.presentation.additem.AddItemViewModel
import com.project.tickr.presentation.consumed.ConsumedItemsViewModel
import com.project.tickr.presentation.di.presentationModule
import com.project.tickr.presentation.expiry.ExpiryViewModel
import com.project.tickr.presentation.expiry.detail.ExpiryDetailViewModel
import com.project.tickr.presentation.help.HelpViewModel
import com.project.tickr.presentation.home.HomeViewModel
import com.project.tickr.presentation.navigation.MainShellViewModel
import com.project.tickr.presentation.notifications.NotificationsViewModel
import com.project.tickr.presentation.onboarding.OnboardingViewModel
import com.project.tickr.presentation.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Konstruktor ViewModel diisi lewat `get()` posisional: compiler hanya memeriksa jumlahnya, bukan apakah
 * Koin punya binding-nya. Tes ini menyelesaikan SEMUA ViewModel dari modul asli supaya salah kabel DI
 * ketahuan di sini, bukan sebagai crash saat layar dibuka.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiGraphTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(Dispatchers.Unconfined)

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    private val platformModule = module {
        // Pengganti semua yang di produksi disediakan LocalModule + actual per platform.
        val db = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, Properties().apply { put("foreign_keys", "true") })
            .let { driver ->
                TickrDatabase.Schema.create(driver)
                TickrDatabase(driver).also { DatabaseSeeder.seedIfEmpty(it, "seed") }
            }
        single<ImageStorage> { FakeImageStorage() }
        single<ItemRepository> { LocalItemRepository(ItemLocalDataSource(db), get()) }
        single<CategoryRepository> { LocalCategoryRepository(CategoryLocalDataSource(db)) }
        single<NotificationRepository> { LocalNotificationRepository(NotificationLocalDataSource(db)) }
        single<BackupRepository> { LocalBackupRepository(db, get(), get()) }
        single<SettingsRepository> {
            SettingsRepositoryImpl(object : KeyValueStore {
                private val map = mutableMapOf<String, String>()
                override fun getString(key: String) = map[key]
                override fun putString(key: String, value: String) { map[key] = value }
            })
        }
        single<AppInfoProvider> {
            object : AppInfoProvider {
                override fun getAppInfo() = AppInfo("Tickr", "1.0", "1")
            }
        }
        single<ReminderPreferences> {
            object : ReminderPreferences {
                override fun hasAskedPermission() = false
                override fun markPermissionAsked() = Unit
            }
        }
        single<ReminderScheduler> {
            object : ReminderScheduler {
                override suspend fun hasPermission() = true
                override suspend fun syncAll(items: List<Item>) = Unit
                override suspend fun deliver(due: List<DueReminder>) = Unit
                override suspend fun cancel(itemId: Long) = Unit
                override suspend fun cancelAll() = Unit
            }
        }
        single<ConnectivityProvider> {
            object : ConnectivityProvider {
                override fun isOnline() = true
            }
        }
        single<ProductLookupRepository> {
            object : ProductLookupRepository {
                override suspend fun lookup(barcode: String): DataResult<ProductInfo?> = DataResult.Success(null)
                override suspend fun downloadImage(url: String): DataResult<ByteArray> = DataResult.Success(ByteArray(0))
            }
        }
        single<OnboardingPreferencesRepository> {
            object : OnboardingPreferencesRepository {
                override fun hasSeenOnboarding(): Flow<Boolean> = flowOf(false)
                override suspend fun setSeen() = Unit
            }
        }
    }

    @Test
    fun every_viewmodel_resolves_from_the_real_modules() {
        val koin = koinApplication { modules(domainModule, presentationModule, platformModule) }.koin
        koin.get<OnboardingViewModel>()
        koin.get<HomeViewModel>()
        koin.get<AddItemViewModel>()
        koin.get<ExpiryViewModel>()
        koin.get<ExpiryDetailViewModel>()
        koin.get<HelpViewModel>()
        koin.get<MainShellViewModel>()
        koin.get<SettingsViewModel>()
        koin.get<AboutViewModel>()
        koin.get<NotificationsViewModel>()
        koin.get<ConsumedItemsViewModel>()
        koin.get<InsightViewModel>()
    }
}
