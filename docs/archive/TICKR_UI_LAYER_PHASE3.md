# 🎨 TICKR — UI / PRESENTATION LAYER Implementation Spec (Phase 3)

> **Target Executor**: Claude Code CLI
> **Scope**: **UI / PRESENTATION LAYER** — `UiState`, `UiAction`/`UiEvent`, `ViewModel` + `StateFlow`, Navigation, **Auth flow & session storage**, plus penyempurnaan kecil pada Data & Domain layer yang dibutuhkan (auth + storage). **Compose Screens DIKECUALIKAN** (lihat §0 — dikerjakan terpisah di Phase 3.5 setelah desain/tema/warna/fitur diterima).
> **Package**: `com.project.tickr`
> **Backend**: Supabase (PostgreSQL + Auth + Storage) via `supabase-kt` (Ktor)
> **Prasyarat**: Data Layer Phase 1 **Complete**; Domain/UseCase Layer Phase 2 **Complete** (lihat `TICKR_DOMAIN_USECASE_PHASE2_DOCUMENTATION.md`).

---

## ✅ PRECONDITIONS (sudah selesai — JANGAN diulang)

- [x] **Data Layer Phase 1 complete**: DTO, BaseRemoteDataSource, RemoteDataSource (4 entity), Mapper, RepositoryImpl, `DataModule` (Koin).
- [x] **Domain/UseCase Layer Phase 2 complete**: 20 UseCase, `DataResult<T>` (+`onSuccess`/`onError`/`getOrNull`/`flatMap`/`mapData`), `AppError` (+`Validation`), `Validators`, `ValidationResult`, `DateTimeUtil` (Opsi A — kotlinx-datetime, expect/actual), `ErrorMessageProvider`, `DomainModule`, `di/InitKoin.kt`.
- [x] `SupabaseProvider` tunggal + Ktor Logging + Chucker (Android) sudah jalan.
- [x] Domain model (pure Kotlin) & repository interface untuk 4 entity sudah ada.
- [x] Dependencies sudah ada: supabase-kt (Postgrest), ktor, koin, kotlinx-serialization, kotlinx-coroutines, kotlinx-datetime, chucker.

> **JANGAN** mengubah arsitektur Phase 1/2 yang sudah jadi (DTO, datasource, mapper, `toAppError()`, `SupabaseProvider`, `BuildKonfig`, engine network, UseCase signature).
> Dependency **baru** yang DIBUTUHKAN phase ini (lihat §0 — beberapa butuh konfirmasi): plugin `Auth` (GoTrue) & `Storage` dari supabase-kt, `koin-compose-viewmodel` (atau `lifecycle-viewmodel-compose` + `koin-androidx-compose`), Compose Navigation (`androidx.navigation:navigation-compose` atau `org.jetbrains.androidx.navigation:navigation-compose` untuk CMP), `multiplatform-settings` (untuk session storage). **Jika ada yang belum ada → STOP & tanya sebelum menambah.**

---

## 0️⃣ KEPUTUSAN AWAL (KONFIRMASI DULU sebelum mulai)

Ada 4 keputusan yang menentukan dependency & struktur. **Tunggu jawaban user**; jika tidak dijawab, pakai **Default** yang tertera.

| # | Keputusan | Opsi | Default bila tak dijawab |
|---|---|---|---|
| **0.1** | **Compose target** | (A) Compose Multiplatform (CMP) di `commonMain` — ViewModel & Navigation shared. (B) ViewModel di `shared` (commonMain), Compose UI native per-platform (Android Compose + SwiftUI). | **(B)** — ViewModel & UiState/UiAction shared di `commonMain`; Screens/Navigation native per platform (cocok dengan keahlian SwiftUI user & sifat KMP). ViewModel pakai `androidx.lifecycle.ViewModel` (KMP-compatible) + `viewModelScope`. |
| **0.2** | **Navigation** | (A) `navigation-compose` (jika CMP). (B) Definisikan kontrak rute (sealed `Destination` + `Navigator` interface) di `commonMain`, implementasi navigasi di tiap platform. | **(B)** — sealed `Destination` di commonMain; host navigasi diserahkan ke platform layer (Phase 3.5). |
| **0.3** | **Session storage** | (A) `multiplatform-settings` (KMP key-value, sederhana). (B) Andalkan persistensi sesi bawaan supabase-kt Auth (`SettingsSessionManager`) — minim kode. | **(B)** — pakai session manager bawaan supabase-kt Auth; `multiplatform-settings` hanya untuk flag UI (mis. onboarding/"remember last user") bila diperlukan. **Konfirmasi bila butuh A.** |
| **0.4** | **Auth scope sekarang** | (A) Email+password (sign up / sign in / sign out / observe session). (B) +OAuth (Google/Apple), magic link, reset password. | **(A)** — email+password + observe session + sign out. OAuth/magic-link → phase berikutnya. |

