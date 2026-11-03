# 🧠 Tickr App — Domain/UseCase Layer Phase 2 Implementation Documentation

**Date:** 2026-06-07  
**Status:** ✅ Complete  
**Next Phase:** UI Layer (ViewModel, UiState, StateFlow, Screens, Navigation)

---

## 📋 Overview

This document captures the **complete Domain/UseCase Layer implementation (Phase 2)** for the Tickr KMP project. The domain layer encapsulates all business logic, validation rules, and use cases that bridge the gap between the UI layer (to be implemented in Phase 3) and the data layer (completed in Phase 1).

### Architecture Stack
- **Framework:** Kotlin Multiplatform (KMP)
- **Core:** Result handling (`DataResult<T>`), domain validation, datetime utilities
- **Domain:** Pure Kotlin models, repository interfaces, 20 UseCases
- **DI Container:** Koin 4.2.1
- **DateTime:** kotlinx-datetime (Opsi A — fully typed, cross-platform)

---

## 🏗️ Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     UI Layer (Phase 3)                      │
│          ViewModel → StateFlow → Compose Screens            │
└──────────────────────────┬──────────────────────────────────┘
                           │ inject via Koin
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Domain/UseCase Layer (Phase 2) ✅             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  UseCase (20 classes)                                │   │
│  │  ├─ Profile: Get, Upsert                             │   │
│  │  ├─ Category: Get, GetByUser, Create, Update, Delete │   │
│  │  ├─ Item: Get, GetByUser, Create, Update, Delete     │   │
│  │  │          + MarkConsumed, GetExpiring             │   │
│  │  └─ Notification: Get, GetByItem, Schedule, Delete   │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Core Utilities                                      │   │
│  │  ├─ DataResult<T> (Success/Error)                    │   │
│  │  ├─ AppError (6 variants + Validation)              │   │
│  │  ├─ Validators (requireNotBlank, requireIsoDate...) │   │
│  │  ├─ DateTimeUtil (Opsi A: kotlinx-datetime)         │   │
│  │  └─ ErrorMessageProvider (error → user-friendly)    │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │ depends on
                           ▼
┌─────────────────────────────────────────────────────────────┐
│           Data Layer (Phase 1) ✅                           │
│  Repository Interface → RepositoryImpl → RemoteDataSource   │
│  ↓ via Supabase Postgrest ↓                               │
│  PostgreSQL Database                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Phase 2 Deliverables

### ✅ Core Layer Enhancements (8 files)

#### 1. **Result Handling** (`core/result/DataResult.kt`)

```kotlin
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val error: AppError) : DataResult<Nothing>
}
```

**New Helper Methods:**
- `mapData(transform)` — Transform success data (already existed)
- `onSuccess(action)` — Execute side effect on success
- `onError(action)` — Execute side effect on error
- `getOrNull()` — Extract data or return null
- `flatMap(transform)` — Chain multiple operations returning DataResult

**Usage Pattern in Phase 3 (ViewModel):**
```kotlin
useCase.invoke().onSuccess { data ->
    _uiState.value = UiState.Success(data)
}.onError { error ->
    _uiState.value = UiState.Error(errorMessageProvider.provide(error))
}
```

#### 2. **Error Handling** (`core/result/AppError.kt`)

```kotlin
sealed class AppError(open val message: String) {
    data object Network : AppError("Network unavailable")
    data object Unauthorized : AppError("Unauthorized / session invalid")
    data class NotFound(val entity: String) : AppError("$entity not found")
    data class Serialization(val detail: String) : AppError("Parse error: $detail")
    data class Validation(val field: String, val reason: String) : AppError(...)
    data class Unknown(val detail: String) : AppError(detail)
}
```

**New Variant:** `Validation(field, reason)` — for domain validation errors

#### 3. **Validation Framework** (`core/validation/`)

**ValidationResult.kt:**
```kotlin
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val field: String, val reason: String) : ValidationResult
}
```

