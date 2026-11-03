# 🧠 TICKR — DOMAIN / USECASE LAYER Implementation Spec (Phase 2)

> **Target Executor**: Claude Code CLI
> **Scope**: **DOMAIN / USECASE LAYER ONLY** — domain validation, UseCases per entity, date/time domain utilities, error→message contract, dan penyempurnaan kecil pada data layer yang masih kurang.
> **Package**: `com.project.tickr`
> **Backend**: Supabase (PostgreSQL) via `supabase-kt` (Ktor) — *sudah selesai di Phase 1*
> **Prasyarat**: Data Layer Phase 1 sudah **Complete** (lihat `DATA_LAYER_PHASE1_DOCUMENTATION.md`)

---

## ✅ PRECONDITIONS (sudah selesai — JANGAN diulang)

- [x] **Data Layer Phase 1 complete**: DTO, BaseRemoteDataSource, RemoteDataSource (4 entity), Mapper, RepositoryImpl, `DataModule` (Koin).
- [x] `core/result/DataResult.kt` (+ `mapData`) & `core/result/AppError.kt` sudah ada.
- [x] Domain **model** (pure Kotlin) & **repository interface** untuk 4 entity sudah ada (`Profile`, `Category`, `Item`, `NotificationLog`).
- [x] `SupabaseProvider` tunggal + Ktor Logging + Chucker (Android) sudah jalan.
- [x] Dependencies (supabase-kt, ktor, koin, kotlinx-serialization, chucker) sudah di-add & sync.

> **JANGAN** menambah dependency baru **kecuali** `kotlinx-datetime` (lihat bagian §0 — butuh konfirmasi user dulu). Jika butuh sesuatu yang lain, **STOP dan tanya**.
> **JANGAN** mengubah config Supabase/`BuildKonfig`/engine network yang sudah jadi.

---

## 🎯 SCOPE & NON-GOALS

### IN SCOPE (kerjakan sekarang)
1. **UseCase layer** per entity — satu UseCase per operasi bisnis (single-responsibility, `operator fun invoke`).
2. **Domain validation** — aturan bisnis (nama tidak kosong, expiry_date valid, dll) lewat `Validator` + `ValidationException`/`AppError.Validation`.
3. **Domain date utilities** — parsing/format `String` ⇄ `LocalDate`/`Instant` terpusat (timestamps masih `String` di model — lihat Phase 1 §Data Handling).
4. **Error message contract** — pemetaan `AppError` → pesan user-friendly (string resource-agnostic) agar UI layer mudah konsumsi.
5. **UseCase DI module** — `domain/di/DomainModule.kt` (Koin) berisi semua UseCase.
6. **Penyempurnaan Data Layer yang kurang** (lihat §7) — TANPA mengubah desain inti.

### OUT OF SCOPE (JANGAN dikerjakan)
- ❌ **UI / Presentation layer** — ViewModel, UiState, StateFlow, Compose Screen, Navigation, Theme. **JANGAN sentuh sama sekali.**
- ❌ Auth flow lengkap (login/logout UI). *Boleh* buat `SessionUseCase` thin wrapper hanya jika dibutuhkan UseCase lain (lihat §3.E — opsional, konfirmasi dulu).
- ❌ Image upload / Storage (`install(Storage)`) → phase berikutnya.
- ❌ Local caching / offline DB (Room/SQLDelight) → phase berikutnya.

---

## 🗂️ TARGET STRUCTURE (Domain Layer Phase)