> ⛔ **STOP & TANYA** keempat poin di atas sebelum menulis kode bila ada ambiguitas. Catat keputusan ke ringkasan akhir.

---

## 🎯 SCOPE & NON-GOALS

### IN SCOPE (kerjakan sekarang)
1. **UiState** — sealed/`data class` immutable per layar (state container), satu sumber kebenaran untuk UI.
2. **UiAction (intent) & UiEvent (one-shot effect)** — kontrak input user → ViewModel, dan efek sekali-pakai (navigate, snackbar) ViewModel → UI.
3. **ViewModel + StateFlow** — `MutableStateFlow` privat + `StateFlow` publik (`asStateFlow()`); konsumsi UseCase via `onSuccess/onError`; map error via `ErrorMessageProvider`.
4. **Auth flow (logika)** — `AuthRepository` + UseCase (sign up / sign in / sign out / observe session / current user id) + `AuthViewModel` + `AuthUiState`. Wiring ke layar login dikerjakan Phase 3.5.
5. **Session storage / restore** — restore sesi saat app start; expose `userId` ke ViewModel lain (gantikan `userId` yang sebelumnya "diteruskan dari pemanggil").
6. **Navigation contract** — sealed `Destination` + argumen rute (type-safe) di `commonMain`; `Navigator` abstraction (sesuai §0.2).
7. **DI presentation** — `presentation/di/PresentationModule.kt` (Koin) untuk semua ViewModel; daftarkan ke `initKoin()`.
8. **Penyempurnaan Data & Domain layer yang kurang** (lihat §7) — Auth & Storage data source/repo/usecase, tanpa mengubah desain inti Phase 1/2.

### OUT OF SCOPE (JANGAN dikerjakan di phase ini)
- ❌ **Compose Screens / komponen visual / Theme / warna / typography / ikon** → **Phase 3.5** (menunggu desain dari user). Lihat §0.
- ❌ Local offline cache / DB (Room/SQLDelight).
- ❌ OAuth/magic-link/reset-password kecuali §0.4 dipilih (B).
- ❌ Push notification scheduling OS-level (WorkManager/UNUserNotificationCenter) — phase berikutnya; phase ini hanya CRUD `NotificationLog` (sudah dari Phase 2).
- ❌ Perubahan DTO/mapper/`toAppError()`/`SupabaseProvider` di luar penambahan plugin Auth/Storage.

---

## 🗂️ TARGET STRUCTURE (UI Layer Phase — tanpa Screens)