**Validators.kt (Reusable):**
```kotlin
object Validators {
    fun requireNotBlank(value: String?, field: String): AppError.Validation?
    fun requireMaxLength(value: String?, max: Int, field: String): AppError.Validation?
    fun requireIsoDate(value: String?, field: String): AppError.Validation?
    fun requireHexColor(value: String?, field: String): AppError.Validation?
}
```

**Usage Pattern in UseCase:**
```kotlin
class CreateItemUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(item: Item): DataResult<Item> {
        Validators.requireNotBlank(item.name, "name")?.let {
            return DataResult.Error(it)
        }
        Validators.requireIsoDate(item.expiryDate, "expiry_date")?.let {
            return DataResult.Error(it)
        }
        return repository.createItem(item)
    }
}
```

#### 4. **DateTime Utilities** (`core/util/DateTimeUtil`)

**Strategy:** Opsi A — `kotlinx-datetime` with expect-actual pattern

**Interface (commonMain):**
```kotlin
interface DateTimeUtil {
    fun isWithinDays(isoDate: String, days: Int): Boolean
    fun isPast(isoDate: String): Boolean
    fun today(): String
}
```

**Implementation Pattern:**
- **commonMain:** `DateTimeUtilImpl` uses `getTodayString()` expect function
- **androidMain:** `java.time.LocalDate.now()`
- **iosMain:** `NSDateFormatter` via platform.Foundation

**Key Points:**
- All dates stored as ISO-8601 strings (yyyy-MM-dd) in domain models
- Parsing/comparison happens at domain layer (not in datasource)
- Safe error handling via try-catch (returns false on parse failure)

---

### ✅ Domain Error Handler (1 file)

#### **ErrorMessageProvider** (`domain/error/ErrorMessageProvider.kt`)

Maps all `AppError` types to user-friendly messages (Bahasa Indonesia):

```kotlin
class ErrorMessageProvider {
    fun provide(error: AppError): String = when (error) {
        is AppError.Network -> "Tidak ada koneksi. Coba lagi."
        is AppError.Unauthorized -> "Sesi berakhir. Silakan masuk kembali."
        is AppError.NotFound -> "${error.entity} tidak ditemukan."
        is AppError.Serialization -> "Gagal memproses data."
        is AppError.Validation -> error.reason
        is AppError.Unknown -> "Terjadi kesalahan. Coba lagi."
    }
}
```

**Usage in Phase 3 (ViewModel):**
```kotlin
val errorMessage = errorMessageProvider.provide(error)
_uiState.value = UiState.Error(errorMessage)
// Display in Snackbar or error composable
```

---

### ✅ UseCase Layer (20 files)

All UseCases follow the same pattern:

```kotlin
class [Operation]UseCase(
    private val repository: XyzRepository,
    // optional: private val dateTime: DateTimeUtil, other helpers
) {
    suspend operator fun invoke(...args): DataResult<ReturnType> {
        // 1. Validate domain rules (if write operation)
        // 2. Delegate to repository
        // 3. Return DataResult (never throw)
    }
}
```

#### **A. Profile UseCases (2)**

| UseCase | Signature | Validation | Notes |
|---------|-----------|-----------|-------|
| `GetProfileUseCase` | `(userId: String)` → `DataResult<Profile>` | — | Read-only passthrough |
| `UpsertProfileUseCase` | `(profile: Profile)` → `DataResult<Profile>` | `fullName` max 100 chars | Insert or update |

**File:** `domain/usecase/profile/[GetProfileUseCase.kt, UpsertProfileUseCase.kt]`

#### **B. Category UseCases (6)**