```
commonMain/kotlin/com/project/tickr/
│
├── core/
│   ├── result/
│   │   ├── DataResult.kt                # (exists) — TAMBAH helper: flatMap, getOrNull, onSuccess/onError
│   │   └── AppError.kt                  # (exists) — TAMBAH: AppError.Validation(field, reason)
│   ├── util/
│   │   └── DateTimeUtil.kt              # ⭐ NEW — parsing/format String ⇄ LocalDate/Instant
│   └── validation/
│       ├── ValidationResult.kt          # NEW — Valid / Invalid(reason)
│       └── Validators.kt                # NEW — reusable field validators
│
├── domain/                              # ⭐ FOKUS PHASE INI
│   ├── model/                           # (exists — JANGAN ubah signature; boleh tambah computed bila perlu)
│   │   ├── Profile.kt
│   │   ├── Category.kt
│   │   ├── Item.kt
│   │   └── NotificationLog.kt
│   ├── repository/                      # (exists — JANGAN ubah; ini kontrak ke data layer)
│   │   ├── ProfileRepository.kt
│   │   ├── CategoryRepository.kt
│   │   ├── ItemRepository.kt
│   │   └── NotificationRepository.kt
│   │
│   ├── error/
│   │   └── ErrorMessageProvider.kt      # NEW — AppError -> user-friendly message (no UI import)
│   │
│   ├── usecase/                         # ⭐ INTI PHASE INI — 1 file per UseCase
│   │   ├── profile/
│   │   │   ├── GetProfileUseCase.kt
│   │   │   └── UpsertProfileUseCase.kt
│   │   ├── category/
│   │   │   ├── GetCategoriesUseCase.kt
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
│   │   │   ├── MarkItemConsumedUseCase.kt        # business rule: toggle isConsumed
│   │   │   └── GetExpiringItemsUseCase.kt        # business rule: filter by expiry window
│   │   └── notification/
│   │       ├── GetNotificationsUseCase.kt
│   │       ├── GetNotificationsByItemUseCase.kt
│   │       ├── ScheduleNotificationUseCase.kt
│   │       └── DeleteNotificationUseCase.kt
│   │
│   └── di/
│       └── DomainModule.kt              # NEW — Koin module untuk semua UseCase
│
└── data/                                # ⚠️ HANYA penyempurnaan kecil (lihat §7)
    └── di/DataModule.kt                 # (exists) — tetap; DomainModule akan depend padanya
```

> Semua kode di `commonMain`. **TIDAK** ada perubahan di `androidMain`/`iosMain`/`androidApp` pada phase ini (kecuali catatan opsional Koin init di §8 — hanya jika user minta).

---

## 0️⃣ KEPUTUSAN: `kotlinx-datetime` (KONFIRMASI DULU)

Phase 1 menyimpan semua tanggal/timestamp sebagai `String`. UseCase seperti `GetExpiringItemsUseCase` butuh membandingkan tanggal. Dua opsi:

- **Opsi A (disarankan)**: tambah dependency `org.jetbrains.kotlinx:kotlinx-datetime` → parsing aman & cross-platform.
- **Opsi B (zero-dependency)**: bandingkan `expiry_date` sebagai `String` ISO-8601 (`yyyy-MM-dd`) secara leksikografis (valid untuk format ini), tanpa lib tambahan.

> ⛔ **STOP & TANYA user**: "Pakai `kotlinx-datetime` (Opsi A) atau perbandingan String ISO (Opsi B)?"
> Default bila tidak dijawab: **Opsi B** (tidak menambah dependency, sesuai aturan). `DateTimeUtil` tetap dibuat sebagai abstraksi agar mudah migrasi ke Opsi A nanti.

---

## ⭐ BAGIAN INTI: POLA USECASE

### Konsep
Setiap UseCase = **satu aksi bisnis**, dipanggil via `operator fun invoke(...)`, mengembalikan `DataResult<T>` (mengalir dari repository). UseCase **tidak** tahu apa-apa soal UI/Android. Validasi & aturan bisnis hidup di sini, **bukan** di repository/datasource.

```
UI(ViewModel)  ──>  UseCase  ──>  Repository(interface)  ──>  RemoteDataSource  ──>  Supabase
                     ▲ validasi & business rule di sini
```

### Aturan wajib
- Konstruktor menerima **repository interface** (bukan impl), di-inject Koin.
- Return type **selalu** `DataResult<…>` — jangan lempar exception ke pemanggil; validasi gagal → `DataResult.Error(AppError.Validation(...))`.
- **Tidak** ada import dari `data.*` (DTO/datasource/impl) maupun framework UI/Android.
- Satu file = satu UseCase = satu `class`.

### Template UseCase (read)
```kotlin
package com.project.tickr.domain.usecase.item

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.repository.ItemRepository

class GetItemsByUserUseCase(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(userId: String): DataResult<List<Item>> =
        repository.getItemsByUser(userId)
}
```

### Template UseCase (write + validation)
```kotlin
package com.project.tickr.domain.usecase.item

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.core.validation.Validators
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.repository.ItemRepository

class CreateItemUseCase(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(item: Item): DataResult<Item> {
        // 1) Validasi domain dulu — gagal = jangan hit network
        Validators.requireNotBlank(item.name, field = "name")?.let {
            return DataResult.Error(it)
        }
        Validators.requireIsoDate(item.expiryDate, field = "expiry_date")?.let {
            return DataResult.Error(it)
        }
        // 2) Delegasi ke repository
        return repository.createItem(item)
    }
}
```