```
commonMain/kotlin/com/project/tickr/
│
├── core/
│   └── (tetap dari Phase 2 — JANGAN ubah)
│
├── domain/
│   ├── model/
│   │   ├── ... (4 model existing)
│   │   └── AuthUser.kt                         # ⭐ NEW — { id, email } (pure Kotlin)
│   │   └── AuthSession.kt                       # ⭐ NEW — { user: AuthUser, isExpired } (opsional)
│   ├── repository/
│   │   ├── ... (4 repo existing)
│   │   └── AuthRepository.kt                    # ⭐ NEW — kontrak auth (no framework import)
│   ├── usecase/
│   │   ├── ... (20 usecase existing)
│   │   └── auth/                                # ⭐ NEW
│   │       ├── SignUpUseCase.kt                 # validasi email & password
│   │       ├── SignInUseCase.kt
│   │       ├── SignOutUseCase.kt
│   │       ├── ObserveSessionUseCase.kt         # Flow<AuthUser?> (reactive)
│   │       └── GetCurrentUserIdUseCase.kt       # String? (gantikan "userId diteruskan dari pemanggil")
│   │   └── item/
│   │       └── UploadItemImageUseCase.kt        # ⭐ NEW (Storage) — opsional, lihat §7
│   └── di/DomainModule.kt                       # (exists) — TAMBAH auth usecase + upload usecase
│
├── presentation/                               # ⭐ FOKUS PHASE INI (logika UI, tanpa Compose)
│   ├── common/
│   │   ├── UiText.kt                            # NEW — abstraksi teks (raw string / nanti string-res)
│   │   └── mvi/
│   │       ├── UiAction.kt                      # NEW — marker interface (per-feature extend)
│   │       └── UiEvent.kt                       # NEW — one-shot effect base (Navigate, ShowSnackbar)
│   │
│   ├── navigation/
│   │   ├── Destination.kt                       # NEW — sealed rute + argumen type-safe
│   │   └── Navigator.kt                         # NEW — abstraksi (navigate/back) — impl di platform (Phase 3.5)
│   │
│   ├── auth/
│   │   ├── AuthUiState.kt                       # NEW — { isLoading, email, password, error, mode }
│   │   ├── AuthAction.kt                        # NEW — EmailChanged, PasswordChanged, Submit, ToggleMode, SignOut
│   │   ├── AuthEvent.kt                         # NEW — NavigateToHome, ShowError
│   │   └── AuthViewModel.kt                     # NEW — sign in/up/out + observe session
│   │
│   ├── item/
│   │   ├── list/
│   │   │   ├── ItemListUiState.kt               # NEW — Loading/Success(items)/Empty/Error
│   │   │   ├── ItemListAction.kt                # NEW — Load, Refresh, Delete, MarkConsumed, ClickItem
│   │   │   ├── ItemListEvent.kt                 # NEW — NavigateToDetail, ShowSnackbar
│   │   │   └── ItemListViewModel.kt             # NEW
│   │   ├── detail/
│   │   │   ├── ItemDetailUiState.kt             # NEW
│   │   │   ├── ItemDetailAction.kt              # NEW
│   │   │   ├── ItemDetailEvent.kt               # NEW
│   │   │   └── ItemDetailViewModel.kt           # NEW
│   │   └── form/                                # create + edit (mode dari arg)
│   │       ├── ItemFormUiState.kt               # NEW — field-level state + validation errors
│   │       ├── ItemFormAction.kt                # NEW — *Changed, Submit
│   │       ├── ItemFormEvent.kt                 # NEW — Saved (navigate back), ShowError
│   │       └── ItemFormViewModel.kt             # NEW
│   │
│   ├── category/
│   │   ├── list/ { CategoryListUiState, *Action, *Event, *ViewModel }    # NEW
│   │   └── form/ { CategoryFormUiState, *Action, *Event, *ViewModel }    # NEW
│   │
│   ├── home/                                    # dashboard "expiring soon"
│   │   ├── HomeUiState.kt                       # NEW — expiringItems + counts
│   │   ├── HomeAction.kt                        # NEW
│   │   ├── HomeEvent.kt                         # NEW
│   │   └── HomeViewModel.kt                     # NEW — pakai GetExpiringItemsUseCase
│   │
│   ├── profile/
│   │   ├── ProfileUiState.kt / *Action / *Event / ProfileViewModel.kt   # NEW
│   │
│   └── di/
│       └── PresentationModule.kt                # NEW — Koin: semua ViewModel
│
├── data/
│   ├── remote/auth/
│   │   └── AuthRemoteDataSource.kt              # ⭐ NEW — bungkus supabase-kt Auth (GoTrue)
│   ├── remote/storage/                          # opsional (§7) — image upload
│   │   └── StorageRemoteDataSource.kt           # ⭐ NEW (opsional)
│   ├── repository/
│   │   └── AuthRepositoryImpl.kt                # ⭐ NEW — map GoTrue → AuthUser / AppError
│   └── di/DataModule.kt                         # (exists) — TAMBAH AuthDataSource + AuthRepositoryImpl
│
└── di/InitKoin.kt                               # (exists) — TAMBAH presentationModule ke modules(...)

androidMain/kotlin/com/project/tickr/
└── (Navigator impl & Compose host → Phase 3.5; phase ini boleh kosong)

iosMain/kotlin/com/project/tickr/
└── (Navigator impl & SwiftUI host → Phase 3.5)
```

> Semua **logika UI** (ViewModel/UiState/UiAction/Navigation contract) di `commonMain`. **TIDAK** ada Compose/SwiftUI screen di phase ini.

---

## ⭐ BAGIAN INTI: POLA MVI / UDF (Unidirectional Data Flow)

### Konsep
Satu arah aliran data: **UI → Action → ViewModel → UseCase → DataResult → reduce → UiState (StateFlow) → UI**. Efek sekali-pakai (navigasi, snackbar) lewat **UiEvent** (channel/SharedFlow), **bukan** disimpan di UiState.