| UseCase | Signature | Validation | Notes |
|---------|-----------|-----------|-------|
| `GetCategoriesUseCase` | `()` → `DataResult<List<Category>>` | — | All categories |
| `GetCategoriesByUserUseCase` | `(userId: String)` → `DataResult<List<Category>>` | — | User's categories |
| `GetCategoryUseCase` | `(id: Long)` → `DataResult<Category>` | — | Single by ID |
| `CreateCategoryUseCase` | `(category: Category)` → `DataResult<Category>` | name, iconName, colorHex (hex color format) | New category |
| `UpdateCategoryUseCase` | `(category: Category)` → `DataResult<Category>` | Same as create | Update existing |
| `DeleteCategoryUseCase` | `(id: Long)` → `DataResult<Unit>` | — | Delete by ID |

**Files:** `domain/usecase/category/*.kt` (6 files)

#### **C. Item UseCases (8)**

| UseCase | Signature | Validation | Notes |
|---------|-----------|-----------|-------|
| `GetItemsUseCase` | `()` → `DataResult<List<Item>>` | — | All items |
| `GetItemsByUserUseCase` | `(userId: String)` → `DataResult<List<Item>>` | — | User's items |
| `GetItemUseCase` | `(id: Long)` → `DataResult<Item>` | — | Single by ID |
| `CreateItemUseCase` | `(item: Item)` → `DataResult<Item>` | name, expiryDate (ISO-8601) | New item |
| `UpdateItemUseCase` | `(item: Item)` → `DataResult<Item>` | Same as create | Update item |
| `DeleteItemUseCase` | `(id: Long)` → `DataResult<Unit>` | — | Delete by ID |
| `MarkItemConsumedUseCase` | `(item: Item, consumed: Boolean)` → `DataResult<Item>` | — | Toggle consumption status via copy+update |
| `GetExpiringItemsUseCase` | `(userId: String, withinDays: Int = 7)` → `DataResult<List<Item>>` | — | **Business rule:** Filter !isConsumed && isWithinDays, sort by expiryDate |

**Files:** `domain/usecase/item/*.kt` (8 files)

**GetExpiringItemsUseCase Example (Business Logic):**
```kotlin
class GetExpiringItemsUseCase(
    private val repository: ItemRepository,
    private val dateTime: DateTimeUtil
) {
    suspend operator fun invoke(userId: String, withinDays: Int = 7): DataResult<List<Item>> =
        repository.getItemsByUser(userId).mapData { items ->
            items.filter { !it.isConsumed && dateTime.isWithinDays(it.expiryDate, withinDays) }
                 .sortedBy { it.expiryDate }
        }
}
```

#### **D. Notification UseCases (4)**

| UseCase | Signature | Validation | Notes |
|---------|-----------|-----------|-------|
| `GetNotificationsUseCase` | `()` → `DataResult<List<NotificationLog>>` | — | All notifications |
| `GetNotificationsByItemUseCase` | `(itemId: Long)` → `DataResult<List<NotificationLog>>` | — | By item ID |
| `ScheduleNotificationUseCase` | `(log: NotificationLog)` → `DataResult<NotificationLog>` | scheduledDate must be ISO-8601 and NOT in past | Schedule new notification |
| `DeleteNotificationUseCase` | `(id: Long)` → `DataResult<Unit>` | — | Delete by ID |

**Files:** `domain/usecase/notification/*.kt` (4 files)

**ScheduleNotificationUseCase Example (Complex Validation):**
```kotlin
class ScheduleNotificationUseCase(
    private val repository: NotificationRepository,
    private val dateTime: DateTimeUtil
) {
    suspend operator fun invoke(log: NotificationLog): DataResult<NotificationLog> {
        Validators.requireIsoDate(log.scheduledDate, "scheduled_date")?.let {
            return DataResult.Error(it)
        }
        if (dateTime.isPast(log.scheduledDate)) {
            return DataResult.Error(AppError.Validation("scheduled_date", "cannot be in the past"))
        }
        return repository.createNotification(log)
    }
}
```

---

### ✅ Dependency Injection (2 files)

#### **DomainModule.kt** (`domain/di/DomainModule.kt`)