### Template UseCase (business rule — derived data)
```kotlin
package com.project.tickr.domain.usecase.item

import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.mapData
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.repository.ItemRepository

/** Item yang kadaluarsa dalam [withinDays] hari ke depan & belum dikonsumsi. */
class GetExpiringItemsUseCase(
    private val repository: ItemRepository,
    private val dateTime: DateTimeUtil
) {
    suspend operator fun invoke(
        userId: String,
        withinDays: Int = 7
    ): DataResult<List<Item>> =
        repository.getItemsByUser(userId).mapData { items ->
            items.filter { !it.isConsumed && dateTime.isWithinDays(it.expiryDate, withinDays) }
                 .sortedBy { it.expiryDate }
        }
}
```

---

## 1️⃣ CORE — penyempurnaan `DataResult` & `AppError`

### `core/result/DataResult.kt` — TAMBAH helper (jangan hapus yang ada)
```kotlin
inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(data)
    return this
}

inline fun <T> DataResult<T>.onError(action: (AppError) -> Unit): DataResult<T> {
    if (this is DataResult.Error) action(error)
    return this
}

fun <T> DataResult<T>.getOrNull(): T? =
    (this as? DataResult.Success)?.data

inline fun <T, R> DataResult<T>.flatMap(transform: (T) -> DataResult<R>): DataResult<R> =
    when (this) {
        is DataResult.Success -> transform(data)
        is DataResult.Error -> this
    }
```

### `core/result/AppError.kt` — TAMBAH satu varian
```kotlin
// tambahkan ke sealed class AppError:
data class Validation(val field: String, val reason: String)
    : AppError("Validation failed on '$field': $reason")
```
> Tambahkan handling `Validation` di mana pun `when(AppError)` exhaustif (mis. `ErrorMessageProvider`). **JANGAN** sentuh `toAppError()` di `BaseRemoteDataSource` (Validation tidak berasal dari network).

---

## 2️⃣ CORE — validation

### `core/validation/ValidationResult.kt`
```kotlin
package com.project.tickr.core.validation

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val field: String, val reason: String) : ValidationResult
}
```

### `core/validation/Validators.kt`
```kotlin
package com.project.tickr.core.validation

import com.project.tickr.core.result.AppError

/**
 * Reusable validators. Mengembalikan AppError.Validation? — null artinya valid.
 * Pola: `Validators.requireNotBlank(x, "name")?.let { return DataResult.Error(it) }`
 */
object Validators {

    fun requireNotBlank(value: String?, field: String): AppError.Validation? =
        if (value.isNullOrBlank()) AppError.Validation(field, "must not be empty") else null

    fun requireMaxLength(value: String?, max: Int, field: String): AppError.Validation? =
        if ((value?.length ?: 0) > max) AppError.Validation(field, "max $max chars") else null

    /** ISO-8601 date yyyy-MM-dd (Opsi B-safe). */
    fun requireIsoDate(value: String?, field: String): AppError.Validation? {
        val regex = Regex("""\d{4}-\d{2}-\d{2}""")
        return if (value == null || !regex.matches(value))
            AppError.Validation(field, "must be yyyy-MM-dd") else null
    }

    fun requireHexColor(value: String?, field: String): AppError.Validation? {
        val regex = Regex("""#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})""")
        return if (value == null || !regex.matches(value))
            AppError.Validation(field, "must be #RRGGBB or #AARRGGBB") else null
    }
}
```

---

## 3️⃣ CORE — `DateTimeUtil` (abstraksi, default Opsi B)

### `core/util/DateTimeUtil.kt`
```kotlin
package com.project.tickr.core.util

/**
 * Abstraksi date/time domain. Default impl (Opsi B) memakai perbandingan String ISO
 * tanpa dependency. Jika user pilih Opsi A, ganti isi method pakai kotlinx-datetime —
 * signature tetap, UseCase tidak berubah.
 */
interface DateTimeUtil {
    /** true jika [isoDate] (yyyy-MM-dd) jatuh antara hari ini dan [days] hari ke depan (inklusif). */
    fun isWithinDays(isoDate: String, days: Int): Boolean

    /** true jika [isoDate] sudah lewat (< hari ini). */
    fun isPast(isoDate: String): Boolean

    /** Hari ini dalam format yyyy-MM-dd. */
    fun today(): String
}
```

