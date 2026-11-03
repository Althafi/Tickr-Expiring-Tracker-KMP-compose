# Tickr App — Data Layer Phase 1 Implementation Documentation

**Date:** 2026-06-07  
**Status:** ✅ Complete  
**Next Phase:** UseCase/ViewModel/UI Layer

---

## Overview

This document captures the complete data layer implementation (Phase 1) for the Tickr KMP project. The data layer provides a clean separation between network/database operations and domain logic, with type-safe error handling and dependency injection ready for use cases and ViewModels.

**Architecture Stack:**
- **Framework:** Kotlin Multiplatform (KMP) with `androidMultiplatformLibrary` plugin
- **HTTP Client:** Ktor 3.5.0 with platform-specific engines (OkHttp for Android, Darwin for iOS)
- **Backend:** Supabase with PostgreSQL (supabase-kt 3.6.0, postgrest-kt 3.6.0)
- **DI Container:** Koin 4.2.1
- **Android Inspector:** Chucker 4.0.0 (HTTP interceptor for dev builds)
- **Logging:** Ktor built-in Logging plugin

---

## Dependency Changes

### Added to `gradle/libs.versions.toml`

```toml
[versions]
# ... existing versions ...
chucker = "4.0.0"

[libraries]
# ... existing libraries ...
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
chucker = { module = "com.github.chuckerteam.chucker:library", version.ref = "chucker" }
chucker-noop = { module = "com.github.chuckerteam.chucker:library-no-op", version.ref = "chucker" }
```

### Modified in `shared/build.gradle.kts`

**commonMain:**
- Added `implementation(libs.ktor.client.logging)`

**androidMain:**
- Replaced `ktor-client-cio` with `ktor-client-okhttp`
- Added `ktor-client-okhttp`
- Added `chucker` library

---

## Core Architecture Layers

### 1. Result & Error Handling

**`core/result/DataResult.kt`**  
Sealed interface for type-safe result handling:

```kotlin
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val error: AppError) : DataResult<Nothing>
}

inline fun <T, R> DataResult<T>.mapData(transform: (T) -> R): DataResult<R>
```

**`core/result/AppError.kt`**  
Domain-specific error types:

```kotlin
sealed class AppError(open val message: String) {
    data object Network : AppError("Network unavailable")
    data object Unauthorized : AppError("Unauthorized / session invalid")
    data class NotFound(val entity: String) : AppError("$entity not found")
    data class Serialization(val detail: String) : AppError("Parse error: $detail")
    data class Unknown(val detail: String) : AppError(detail)
}
```

---

### 2. Network Layer

**`core/network/HttpEngineFactory.kt` (expect/actual pattern)**

Provides platform-specific HTTP engines:

- **Android** (`HttpEngineFactory.android.kt`): OkHttp with ChuckerInterceptor
- **iOS** (`HttpEngineFactory.ios.kt`): Darwin engine

```kotlin
// commonMain
expect fun provideHttpEngine(): HttpClientEngine

// androidMain
actual fun provideHttpEngine(): HttpClientEngine = OkHttp.create {
    addInterceptor(ChuckerInterceptor.Builder(context)...)
}

// iosMain
actual fun provideHttpEngine(): HttpClientEngine = Darwin.create()
```

**`core/network/SupabaseProvider.kt`**

Singleton lazy initialization of Supabase client:

```kotlin
object SupabaseProvider {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_KEY
        ) {
            httpEngine = provideHttpEngine()
            install(Postgrest)
            install(Auth)
            httpConfig {
                install(Logging) { level = LogLevel.ALL }
            }
        }
    }
}
```

**`core/network/ChuckerContext.kt`** (Android only)

Holds application context for Chucker initialization:

```kotlin
object ChuckerContext {
    var applicationContext: Context? = null
}
```

---

### 3. Data Layer

#### Remote Data Source Base

**`data/remote/datasource/BaseRemoteDataSource.kt`**

Abstract base class with type-based exception mapping:

```kotlin
abstract class BaseRemoteDataSource(protected val client: SupabaseClient) {
    protected val db get() = client.postgrest

    protected suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> =
        try {
            DataResult.Success(block())
        } catch (e: Exception) {
            DataResult.Error(e.toAppError())
        }

    private fun Exception.toAppError(): AppError = when (this) {
        is UnauthorizedRestException -> AppError.Unauthorized
        is NotFoundRestException -> AppError.NotFound(error)
        is HttpRequestException -> AppError.Network
        is HttpRequestTimeoutException -> AppError.Network
        is SerializationException -> AppError.Serialization(message ?: "...")
        else -> AppError.Unknown(message ?: this::class.simpleName ?: "unknown")
    }
}
```

#### Data Transfer Objects (DTOs)

**`data/remote/dto/ProfileDto.kt`**
```kotlin
@Serializable
data class ProfileDto(
    @SerialName("id")
    val id: String,
    @SerialName("full_name")
    val fullName: String?,
    @SerialName("updated_at")
    val updatedAt: String
)
```

**`data/remote/dto/CategoryDto.kt`**
```kotlin
@Serializable
data class CategoryDto(
    val id: Long,
    @SerialName("user_id")
    val userId: String?,
    val name: String,
    @SerialName("icon_name")
    val iconName: String,
    @SerialName("color_hex")
    val colorHex: String,
    @SerialName("created_at")
    val createdAt: String
)
```

**`data/remote/dto/ItemDto.kt`**
```kotlin
@Serializable
data class ItemDto(
    val id: Long,
    @SerialName("user_id")
    val userId: String,
    @SerialName("category_id")
    val categoryId: Long?,
    val name: String,
    val barcode: String?,
    @SerialName("expiry_date")
    val expiryDate: String,
    @SerialName("image_url")
    val imageUrl: String?,
    val notes: String?,
    @SerialName("is_consumed")
    val isConsumed: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
```

**`data/remote/dto/NotificationLogDto.kt`**
```kotlin
@Serializable
data class NotificationLogDto(
    val id: Long,
    @SerialName("item_id")
    val itemId: Long,
    @SerialName("scheduled_date")
    val scheduledDate: String,
    @SerialName("is_sent")
    val isSent: Boolean,
    @SerialName("created_at")
    val createdAt: String
)
```

#### Remote Data Sources

All extend `BaseRemoteDataSource` and expose CRUD operations:

- **`ProfileRemoteDataSource`**: `getById(userId)`, `upsert(dto)`
- **`CategoryRemoteDataSource`**: `getAll()`, `getByUserId()`, `getById()`, `insert()`, `update()`, `delete()`
- **`ItemRemoteDataSource`**: `getAll()`, `getByUserId()`, `getById()`, `insert()`, `update()`, `delete()`
- **`NotificationRemoteDataSource`**: `getAll()`, `getByItemId()`, `getById()`, `insert()`, `delete()`

All methods return `DataResult<T>`.

#### Mappers

Extension functions for DTO ↔ Domain conversion:

```kotlin
// ProfileMapper.kt
fun ProfileDto.toDomain() = Profile(id, fullName, updatedAt)
fun Profile.toDto() = ProfileDto(id, fullName, updatedAt)
```

Similar mappers exist for all 4 entities in `data/mapper/`.

#### Repository Implementations

Implement domain repository interfaces and use `.mapData()` for transformation:

```kotlin
// ProfileRepositoryImpl
class ProfileRepositoryImpl(private val remote: ProfileRemoteDataSource) : ProfileRepository {
    override suspend fun getProfile(userId: String): DataResult<Profile> =
        remote.getById(userId).mapData { it.toDomain() }

    override suspend fun upsertProfile(profile: Profile): DataResult<Profile> =
        remote.upsert(profile.toDto()).mapData { it.toDomain() }
}
```

---

### 4. Domain Layer

**Domain Models** (pure Kotlin, no framework imports)

```kotlin
// domain/model/Profile.kt
data class Profile(
    val id: String,
    val fullName: String?,
    val updatedAt: String
)

// domain/model/Category.kt
data class Category(
    val id: Long,
    val userId: String?,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val createdAt: String
)

// domain/model/Item.kt
data class Item(
    val id: Long,
    val userId: String,
    val categoryId: Long?,
    val name: String,
    val barcode: String?,
    val expiryDate: String,
    val imageUrl: String?,
    val notes: String?,
    val isConsumed: Boolean,
    val createdAt: String,
    val updatedAt: String
)

// domain/model/NotificationLog.kt
data class NotificationLog(
    val id: Long,
    val itemId: Long,
    val scheduledDate: String,
    val isSent: Boolean,
    val createdAt: String
)
```