```kotlin
val domainModule = module {
    // Core helpers
    single<DateTimeUtil> { DateTimeUtilImpl() }
    single { ErrorMessageProvider() }

    // Profile (2)
    factory { GetProfileUseCase(get()) }
    factory { UpsertProfileUseCase(get()) }

    // Category (6)
    factory { GetCategoriesUseCase(get()) }
    factory { GetCategoriesByUserUseCase(get()) }
    factory { GetCategoryUseCase(get()) }
    factory { CreateCategoryUseCase(get()) }
    factory { UpdateCategoryUseCase(get()) }
    factory { DeleteCategoryUseCase(get()) }

    // Item (8)
    factory { GetItemsUseCase(get()) }
    factory { GetItemsByUserUseCase(get()) }
    factory { GetItemUseCase(get()) }
    factory { CreateItemUseCase(get()) }
    factory { UpdateItemUseCase(get()) }
    factory { DeleteItemUseCase(get()) }
    factory { MarkItemConsumedUseCase(get()) }
    factory { GetExpiringItemsUseCase(get(), get()) }  // repo + DateTimeUtil

    // Notification (4)
    factory { GetNotificationsUseCase(get()) }
    factory { GetNotificationsByItemUseCase(get()) }
    factory { ScheduleNotificationUseCase(get(), get()) }  // repo + DateTimeUtil
    factory { DeleteNotificationUseCase(get()) }
}
```

**Key Points:**
- UseCases are `factory` (stateless, created fresh each time)
- `DateTimeUtil` & `ErrorMessageProvider` are `single` (singletons)
- Repositories injected from `dataModule` via `get()`

#### **InitKoin.kt** (`di/InitKoin.kt`)

```kotlin
fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) = startKoin {
    appDeclaration()
    modules(dataModule, domainModule)
}
```

**Usage in Phase 3 (Android):**
```kotlin
class TickrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ChuckerContext.applicationContext = applicationContext
        initKoin {
            androidContext(this@TickrApplication)
        }
    }
}
```

---

## 📦 Domain Models (from Phase 1)

All domain models are **pure Kotlin data classes** (no framework dependencies):

### Profile
```kotlin
data class Profile(
    val id: String,
    val fullName: String?,
    val updatedAt: String  // ISO timestamp
)
```

### Category
```kotlin
data class Category(
    val id: Long,
    val userId: String?,
    val name: String,
    val iconName: String,
    val colorHex: String,  // #RRGGBB or #AARRGGBB
    val createdAt: String  // ISO timestamp
)
```

### Item
```kotlin
data class Item(
    val id: Long,
    val userId: String,
    val categoryId: Long?,
    val name: String,
    val barcode: String?,
    val expiryDate: String,  // yyyy-MM-dd
    val imageUrl: String?,
    val notes: String?,
    val isConsumed: Boolean,
    val createdAt: String,   // ISO timestamp
    val updatedAt: String    // ISO timestamp
)
```

### NotificationLog
```kotlin
data class NotificationLog(
    val id: Long,
    val itemId: Long,
    val scheduledDate: String,  // yyyy-MM-dd
    val isSent: Boolean,
    val createdAt: String       // ISO timestamp
)
```

---

## 🔗 Repository Interfaces (from Phase 1)

All repositories return `DataResult<T>` and are injected into UseCases:

### ProfileRepository
```kotlin
interface ProfileRepository {
    suspend fun getProfile(userId: String): DataResult<Profile>
    suspend fun upsertProfile(profile: Profile): DataResult<Profile>
}
```

### CategoryRepository
```kotlin
interface CategoryRepository {
    suspend fun getCategories(): DataResult<List<Category>>
    suspend fun getCategoriesByUser(userId: String): DataResult<List<Category>>
    suspend fun getCategory(id: Long): DataResult<Category>
    suspend fun createCategory(category: Category): DataResult<Category>
    suspend fun updateCategory(category: Category): DataResult<Category>
    suspend fun deleteCategory(id: Long): DataResult<Unit>
}
```