```
        Action (intent)                       UiState (StateFlow, render)
UI ───────────────────────▶ ViewModel ─────────────────────────────▶ UI
                               │  invoke UseCase → DataResult
                               │  onSuccess/onError → reduce state
                               └── UiEvent (one-shot) ──▶ UI (navigate/snackbar)
```

### Aturan wajib
- **UiState immutable** (`data class` dengan `copy`, atau `sealed interface` untuk state diskret). Tidak ada logika di dalam state.
- **ViewModel tidak mengimpor** `data.*` (DTO/datasource/impl), tidak impor framework UI (Compose/SwiftUI/`android.*`). Hanya `domain.usecase.*`, `domain.model.*`, `core.*`, `kotlinx.coroutines.*`, `androidx.lifecycle.ViewModel`.
- **Semua pemanggilan UseCase** dibungkus `viewModelScope.launch { ... }` dan menangani `DataResult` via `onSuccess/onError`; error → `errorMessageProvider.provide(error)`.
- **Validasi tetap di domain** (UseCase). ViewModel boleh menampilkan ulang `AppError.Validation.reason` per-field, tapi **tidak menduplikasi aturan**.
- **One-shot event** via `Channel(Channel.BUFFERED)` → `receiveAsFlow()` (atau `MutableSharedFlow(extraBufferCapacity=1)`), **jangan** taruh navigasi/snackbar di StateFlow.
- Satu fitur = satu paket berisi `UiState` + `Action` + `Event` + `ViewModel`.

### Template ViewModel (list + reduce + event)
```kotlin
package com.project.tickr.presentation.item.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.item.DeleteItemUseCase
import com.project.tickr.domain.usecase.item.GetItemsByUserUseCase
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemListViewModel(
    private val getItemsByUser: GetItemsByUserUseCase,
    private val deleteItem: DeleteItemUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val errorMessages: ErrorMessageProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ItemListUiState())
    val state: StateFlow<ItemListUiState> = _state.asStateFlow()

    private val _events = Channel<ItemListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ItemListAction) {
        when (action) {
            ItemListAction.Load, ItemListAction.Refresh -> load()
            is ItemListAction.Delete -> delete(action.id)
            is ItemListAction.ClickItem ->
                viewModelScope.launch { _events.send(ItemListEvent.NavigateToDetail(action.id)) }
        }
    }

    private fun load() {
        val userId = getCurrentUserId() ?: run {
            _state.update { it.copy(isLoading = false, error = "Sesi berakhir. Silakan masuk kembali.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getItemsByUser(userId)
                .onSuccess { items -> _state.update { it.copy(isLoading = false, items = items) } }
                .onError { e -> _state.update { it.copy(isLoading = false, error = errorMessages.provide(e)) } }
        }
    }

    private fun delete(id: Long) {
        viewModelScope.launch {
            deleteItem(id)
                .onSuccess { load() }
                .onError { e -> _events.send(ItemListEvent.ShowSnackbar(errorMessages.provide(e))) }
        }
    }
}
```

### Template UiState (data class immutable)
```kotlin
package com.project.tickr.presentation.item.list

import com.project.tickr.domain.model.Item

data class ItemListUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()
}
```

### Template Action & Event
```kotlin
// ItemListAction.kt
package com.project.tickr.presentation.item.list

sealed interface ItemListAction {
    data object Load : ItemListAction
    data object Refresh : ItemListAction
    data class Delete(val id: Long) : ItemListAction
    data class ClickItem(val id: Long) : ItemListAction
}

// ItemListEvent.kt
sealed interface ItemListEvent {
    data class NavigateToDetail(val id: Long) : ItemListEvent
    data class ShowSnackbar(val message: String) : ItemListEvent
}
```

> **Catatan ViewModel KMP**: pakai `androidx.lifecycle:lifecycle-viewmodel` (sudah multiplatform). `viewModelScope` tersedia di commonMain pada versi lifecycle 2.8+. Jika versi lifecycle di proyek belum mendukung commonMain `viewModelScope` → **STOP & tanya** (alternatif: scope manual `CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)` yang di-`cancel` di `onCleared()`).

---

## 1️⃣ PRESENTATION — common (`UiText`, MVI base)