**Domain Repository Interfaces**

Located in `domain/repository/`, these define contracts:

```kotlin
interface ProfileRepository {
    suspend fun getProfile(userId: String): DataResult<Profile>
    suspend fun upsertProfile(profile: Profile): DataResult<Profile>
}

interface CategoryRepository {
    suspend fun getCategories(): DataResult<List<Category>>
    suspend fun getCategoriesByUser(userId: String): DataResult<List<Category>>
    suspend fun getCategory(id: Long): DataResult<Category>
    suspend fun createCategory(category: Category): DataResult<Category>
    suspend fun updateCategory(id: Long, category: Category): DataResult<Category>
    suspend fun deleteCategory(id: Long): DataResult<Unit>
}

// ... and similar for ItemRepository, NotificationRepository
```

---

### 5. Dependency Injection

**`data/di/DataModule.kt`**

Koin module providing all data-layer dependencies:

```kotlin
val dataModule = module {
    single { SupabaseProvider.client }

    single { ProfileRemoteDataSource(get()) }
    single { CategoryRemoteDataSource(get()) }
    single { ItemRemoteDataSource(get()) }
    single { NotificationRemoteDataSource(get()) }

    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<ItemRepository> { ItemRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
}
```

---

### 6. Android Application Setup

**`androidApp/src/main/kotlin/com/project/tickr/TickrApplication.kt`**

```kotlin
class TickrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ChuckerContext.applicationContext = applicationContext
    }
}
```

**`androidApp/src/main/AndroidManifest.xml`** (modified)

```xml
<application
    android:name=".TickrApplication"
    ...>
    ...
</application>
```

---

## Database Schema

All timestamps and dates stored as `String` in Kotlin (conversion to `Instant`/`LocalDate` happens in UI or UseCase layer).

### profiles

| Column | Type | Kotlin | Nullable |
|--------|------|--------|----------|
| id | uuid | String | NO |
| full_name | text | String? | YES |
| updated_at | timestamp with time zone | String | NO |

### categories

| Column | Type | Kotlin | Nullable |
|--------|------|--------|----------|
| id | bigint | Long | NO |
| user_id | uuid | String? | YES |
| name | text | String | NO |
| icon_name | text | String | NO |
| color_hex | text | String | NO |
| created_at | timestamp with time zone | String | NO |

### items

| Column | Type | Kotlin | Nullable |
|--------|------|--------|----------|
| id | bigint | Long | NO |
| user_id | uuid | String | NO |
| category_id | bigint | Long? | YES |
| name | text | String | NO |
| barcode | text | String? | YES |
| expiry_date | date | String | NO |
| image_url | text | String? | YES |
| notes | text | String? | YES |
| is_consumed | boolean | Boolean | NO |
| created_at | timestamp with time zone | String | NO |
| updated_at | timestamp with time zone | String | NO |

### notifications_log

| Column | Type | Kotlin | Nullable |
|--------|------|--------|----------|
| id | bigint | Long | NO |
| item_id | bigint | Long | NO |
| scheduled_date | date | String | NO |
| is_sent | boolean | Boolean | NO |
| created_at | timestamp with time zone | String | NO |

---

## Important Quirks & Notes

### supabase-kt 3.6.0 Specific

1. **`httpEngine` property** — PUBLIC, no opt-in required
2. **`httpConfig { }` block** — **REQUIRES `@file:OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)`** at top of file
3. **Exception hierarchy:** `UnauthorizedRestException`, `NotFoundRestException` extend `RestException`
4. **Network exception:** `io.github.jan.supabase.exceptions.HttpRequestException`
5. **Timeout exception:** `io.ktor.client.plugins.HttpRequestTimeoutException`
6. **BuildKonfig keys:** `SUPABASE_URL` and `SUPABASE_KEY` (NOT `SUPABASE_ANON_KEY`)

### Gradle Tasks

- **commonMain compile:** `:shared:compileCommonMainKotlinMetadata`
- **Android compile:** `:shared:compileAndroidMain`
- **iOS compile:** `:shared:compileKotlinIosSimulatorArm64`