### ItemRepository
```kotlin
interface ItemRepository {
    suspend fun getItems(): DataResult<List<Item>>
    suspend fun getItemsByUser(userId: String): DataResult<List<Item>>
    suspend fun getItem(id: Long): DataResult<Item>
    suspend fun createItem(item: Item): DataResult<Item>
    suspend fun updateItem(item: Item): DataResult<Item>
    suspend fun deleteItem(id: Long): DataResult<Unit>
}
```

### NotificationRepository
```kotlin
interface NotificationRepository {
    suspend fun getNotifications(): DataResult<List<NotificationLog>>
    suspend fun getNotificationsByItem(itemId: Long): DataResult<List<NotificationLog>>
    suspend fun getNotification(id: Long): DataResult<NotificationLog>
    suspend fun createNotification(notification: NotificationLog): DataResult<NotificationLog>
    suspend fun deleteNotification(id: Long): DataResult<Unit>
}
```

---

## 🎯 Key Patterns & Conventions

### 1. UseCase Pattern

**All UseCases follow this template:**

```kotlin
class [OperationName]UseCase(
    private val repository: [SomeRepository]
    // optional: private val helper: Helper
) {
    suspend operator fun invoke(args): DataResult<ReturnType> {
        // Step 1: Validate (if write operation)
        Validators.requireNotBlank(value, "field")?.let {
            return DataResult.Error(it)
        }
        
        // Step 2: Delegate to repository
        return repository.operation(args)
        
        // OR: Transform result
        // return repository.operation(args).mapData { /* transform */ }
    }
}
```

**Key Rules:**
- Never throw exceptions → return `DataResult.Error(...)`
- Single responsibility → one operation per UseCase
- Inject repository interface (not implementation)
- Use `mapData` to transform success data before returning

### 2. Validation Pattern

**For read operations:** No validation needed, passthrough

**For write operations:** Validate before hitting repository

```kotlin
suspend operator fun invoke(entity: Entity): DataResult<Entity> {
    Validators.requireNotBlank(entity.name, "name")?.let {
        return DataResult.Error(it)  // Return error, don't throw
    }
    return repository.createEntity(entity)
}
```

### 3. Business Logic Pattern

**For complex operations:** Use helper (like `DateTimeUtil`) to apply business rules

```kotlin
suspend operator fun invoke(userId: String, withinDays: Int): DataResult<List<Item>> =
    repository.getItemsByUser(userId).mapData { items ->
        // Apply business rule: filter + sort
        items.filter { !it.isConsumed && dateTime.isWithinDays(it.expiryDate, withinDays) }
             .sortedBy { it.expiryDate }
    }
```

---

## 🚀 Integration with Phase 3 (UI Layer)

### ViewModel Structure (Preview)

```kotlin
class ItemListViewModel(
    private val getItemsByUserUseCase: GetItemsByUserUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val errorMessageProvider: ErrorMessageProvider
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadItems(userId: String) {
        viewModelScope.launch {
            getItemsByUserUseCase(userId)
                .onSuccess { items ->
                    _uiState.value = UiState.Success(items)
                }
                .onError { error ->
                    _uiState.value = UiState.Error(errorMessageProvider.provide(error))
                }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            deleteItemUseCase(id)
                .onSuccess {
                    // Refresh list after delete
                    loadItems(currentUserId)
                }
                .onError { error ->
                    // Show error
                }
        }
    }
}
```

### Dependency Injection in ViewModel

**Using Koin factory:**
```kotlin
@Composable
fun ItemListScreen() {
    val viewModel: ItemListViewModel = koinViewModel()
    // ViewModel receives all UseCases + helpers via Koin
    val uiState = viewModel.uiState.collectAsState()
    // Render based on uiState
}
```

### Typical Flow (Phase 3)

```
User clicks "Delete Item"
↓
ViewModel.deleteItem(itemId) calls DeleteItemUseCase(itemId)
↓
UseCase delegates to ItemRepository.deleteItem(itemId)
↓
Repository calls ItemRemoteDataSource.delete(itemId)
↓
DataSource makes HTTP request to Supabase → PostgreSQL
↓
Response wrapped in DataResult.Success(Unit) or DataResult.Error(AppError)
↓
ViewModel receives result via onSuccess/onError
↓
ViewModel updates _uiState (StateFlow)
↓
Compose collects new state → recompose → UI updates
```