### `presentation/common/UiText.kt`
Abstraksi teks agar UI tidak hard-code string & siap untuk string resource nanti:
```kotlin
package com.project.tickr.presentation.common

sealed interface UiText {
    data class Raw(val value: String) : UiText
    // nanti bisa tambah: data class Res(val key: String, val args: List<Any> = emptyList())
}
```
> Default phase ini cukup `Raw` (output `ErrorMessageProvider` sudah string siap-tampil). **Jangan** over-engineer.

### `presentation/common/mvi/UiAction.kt` & `UiEvent.kt`
Marker interface (opsional) bila ingin base bersama. Boleh juga tiap fitur pakai sealed sendiri (seperti template). **Pilih satu pola dan konsisten.**

---

## 2️⃣ NAVIGATION — kontrak rute (commonMain)

### `presentation/navigation/Destination.kt`
```kotlin
package com.project.tickr.presentation.navigation

sealed interface Destination {
    val route: String

    data object Auth : Destination { override val route = "auth" }
    data object Home : Destination { override val route = "home" }
    data object ItemList : Destination { override val route = "items" }
    data class ItemDetail(val id: Long) : Destination {
        override val route = "items/$id"
        companion object { const val PATTERN = "items/{id}"; const val ARG_ID = "id" }
    }
    data object ItemCreate : Destination { override val route = "items/new" }
    data class ItemEdit(val id: Long) : Destination {
        override val route = "items/$id/edit"
        companion object { const val PATTERN = "items/{id}/edit"; const val ARG_ID = "id" }
    }
    data object CategoryList : Destination { override val route = "categories" }
    data object Profile : Destination { override val route = "profile" }
}
```

### `presentation/navigation/Navigator.kt`
```kotlin
package com.project.tickr.presentation.navigation

interface Navigator {
    fun navigate(destination: Destination, popUpToInclusive: Boolean = false)
    fun back()
}
```
> Implementasi konkret (NavController Compose / SwiftUI path) **dikerjakan Phase 3.5** di platform layer. Phase ini hanya kontrak + emisi `UiEvent.Navigate(destination)` dari ViewModel.

---

## 3️⃣ AUTH FLOW (logika) — UiState/Action/Event + ViewModel

### `presentation/auth/AuthUiState.kt`
```kotlin
package com.project.tickr.presentation.auth

data class AuthUiState(
    val mode: Mode = Mode.SignIn,
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
) {
    enum class Mode { SignIn, SignUp }
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isSubmitting
}
```

### `presentation/auth/AuthAction.kt` & `AuthEvent.kt`
```kotlin
sealed interface AuthAction {
    data class EmailChanged(val value: String) : AuthAction
    data class PasswordChanged(val value: String) : AuthAction
    data object ToggleMode : AuthAction
    data object Submit : AuthAction
    data object SignOut : AuthAction
}

sealed interface AuthEvent {
    data object NavigateToHome : AuthEvent
    data object NavigateToAuth : AuthEvent   // setelah sign out
    data class ShowError(val message: String) : AuthEvent
}
```

### `presentation/auth/AuthViewModel.kt`
- Observasi sesi saat init via `ObserveSessionUseCase` → bila ada user, emit `NavigateToHome`.
- `Submit`: pilih `SignInUseCase`/`SignUpUseCase` sesuai `mode`; `onSuccess` → emit `NavigateToHome`; `onError` → map `AppError.Validation` ke `emailError`/`passwordError`, lainnya ke `generalError`.
- `SignOut`: `SignOutUseCase` → emit `NavigateToAuth`.

> Validasi email/password ada di **UseCase** (lihat §7), bukan di ViewModel. ViewModel hanya menempatkan `reason` ke field yang sesuai berdasarkan `AppError.Validation.field`.

---

## 4️⃣ DAFTAR ViewModel & state per fitur (ringkas)