> Implementasi konkret (`DateTimeUtilImpl`) ditaruh di `core/util/DateTimeUtilImpl.kt`:
> - **Opsi B**: pakai perbandingan `String` ISO + hitung selisih hari sederhana. Untuk `today()` di KMP tanpa lib, gunakan `kotlin.time`/`expect-actual` minimal **atau** terima `today` sebagai parameter dari pemanggil. ⛔ **Jika `today()` butuh lib, STOP & tanya** (kembali ke keputusan §0).
> - **Opsi A**: `kotlinx.datetime.Clock.System.todayIn(TimeZone.currentSystemDefault())`, `LocalDate.parse(...)`, `daysUntil(...)`.

> ⚠️ **CHECKPOINT**: Sebelum tulis `DateTimeUtilImpl`, konfirmasikan Opsi A/B (§0). Ini satu-satunya bagian yang menyentuh keputusan dependency.

---

## 4️⃣ DOMAIN — `ErrorMessageProvider`

### `domain/error/ErrorMessageProvider.kt`
```kotlin
package com.project.tickr.domain.error

import com.project.tickr.core.result.AppError

/**
 * Memetakan AppError -> pesan siap-tampil. TIDAK mengimpor framework UI/Android,
 * sehingga aman dipakai lintas platform. UI layer cukup memanggil provide(error).
 */
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
> Sesuaikan `when` agar **exhaustive** dengan varian `AppError` aktual (termasuk `Validation` baru). Bahasa pesan boleh disesuaikan.

---

## 5️⃣ DOMAIN — USECASE per entity

> Pakai signature repository **yang sudah ada di Phase 1**. Jangan mengarang nama method — buka tiap interface dulu dan cocokkan. Daftar method Phase 1:
>
> - `ProfileRepository`: `getProfile(userId)`, `upsertProfile(profile)`
> - `CategoryRepository`: `getCategories()`, `getCategoriesByUser(userId)`, `getCategory(id)`, `createCategory(c)`, `updateCategory(id, c)`, `deleteCategory(id)`
> - `ItemRepository`: (cek file — pola sama seperti Category, berbasis `Long` id & `userId: String`)
> - `NotificationRepository`: `getNotifications()`/`getAll`, `getByItemId(itemId)`, `getById(id)`, `insert/schedule`, `delete(id)` — **VERIFY nama aktual di interface**.

### A. Profile
| UseCase | invoke | Aturan |
|---|---|---|
| `GetProfileUseCase` | `(userId: String)` → `DataResult<Profile>` | passthrough |
| `UpsertProfileUseCase` | `(profile: Profile)` → `DataResult<Profile>` | `requireMaxLength(fullName, 100, "full_name")` |

### B. Category
| UseCase | invoke | Aturan |
|---|---|---|
| `GetCategoriesUseCase` | `()` | passthrough |
| `GetCategoriesByUserUseCase` | `(userId)` | passthrough |
| `GetCategoryUseCase` | `(id: Long)` | passthrough |
| `CreateCategoryUseCase` | `(category)` | `requireNotBlank(name)`, `requireNotBlank(iconName)`, `requireHexColor(colorHex)` |
| `UpdateCategoryUseCase` | `(id, category)` | sama seperti create |
| `DeleteCategoryUseCase` | `(id: Long)` → `DataResult<Unit>` | passthrough |

### C. Item
| UseCase | invoke | Aturan |
|---|---|---|
| `GetItemsUseCase` | `()` | passthrough |
| `GetItemsByUserUseCase` | `(userId)` | passthrough |
| `GetItemUseCase` | `(id: Long)` | passthrough |
| `CreateItemUseCase` | `(item)` | `requireNotBlank(name)`, `requireIsoDate(expiryDate)` |
| `UpdateItemUseCase` | `(id, item)` | sama seperti create |
| `DeleteItemUseCase` | `(id: Long)` → `DataResult<Unit>` | passthrough |
| `MarkItemConsumedUseCase` | `(item, consumed: Boolean)` | set `isConsumed`, delegasi `updateItem` (immutability: `item.copy(...)`) |
| `GetExpiringItemsUseCase` | `(userId, withinDays = 7)` | filter `!isConsumed && isWithinDays`, sort by `expiryDate` |

### D. Notification
| UseCase | invoke | Aturan |
|---|---|---|
| `GetNotificationsUseCase` | `()` | passthrough |
| `GetNotificationsByItemUseCase` | `(itemId: Long)` | passthrough |
| `ScheduleNotificationUseCase` | `(log: NotificationLog)` | `requireIsoDate(scheduledDate)`; tolak jadwal di masa lalu via `dateTime.isPast(...)` → `AppError.Validation` |
| `DeleteNotificationUseCase` | `(id: Long)` → `DataResult<Unit>` | passthrough |

### E. (OPSIONAL) Session — **konfirmasi dulu**
Jika UseCase butuh `userId` saat ini (mis. `GetExpiringItemsUseCase`), pertimbangkan `GetCurrentUserIdUseCase` membungkus `SupabaseProvider.client.auth`. **STOP & tanya** apakah perlu sekarang; bila tidak, `userId` diteruskan dari pemanggil (UI layer nanti).

---

## 6️⃣ DI — `domain/di/DomainModule.kt`

```kotlin
package com.project.tickr.domain.di