---

## 📝 File Structure

```
commonMain/kotlin/com/project/tickr/
│
├── core/
│   ├── result/
│   │   ├── DataResult.kt                    ✅ +4 helpers
│   │   └── AppError.kt                      ✅ +Validation
│   ├── validation/
│   │   ├── ValidationResult.kt              ✅ NEW
│   │   └── Validators.kt                    ✅ NEW
│   └── util/
│       └── DateTimeUtil.kt                  ✅ NEW (interface)
│       └── DateTimeUtilImpl.kt               ✅ NEW (commonMain)
│
├── domain/
│   ├── model/
│   │   ├── Profile.kt                       ✅ (exists)
│   │   ├── Category.kt                      ✅ (exists)
│   │   ├── Item.kt                          ✅ (exists)
│   │   └── NotificationLog.kt               ✅ (exists)
│   │
│   ├── repository/
│   │   ├── ProfileRepository.kt             ✅ (exists)
│   │   ├── CategoryRepository.kt            ✅ (exists)
│   │   ├── ItemRepository.kt                ✅ (exists)
│   │   └── NotificationRepository.kt        ✅ (exists)
│   │
│   ├── error/
│   │   └── ErrorMessageProvider.kt          ✅ NEW
│   │
│   ├── usecase/
│   │   ├── profile/
│   │   │   ├── GetProfileUseCase.kt         ✅ NEW
│   │   │   └── UpsertProfileUseCase.kt      ✅ NEW
│   │   ├── category/
│   │   │   ├── GetCategoriesUseCase.kt      ✅ NEW
│   │   │   ├── GetCategoriesByUserUseCase.kt
│   │   │   ├── GetCategoryUseCase.kt
│   │   │   ├── CreateCategoryUseCase.kt
│   │   │   ├── UpdateCategoryUseCase.kt
│   │   │   └── DeleteCategoryUseCase.kt
│   │   ├── item/
│   │   │   ├── GetItemsUseCase.kt
│   │   │   ├── GetItemsByUserUseCase.kt
│   │   │   ├── GetItemUseCase.kt
│   │   │   ├── CreateItemUseCase.kt
│   │   │   ├── UpdateItemUseCase.kt
│   │   │   ├── DeleteItemUseCase.kt
│   │   │   ├── MarkItemConsumedUseCase.kt
│   │   │   └── GetExpiringItemsUseCase.kt
│   │   └── notification/
│   │       ├── GetNotificationsUseCase.kt
│   │       ├── GetNotificationsByItemUseCase.kt
│   │       ├── ScheduleNotificationUseCase.kt
│   │       └── DeleteNotificationUseCase.kt
│   │
│   └── di/
│       └── DomainModule.kt                  ✅ NEW
│
├── di/
│   └── InitKoin.kt                          ✅ NEW
│
└── data/
    └── di/DataModule.kt                     ✅ (exists, from Phase 1)

androidMain/kotlin/com/project/tickr/
└── core/util/
    └── DateTimeUtil.android.kt              ✅ NEW (java.time)

iosMain/kotlin/com/project/tickr/
└── core/util/
    └── DateTimeUtil.ios.kt                  ✅ NEW (NSDateFormatter)
```

---

## ✅ Compile Status

| Target | Status | Notes |
|--------|--------|-------|
| `:shared:compileCommonMainKotlinMetadata` | ✅ SUCCESS | Core + domain metadata |
| `:shared:compileAndroidMain` | ✅ SUCCESS | Android-specific (java.time) |
| `:shared:compileKotlinIosSimulatorArm64` | ✅ SUCCESS | iOS-specific (NSDateFormatter) |

---

## 🔐 Key Guarantees