| Fitur | Paket | UiState (inti) | Action (inti) | Event (inti) | UseCase dipakai |
|---|---|---|---|---|---|
| Auth | `presentation/auth` | email, password, mode, isSubmitting, errors | EmailChanged, PasswordChanged, ToggleMode, Submit, SignOut | NavigateToHome, NavigateToAuth, ShowError | SignIn, SignUp, SignOut, ObserveSession |
| Home (dashboard) | `presentation/home` | isLoading, expiringItems, expiringCount, error | Load, Refresh, ClickItem | NavigateToDetail | GetCurrentUserId, GetExpiringItemsUseCase |
| Item List | `presentation/item/list` | isLoading, items, error | Load, Refresh, Delete, MarkConsumed, ClickItem, ClickAdd | NavigateToDetail, NavigateToCreate, ShowSnackbar | GetCurrentUserId, GetItemsByUser, DeleteItem, MarkItemConsumed |
| Item Detail | `presentation/item/detail` | isLoading, item, error | Load(id), Delete, MarkConsumed, ClickEdit | NavigateBack, NavigateToEdit, ShowSnackbar | GetItem, DeleteItem, MarkItemConsumed |
| Item Form (create/edit) | `presentation/item/form` | field state + per-field errors, isSaving | NameChanged, ExpiryChanged, CategoryChanged, NotesChanged, (PickImage), Submit | Saved, ShowError | GetItem (edit prefill), CreateItem, UpdateItem, (UploadItemImage), GetCategoriesByUser |
| Category List | `presentation/category/list` | isLoading, categories, error | Load, Delete, ClickAdd, ClickCategory | NavigateToForm, ShowSnackbar | GetCurrentUserId, GetCategoriesByUser, DeleteCategory |
| Category Form | `presentation/category/form` | name, iconName, colorHex, errors, isSaving | *Changed, Submit | Saved, ShowError | GetCategory (edit), CreateCategory, UpdateCategory |
| Profile | `presentation/profile` | isLoading, profile, fullName, isSaving, error | Load, FullNameChanged, Save, SignOut | Saved, NavigateToAuth, ShowSnackbar | GetCurrentUserId, GetProfile, UpsertProfile, SignOut |

> **Pola seragam** untuk setiap ViewModel: `MutableStateFlow(<State>())` + `asStateFlow()`, `Channel<Event>` + `receiveAsFlow()`, fungsi tunggal `onAction(action)`, semua UseCase via `viewModelScope.launch` + `onSuccess/onError`.

---

## 5️⃣ DI — `presentation/di/PresentationModule.kt`

```kotlin
package com.project.tickr.presentation.di

import com.project.tickr.presentation.auth.AuthViewModel
import com.project.tickr.presentation.home.HomeViewModel
import com.project.tickr.presentation.item.list.ItemListViewModel
import com.project.tickr.presentation.item.detail.ItemDetailViewModel
import com.project.tickr.presentation.item.form.ItemFormViewModel
import com.project.tickr.presentation.category.list.CategoryListViewModel
import com.project.tickr.presentation.category.form.CategoryFormViewModel
import com.project.tickr.presentation.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf       // jika pakai koin-compose-viewmodel
import org.koin.dsl.module

val presentationModule = module {
    // Gunakan `viewModel { ... }` (koin-android/compose) atau `factory { ... }`
    // tergantung mekanisme injeksi di platform (lihat §0.1). Contoh factory:
    factory { AuthViewModel(get(), get(), get(), get(), get()) }
    factory { HomeViewModel(get(), get(), get()) }
    factory { ItemListViewModel(get(), get(), get(), get()) }
    factory { ItemDetailViewModel(get(), get(), get()) }
    factory { ItemFormViewModel(get(), get(), get(), get(), get()) }
    factory { CategoryListViewModel(get(), get(), get(), get()) }
    factory { CategoryFormViewModel(get(), get(), get()) }
    factory { ProfileViewModel(get(), get(), get(), get()) }
}
```
> Sesuaikan jumlah `get()` dengan konstruktor final tiap ViewModel. Bila pakai Koin ViewModel DSL (`viewModelOf(::AuthViewModel)`), pastikan dependency `koin-compose-viewmodel`/`koin-androidx-compose` tersedia — **cek dulu, kalau belum ada STOP & tanya** (§0).

### `di/InitKoin.kt` — TAMBAH module
```kotlin
fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) = startKoin {
    appDeclaration()
    modules(dataModule, domainModule, presentationModule)   // ⬅ tambah presentationModule
}
```

---

## 6️⃣ AUTH & SESSION — kontrak domain + flow

### `domain/model/AuthUser.kt`
```kotlin
package com.project.tickr.domain.model

data class AuthUser(
    val id: String,
    val email: String?
)
```

### `domain/repository/AuthRepository.kt`
```kotlin
package com.project.tickr.domain.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String): DataResult<AuthUser>
    suspend fun signIn(email: String, password: String): DataResult<AuthUser>
    suspend fun signOut(): DataResult<Unit>
    /** Reactive — emit user saat ini atau null saat signed out / session expired. */
    fun observeSession(): Flow<AuthUser?>
    /** Snapshot sinkron (untuk userId di ViewModel lain). */
    fun currentUserId(): String?
}
```