import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.core.util.DateTimeUtilImpl
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.category.*
import com.project.tickr.domain.usecase.item.*
import com.project.tickr.domain.usecase.notification.*
import com.project.tickr.domain.usecase.profile.*
import org.koin.dsl.module

val domainModule = module {
    // core helpers
    single<DateTimeUtil> { DateTimeUtilImpl() }
    single { ErrorMessageProvider() }

    // profile
    factory { GetProfileUseCase(get()) }
    factory { UpsertProfileUseCase(get()) }

    // category
    factory { GetCategoriesUseCase(get()) }
    factory { GetCategoriesByUserUseCase(get()) }
    factory { GetCategoryUseCase(get()) }
    factory { CreateCategoryUseCase(get()) }
    factory { UpdateCategoryUseCase(get()) }
    factory { DeleteCategoryUseCase(get()) }

    // item
    factory { GetItemsUseCase(get()) }
    factory { GetItemsByUserUseCase(get()) }
    factory { GetItemUseCase(get()) }
    factory { CreateItemUseCase(get()) }
    factory { UpdateItemUseCase(get()) }
    factory { DeleteItemUseCase(get()) }
    factory { MarkItemConsumedUseCase(get()) }
    factory { GetExpiringItemsUseCase(get(), get()) }   // repo + DateTimeUtil

    // notification
    factory { GetNotificationsUseCase(get()) }
    factory { GetNotificationsByItemUseCase(get()) }
    factory { ScheduleNotificationUseCase(get(), get()) }
    factory { DeleteNotificationUseCase(get()) }
}
```
> UseCase pakai `factory` (stateless, murah dibuat). `DateTimeUtil`/`ErrorMessageProvider` `single`.
> `domainModule` bergantung pada repository dari `dataModule` → keduanya harus dimuat bersama saat `startKoin`.

---

## 7️⃣ PENYEMPURNAAN DATA LAYER (yang kurang — kerjakan ringan)

Berdasarkan audit Phase 1, lengkapi item berikut **tanpa** mengubah arsitektur:

1. **Pending Task #1 Phase 1 — Koin belum di-init.** Siapkan helper `initKoin()` di commonMain (`di/InitKoin.kt`) yang memuat `dataModule + domainModule`. **JANGAN** panggil di entry point Android/iOS kecuali user minta (lihat §8). Contoh:
   ```kotlin
   package com.project.tickr.di
   import com.project.tickr.data.di.dataModule
   import com.project.tickr.domain.di.domainModule
   import org.koin.core.context.startKoin
   import org.koin.core.KoinApplication

   fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) = startKoin {
       appDeclaration()
       modules(dataModule, domainModule)
   }
   ```
2. **Konsistensi method `getByUser` vs `getByUserId`.** Phase 1 doc menyebut datasource `getByUserId()` tapi repository `getItemsByUser()`/`getCategoriesByUser()`. **VERIFY** nama aktual di kode; selaraskan penamaan agar UseCase tidak salah panggil. Catat ke user bila ada mismatch.
3. **Validasi `NotificationRepository` interface.** Phase 1 doc tidak menampilkan signature lengkapnya. **Buka file**, pastikan ada `getByItemId`, `insert`/`schedule`, `delete`. Jika method yang dibutuhkan UseCase belum ada, **tambahkan ke interface + impl + datasource** mengikuti pola Phase 1 (mapper `.mapData { it.toDomain() }`).
4. **`AppError` exhaustiveness.** Setelah menambah `AppError.Validation`, pastikan semua `when(error)` di codebase tetap compile (tidak ada yang `else`-only yang menyembunyikan cabang baru).
5. **(Tidak wajib)** Tambah KDoc singkat pada repository interface yang masih kosong dokumentasinya.

> ⛔ **JANGAN**: ubah DTO, ubah `BaseRemoteDataSource.toAppError()`, ubah `SupabaseProvider`, ubah engine/Chucker, atau menambah RLS. Itu di luar scope phase ini.

---

## 8️⃣ (OPSIONAL) Koin init di entry point — HANYA jika user minta

Jika user ingin sekalian mengaktifkan Koin (Pending Task #1 Phase 1):
- **Android**: di `TickrApplication.onCreate()` → `initKoin { androidContext(this@TickrApplication) }` (butuh `koin-android`; **cek apakah sudah ada**, kalau belum **STOP & tanya**).
- **iOS**: expose `fun doInitKoin() = initKoin()` untuk dipanggil dari Swift.

> Default: **JANGAN** sentuh entry point. Cukup sediakan `initKoin()` agar siap dipakai phase UI.

---

## ✅ ACCEPTANCE CRITERIA (Domain Layer)

- [ ] Semua UseCase = file terpisah, `operator fun invoke`, konstruktor menerima **repository interface**.
- [ ] **TIDAK** ada import `data.*` (DTO/datasource/impl) atau framework UI/Android di dalam `domain/usecase/**`.
- [ ] Semua UseCase mengembalikan `DataResult<…>`; validasi gagal → `DataResult.Error(AppError.Validation(...))`, **tanpa** throw.
- [ ] `Validators` & `ValidationResult` reusable; aturan bisnis di tabel §5 terpasang.
- [ ] `DateTimeUtil` interface + impl sesuai Opsi A/B yang dikonfirmasi; `GetExpiringItemsUseCase` & `ScheduleNotificationUseCase` memakainya.
- [ ] `ErrorMessageProvider.provide(...)` exhaustive terhadap semua `AppError` (termasuk `Validation`).
- [ ] `domainModule` Koin memuat seluruh UseCase + helper; resolve bersama `dataModule` tanpa missing dependency.
- [ ] Penyempurnaan data layer §7 selesai (terutama `initKoin()` siap & penamaan `getByUser*` konsisten).
- [ ] **TIDAK** ada UI/Presentation layer yang dibuat.
- [ ] **TIDAK** ada dependency baru kecuali (bila disetujui) `kotlinx-datetime`; config Supabase/engine tidak diubah.
- [ ] Compile sukses: `:shared:compileCommonMainKotlinMetadata`, `:shared:compileAndroidMain`, `:shared:compileKotlinIosSimulatorArm64`.

---

## 🔢 EXECUTION ORDER (verify compile tiap step)

1. **Keputusan §0**: konfirmasi `kotlinx-datetime` (A) atau String-ISO (B). **Tunggu jawaban** (default B).
2. **Core result**: tambah helper `flatMap/getOrNull/onSuccess/onError` ke `DataResult.kt`; tambah `AppError.Validation`. → compile commonMain.
3. **Core validation**: `ValidationResult.kt`, `Validators.kt`. → compile.
4. **Core util**: `DateTimeUtil.kt` (interface) → `DateTimeUtilImpl.kt` (sesuai §0). → compile.
5. **CHECKPOINT**: buka & **verifikasi signature** keempat repository interface (terutama `NotificationRepository` & penamaan `getByUser*`). Catat mismatch ke user, selaraskan (§7.2/§7.3).
6. **Domain error**: `ErrorMessageProvider.kt` (exhaustive). → compile.
7. **UseCase Profile** (2 file) → compile.
8. **UseCase Category** (6 file) → compile.
9. **UseCase Item** (8 file, termasuk business rule) → compile.
10. **UseCase Notification** (4 file) → compile.
11. **DI**: `DomainModule.kt` + `di/InitKoin.kt`. → compile (commonMain + androidMain metadata).
12. **Penyempurnaan data layer** §7 sisa (verifikasi exhaustiveness, KDoc).
13. **Final**: verify acceptance criteria + compile ketiga target.

> Setelah phase ini, UI layer cukup: inject UseCase via Koin → panggil `invoke()` → tangani `DataResult` (`onSuccess/onError`) → render `ErrorMessageProvider.provide(error)`. Tidak perlu menyentuh data layer sama sekali.

---

**END OF DOMAIN / USECASE LAYER SPEC**