✅ **No UI/Framework Dependencies in Domain Layer**
- No imports from `android.*`, `androidx.*`, `compose.*`
- Pure Kotlin, platform-agnostic business logic
- Can be tested independently of UI

✅ **Type-Safe Error Handling**
- All operations return `DataResult<T>` (never throw to caller)
- Validation errors mapped to `AppError.Validation`
- Exhaustive error handling in `ErrorMessageProvider`

✅ **Reusable Validators**
- `Validators` object provides common field rules
- Easy to add custom validators
- Consistent error messages across app

✅ **Dependency Injection Ready**
- All 20 UseCases registered in Koin
- Repositories injected via `get()`
- Phase 3 ViewModels receive UseCases automatically

✅ **DateTime Handling**
- Opsi A: kotlinx-datetime (fully typed, safe parsing)
- expect-actual pattern (Android: java.time, iOS: NSDateFormatter)
- ISO-8601 strings throughout domain (no raw timestamps)

---

## 📚 Next Steps for Phase 3 (UI Layer)

1. **Create UiState classes** — Define sealed class for each screen state
   ```kotlin
   sealed interface ItemListUiState {
       data object Loading : ItemListUiState
       data class Success(val items: List<Item>) : ItemListUiState
       data class Error(val message: String) : ItemListUiState
   }
   ```

2. **Implement ViewModels** — Inject UseCases, manage StateFlow
   ```kotlin
   class ItemListViewModel(
       val getItemsByUserUseCase: GetItemsByUserUseCase,
       val errorMessageProvider: ErrorMessageProvider
   ) : ViewModel()
   ```

3. **Build Compose Screens** — Collect StateFlow, call ViewModel methods
   ```kotlin
   @Composable
   fun ItemListScreen(viewModel: ItemListViewModel) {
       val uiState = viewModel.uiState.collectAsState()
       // Render based on UiState
   }
   ```

4. **Setup Navigation** — NavGraph with Compose Navigation
   ```kotlin
   NavHost(navController, startDestination = "items") {
       composable("items") { ItemListScreen(...) }
       composable("item/{id}") { ItemDetailScreen(...) }
   }
   ```

5. **Implement Auth Flow** — Optional: Create `SessionUseCase` if needed
   ```kotlin
   class GetCurrentUserIdUseCase(private val authProvider: AuthProvider)
   ```

6. **Add Storage Support** — Enable Supabase Storage for image upload
   ```kotlin
   // In SupabaseProvider: install(Storage)
   class UploadItemImageUseCase(private val repository: ItemRepository)
   ```

---

## 📖 Reference Guide

### UseCase Injection Pattern
```kotlin
val useCase: GetItemsUseCase = get()  // Koin
```

### DataResult Handling
```kotlin
result
    .onSuccess { data -> /* handle success */ }
    .onError { error -> /* handle error */ }
```

### Date Comparison
```kotlin
val today = dateTime.today()
val isPast = dateTime.isPast("2026-06-07")
val isExpiring = dateTime.isWithinDays("2026-06-14", 7)
```

### Validation in UseCase
```kotlin
Validators.requireNotBlank(name, "name")?.let {
    return DataResult.Error(it)
}
```

### Error to Message
```kotlin
val message = errorMessageProvider.provide(error)
```

---

## 🎓 Summary

**Phase 2 (Domain/UseCase Layer) delivers:**

- ✅ 20 fully implemented UseCases (CRUD + business logic)
- ✅ Type-safe result handling (`DataResult<T>`)
- ✅ Exhaustive error handling with user-friendly messages
- ✅ Reusable validation framework
- ✅ Cross-platform datetime utilities (Opsi A)
- ✅ Koin DI with 20 UseCases + helpers
- ✅ Clean architecture (no framework deps in domain)
- ✅ Ready for Phase 3 (ViewModel + UI integration)

**All compile targets: ✅ SUCCESS**

---

**END OF DOMAIN/USECASE LAYER PHASE 2 DOCUMENTATION**