### UseCase auth (`domain/usecase/auth/`)
| UseCase | invoke | Aturan |
|---|---|---|
| `SignUpUseCase` | `(email, password)` → `DataResult<AuthUser>` | `requireNotBlank(email)`, validasi format email, `password` min 6 char → `AppError.Validation` |
| `SignInUseCase` | `(email, password)` → `DataResult<AuthUser>` | `requireNotBlank` keduanya |
| `SignOutUseCase` | `()` → `DataResult<Unit>` | passthrough |
| `ObserveSessionUseCase` | `()` → `Flow<AuthUser?>` | passthrough reactive |
| `GetCurrentUserIdUseCase` | `()` → `String?` | passthrough; **menggantikan** asumsi Phase 2 "userId diteruskan dari pemanggil" |

> Tambah validator baru bila perlu: `Validators.requireEmail(value, field)` (regex sederhana) & `Validators.requireMinLength(value, min, field)`. Letakkan di `core/validation/Validators.kt` mengikuti pola Phase 2 (return `AppError.Validation?`).

---

## 7️⃣ PENYEMPURNAAN DATA & DOMAIN LAYER (yang kurang — kerjakan terukur)

Phase 1/2 belum mencakup Auth & Storage. Lengkapi **mengikuti pola Phase 1** (datasource → repository impl → map ke domain/`AppError`), **tanpa** mengubah arsitektur inti.

1. **Aktifkan plugin Auth (GoTrue)** di `SupabaseProvider` — `install(Auth)`. **STOP & tanya** bila plugin/config belum ada. Jangan ubah Postgrest/engine existing, hanya `install(Auth)` (+ `install(Storage)` bila §7.5 dikerjakan).
2. **`data/remote/auth/AuthRemoteDataSource.kt`** — bungkus `client.auth`: `signUpWith(Email)`, `signInWith(Email)`, `signOut()`, ekspos `sessionStatus`/`currentUserOrNull()`. Map error via pola `BaseRemoteDataSource.toAppError()` yang ada (jangan ubah fungsinya; panggil saja). Auth gagal kredensial → `AppError.Unauthorized`.
3. **`data/repository/AuthRepositoryImpl.kt`** — implement `AuthRepository`; map GoTrue `UserInfo` → `AuthUser` (`toDomain()`), `sessionStatus: Flow<SessionStatus>` → `Flow<AuthUser?>`. Return `DataResult` (tidak throw).
4. **Session restore** — pastikan supabase-kt Auth meng-`loadFromStorage()`/auto-refresh saat init (default `SettingsSessionManager`). **VERIFY** sesi persist antar app restart. Bila butuh `multiplatform-settings` (§0.3 Opsi A) → tambah dependency setelah konfirmasi.
5. **(Opsional — §0/desain) Storage untuk image item** — `install(Storage)`, `StorageRemoteDataSource.upload(bucket, path, bytes): DataResult<String /*publicUrl*/>`, dan `UploadItemImageUseCase`. Hanya kerjakan bila form item butuh upload gambar **dan** user setuju. Bila ditunda, `imageUrl` tetap diisi manual/null.
6. **Daftarkan ke DI**: tambah `AuthRemoteDataSource` + `AuthRepositoryImpl` (+ Storage) di `DataModule.kt`; tambah auth UseCase (+ `UploadItemImageUseCase`) di `DomainModule.kt`.
7. **`ErrorMessageProvider` exhaustiveness** — tetap exhaustive; tidak ada varian `AppError` baru yang diperlukan (auth error → `Unauthorized`/`Network`/`Unknown`/`Validation` yang sudah ada).
8. **Konsistensi `userId`** — setelah `GetCurrentUserIdUseCase` ada, ViewModel **tidak** lagi menerima `userId` dari luar; ambil dari UseCase. Pastikan tidak ada hard-code userId.

> ⛔ **JANGAN**: ubah DTO/mapper entity existing, ubah `toAppError()`, ubah engine/Chucker, ubah `BuildKonfig`, atau menambah RLS. Itu di luar scope.

---

## 8️⃣ (CATATAN) Wiring ke entry point & Screens — Phase 3.5