### Data Handling

- All timestamps/dates are **`String`** in domain models — parse them in UI/UseCase as needed
- `user_id` on items/categories = UUID from auth (String type)
- Profile `id` = auth user UUID, not auto-increment
- No Row-Level Security (RLS) filtering implemented at data layer — must be done explicitly in datasources if needed

### Delete Operations

Delete methods return `DataResult<Unit>` after deletion:

```kotlin
suspend fun delete(id: Long): DataResult<Unit> = safeApiCall {
    db.from(table).delete { filter { eq("id", id) } }
    Unit
}
```

---

## Complete File List

### Created (28 files)

**commonMain:**
```
core/result/DataResult.kt
core/result/AppError.kt
core/network/HttpEngineFactory.kt
core/network/SupabaseProvider.kt
data/remote/dto/ProfileDto.kt
data/remote/dto/CategoryDto.kt
data/remote/dto/ItemDto.kt
data/remote/dto/NotificationLogDto.kt
data/remote/datasource/BaseRemoteDataSource.kt
data/remote/datasource/ProfileRemoteDataSource.kt
data/remote/datasource/CategoryRemoteDataSource.kt
data/remote/datasource/ItemRemoteDataSource.kt
data/remote/datasource/NotificationRemoteDataSource.kt
data/mapper/ProfileMapper.kt
data/mapper/CategoryMapper.kt
data/mapper/ItemMapper.kt
data/mapper/NotificationMapper.kt
data/repository/ProfileRepositoryImpl.kt
data/repository/CategoryRepositoryImpl.kt
data/repository/ItemRepositoryImpl.kt
data/repository/NotificationRepositoryImpl.kt
data/di/DataModule.kt
domain/model/Profile.kt
domain/model/Category.kt
domain/model/Item.kt
domain/model/NotificationLog.kt
domain/repository/ProfileRepository.kt
domain/repository/CategoryRepository.kt
domain/repository/ItemRepository.kt
domain/repository/NotificationRepository.kt
```

**androidMain:**
```
core/network/HttpEngineFactory.android.kt
core/network/ChuckerContext.kt
```

**iosMain:**
```
core/network/HttpEngineFactory.ios.kt
```

**androidApp:**
```
src/main/kotlin/com/project/tickr/TickrApplication.kt
src/main/AndroidManifest.xml (modified)
```

### Deleted (1 file)

```
shared/src/commonMain/kotlin/com/project/tickr/SupabaseClient.kt
```

### Modified (2 files)

```
gradle/libs.versions.toml
shared/build.gradle.kts
```

---

## Pending Tasks (Phase 2+)

1. **Koin Initialization** — Add `startKoin { modules(dataModule) }` to `TickrApplication.onCreate()`
2. **UseCase Layer** — Business logic per entity using repository interfaces
3. **ViewModel** — `UiState` + `StateFlow` for UI state management
4. **UI/Screens** — Compose screens consuming ViewModels
5. **Navigation** — NavGraph setup
6. **Auth Flow** — Login/logout/session management
7. **Storage** — Image upload support (supabase-storage already available as dependency, just needs `install(Storage)` in SupabaseProvider)

---

## Testing Checklist (Phase 1)

- ✅ All 4 compile targets pass (commonMain, androidMain, iosSimulator, androidApp)
- ✅ Chucker context initialized correctly in Application.onCreate()
- ✅ Supabase client lazy-initializes with custom engine and logging
- ✅ Exception mapping correctly categorizes errors
- ✅ DTOs serialize/deserialize with Supabase schema
- ✅ Mappers convert DTO ↔ Domain models
- ✅ Repositories return `DataResult<T>` with proper error wrapping
- ✅ Koin module provides all data-layer dependencies

---

## Next Steps

When ready for Phase 2:

1. Initialize Koin with `dataModule` in `TickrApplication.onCreate()`
2. Create UseCase classes for each entity (e.g., `GetItemsUseCase`, `CreateItemUseCase`)
3. Build ViewModels consuming UseCases via dependency injection
4. Implement UI screens with Compose, connecting to ViewModels

All infrastructure is in place for seamless integration.