- **Screens (Compose/SwiftUI), Theme, warna, komponen, ikon, layout** → **Phase 3.5** setelah user mengirim desain. Phase ini menyiapkan semua kontrak (UiState/Action/Event/ViewModel/Destination/Navigator) agar Screens tinggal: `collectAsState()` → render → kirim `onAction(...)` → handle `events` (navigate/snackbar).
- **Koin init di entry point** (Android `Application` / iOS) hanya disentuh bila user minta; default cukup `initKoin()` siap dengan 3 module.
- **Navigator impl** (NavController / SwiftUI) menyusul di Phase 3.5 bersama Screens.

---

## ✅ ACCEPTANCE CRITERIA (UI Logic Layer)

- [ ] Tiap fitur punya **UiState (immutable)** + **Action (sealed)** + **Event (sealed, one-shot)** + **ViewModel** terpisah, pola seragam (`onAction` tunggal).
- [ ] **ViewModel TIDAK** mengimpor `data.*`, Compose, SwiftUI, atau `android.*` (selain `androidx.lifecycle.ViewModel`).
- [ ] Semua UseCase dipanggil dalam `viewModelScope.launch`, hasil `DataResult` ditangani `onSuccess/onError`, error dipetakan `ErrorMessageProvider.provide(...)`.
- [ ] Navigasi & snackbar lewat **UiEvent** (Channel/SharedFlow), **bukan** disimpan di StateFlow.
- [ ] **Auth flow** lengkap secara logika: `AuthRepository` + 5 UseCase + `AuthViewModel`; `GetCurrentUserIdUseCase` menggantikan passing `userId` manual.
- [ ] **Session restore** terverifikasi (sesi bertahan antar restart) sesuai §0.3.
- [ ] **Navigation contract** (`Destination` sealed type-safe + `Navigator`) ada di `commonMain`; tidak ada impl platform di phase ini.
- [ ] `presentationModule` memuat seluruh ViewModel; `initKoin()` memuat `dataModule + domainModule + presentationModule` tanpa missing dependency.
- [ ] Penyempurnaan §7 selesai: `install(Auth)`, `AuthRemoteDataSource`, `AuthRepositoryImpl`, DI terdaftar; Storage hanya bila disetujui.
- [ ] **TIDAK** ada Compose Screen / Theme / warna / komponen visual dibuat (ditunda Phase 3.5).
- [ ] **TIDAK** ada dependency baru tanpa konfirmasi (§0); config Supabase existing (Postgrest/engine) tidak diubah selain `install(Auth)`/`install(Storage)`.
- [ ] Compile sukses: `:shared:compileCommonMainKotlinMetadata`, `:shared:compileAndroidMain`, `:shared:compileKotlinIosSimulatorArm64`.

---

## 🔢 EXECUTION ORDER (verify compile tiap step)

1. **Keputusan §0** (Compose target, Navigation, Session storage, Auth scope). **Tunggu jawaban** (pakai default bila kosong). Konfirmasi dependency yang belum ada.
2. **Data/Domain auth (§6, §7.1–7.4)**: `install(Auth)` → `AuthRemoteDataSource` → `AuthRepositoryImpl` → `AuthUser` model → `AuthRepository` interface. Tambah validator email/min-length. → compile.
3. **UseCase auth** (5 file) + register di `DomainModule`. → compile.
4. **Presentation common** (`UiText`, MVI base) + **Navigation** (`Destination`, `Navigator`). → compile.
5. **AuthViewModel** + UiState/Action/Event. → compile.
6. **Home ViewModel** (pakai `GetExpiringItemsUseCase` + `GetCurrentUserIdUseCase`). → compile.
7. **Item** list / detail / form (UiState/Action/Event/ViewModel). → compile.
8. **Category** list / form. → compile.
9. **Profile** ViewModel. → compile.
10. **DI**: `PresentationModule.kt` + tambah ke `initKoin()`. → compile (commonMain + androidMain metadata).
11. **(Opsional §7.5)** Storage + `UploadItemImageUseCase` bila disetujui. → compile.
12. **Final**: verify acceptance criteria + compile ketiga target. Tulis ringkasan keputusan §0 yang diambil.

> Setelah phase ini, **Phase 3.5 (Screens)** tinggal: terima desain/tema/warna/fitur dari user → bangun Compose/SwiftUI yang meng-`collect` `state`, mengirim `onAction(...)`, dan menangani `events` (Navigator + Snackbar). Tidak perlu menyentuh ViewModel/Domain/Data lagi.

---

**END OF UI / PRESENTATION LAYER SPEC (Phase 3 — Screens excluded)**
