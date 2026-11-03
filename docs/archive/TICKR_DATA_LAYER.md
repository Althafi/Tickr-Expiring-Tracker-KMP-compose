# 🗄️ TICKR — DATA LAYER Implementation Spec (Phase 1)

> **Target Executor**: Claude Code CLI
> **Scope**: **DATA LAYER ONLY** — base API, datasources, DTOs, mappers, repository implementations + **Chucker** API logging
> **Package**: `com.project.tickr`
> **Backend**: Supabase (PostgreSQL) via `supabase-kt` (Ktor)

---

## ✅ PRECONDITIONS (sudah selesai — JANGAN diulang)

- [x] Dependencies sudah di-add & sync manual (supabase-kt, ktor, kotlinx-serialization, koin, chucker, dll).
- [x] `SUPABASE_URL` & `SUPABASE_ANON_KEY` sudah di `local.properties` + `BuildKonfig`. **JANGAN hardcode, JANGAN ubah config ini.**
- [x] Struktur dasar sudah ada: `App.kt`, `SupabaseClient.kt`, `composeResources/drawable`.

> **JANGAN** menambah dependency baru. Jika butuh sesuatu yang belum ada, **STOP dan tanya** dulu.

---

## 🎯 SCOPE & NON-GOALS

### IN SCOPE (kerjakan sekarang)
1. **Base API layer** terpisah & reusable (`core/network`) — engine config per-platform + Chucker.
2. **Chucker** integration untuk inspeksi API log in-app (Android).
3. **Ktor Logging plugin** untuk fallback logging cross-platform (termasuk iOS).
4. **DTO** untuk 4 table: `profiles`, `items`, `categories`, `notifications_log`.
5. **RemoteDataSource** per-entity dengan **BaseRemoteDataSource** (shared error handling).
6. **Mapper** DTO ↔ Domain Model.
7. **Repository implementation** untuk tiap entity.
8. **Domain contracts minimal** (model + repository interface) — HANYA yang dibutuhkan data layer agar compile.

### OUT OF SCOPE (JANGAN dikerjakan dulu)
- ❌ UseCase / business logic (domain layer penuh) → phase berikutnya.
- ❌ ViewModel / UiState / Screen (UI layer) → phase berikutnya.
- ❌ Koin module wiring penuh (cukup buat 1 `dataModule` minimal untuk data layer).
- ❌ Navigation, Theme, Auth flow.

---

## 🗂️ TARGET STRUCTURE (Data Layer Phase)

```
commonMain/kotlin/com/project/tickr/
├── App.kt                                  # (exists — jangan diubah)
├── SupabaseClient.kt                       # (exists — REFACTOR jadi core/network, lihat catatan)
│
├── core/
│   ├── network/
│   │   ├── SupabaseProvider.kt             # Membuat SupabaseClient pakai engine per-platform
│   │   ├── HttpEngineFactory.kt            # expect fun — engine factory
│   │   └── KtorLogger.kt                   # Ktor Logging plugin config (cross-platform)
│   └── result/
│       ├── DataResult.kt                   # Sealed: Success / Error
│       └── AppError.kt                     # Sealed error types
│
├── data/
│   ├── remote/
│   │   ├── datasource/
│   │   │   ├── BaseRemoteDataSource.kt     # ⭐ BASE API — shared safe-call & error mapping
│   │   │   ├── ProfileRemoteDataSource.kt
│   │   │   ├── ItemRemoteDataSource.kt
│   │   │   ├── CategoryRemoteDataSource.kt
│   │   │   └── NotificationRemoteDataSource.kt
│   │   └── dto/
│   │       ├── ProfileDto.kt
│   │       ├── ItemDto.kt
│   │       ├── CategoryDto.kt
│   │       └── NotificationLogDto.kt
│   ├── mapper/
│   │   ├── ProfileMapper.kt
│   │   ├── ItemMapper.kt
│   │   ├── CategoryMapper.kt
│   │   └── NotificationMapper.kt
│   ├── repository/
│   │   ├── ProfileRepositoryImpl.kt
│   │   ├── ItemRepositoryImpl.kt
│   │   ├── CategoryRepositoryImpl.kt
│   │   └── NotificationRepositoryImpl.kt
│   └── di/
│       └── DataModule.kt                   # Koin module minimal (data layer saja)
│
└── domain/                                 # ⚠️ CONTRACTS MINIMAL (bukan domain penuh)
    ├── model/
    │   ├── Profile.kt
    │   ├── Item.kt
    │   ├── Category.kt
    │   └── NotificationLog.kt
    └── repository/
        ├── ProfileRepository.kt            # interface
        ├── ItemRepository.kt
        ├── CategoryRepository.kt
        └── NotificationRepository.kt

androidMain/kotlin/com/project/tickr/core/network/
└── HttpEngineFactory.android.kt            # actual — OkHttp + Chucker

iosMain/kotlin/com/project/tickr/core/network/
└── HttpEngineFactory.ios.kt                # actual — Darwin (tanpa Chucker)

androidMain/kotlin/com/project/tickr/core/network/
└── ChuckerContext.kt                       # holder Context untuk Chucker
```

---

## ⭐ BAGIAN INTI: BASE API + CHUCKER

### Konsep
`supabase-kt` membungkus Ktor. Agar Chucker (OkHttp interceptor, **Android-only**) bisa menangkap semua trafik Supabase, kita **inject engine OkHttp** yang sudah dipasangi `ChuckerInterceptor` ke `createSupabaseClient`. iOS pakai engine Darwin tanpa Chucker, tapi tetap dapat log via **Ktor Logging plugin**.

```
createSupabaseClient(url, key) {
    httpEngine = <platform engine>   // Android: OkHttp+Chucker | iOS: Darwin
    install(Postgrest); install(Auth)
    // Ktor Logging plugin via httpConfig (cross-platform)
}
```

> ⚠️ **VERIFY**: konfirmasi nama property builder supabase-kt untuk custom engine (kemungkinan `httpEngine`). Cek versi yang terpasang sebelum menulis. Kalau berbeda (mis. lewat `httpConfig`/`engine {}`), sesuaikan.

---

### 1. `core/network/HttpEngineFactory.kt` (commonMain — expect)
```kotlin
package com.project.tickr.core.network

import io.ktor.client.engine.HttpClientEngine

/** Engine factory per-platform. Android menyuntik Chucker, iOS pakai Darwin polos. */
expect fun provideHttpEngine(): HttpClientEngine
```

### 2. `androidMain/.../HttpEngineFactory.android.kt` (actual — OkHttp + Chucker)
```kotlin
package com.project.tickr.core.network

import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun provideHttpEngine(): HttpClientEngine = OkHttp.create {
    val context = ChuckerContext.applicationContext
        ?: error("ChuckerContext.applicationContext belum di-init di Application.onCreate()")
    addInterceptor(
        ChuckerInterceptor.Builder(context)
            .collector(com.chuckerteam.chucker.api.ChuckerCollector(context))
            .maxContentLength(250_000L)
            .alwaysReadResponseBody(true)
            .build()
    )
}
```

### 3. `androidMain/.../ChuckerContext.kt` (Context holder)
```kotlin
package com.project.tickr.core.network

import android.content.Context

/** Di-set sekali di Application.onCreate(): ChuckerContext.applicationContext = this */
object ChuckerContext {
    var applicationContext: Context? = null
}
```
> **ACTION (Android app module)**: di `Application.onCreate()` tambahkan
> `ChuckerContext.applicationContext = applicationContext`.
> (Kalau project sudah pakai Koin dengan `androidContext()`, boleh ambil dari Koin sebagai alternatif — tapi holder ini cukup & tanpa dependency tambahan.)

### 4. `iosMain/.../HttpEngineFactory.ios.kt` (actual — Darwin)
```kotlin
package com.project.tickr.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun provideHttpEngine(): HttpClientEngine = Darwin.create()
```

### 5. `core/network/SupabaseProvider.kt` (commonMain)
Refactor `SupabaseClient.kt` lama ke sini (atau jadikan ini sumber tunggal):
```kotlin
package com.project.tickr.core.network

import com.project.tickr.BuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel

object SupabaseProvider {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_ANON_KEY
        ) {
            httpEngine = provideHttpEngine()   // ⚠️ verify property name
            install(Postgrest)
            install(Auth)
            httpConfig {
                install(Logging) {
                    level = LogLevel.ALL        // turunkan ke HEADERS/NONE di release
                }
            }
        }
    }
}
```
> Jika `SupabaseClient.kt` lama berisi client lama, **migrasikan referensinya** ke `SupabaseProvider.client` lalu hapus duplikatnya. Jangan biarkan dua client.

---

## 🧱 CORE: RESULT & ERROR

### `core/result/DataResult.kt`
```kotlin
package com.project.tickr.core.result

sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val error: AppError) : DataResult<Nothing>
}
```

### `core/result/AppError.kt`
```kotlin
package com.project.tickr.core.result

sealed class AppError(open val message: String) {
    data object Network : AppError("Network unavailable")
    data object Unauthorized : AppError("Unauthorized / session invalid")
    data class NotFound(val entity: String) : AppError("$entity not found")
    data class Serialization(val detail: String) : AppError("Parse error: $detail")
    data class Unknown(val detail: String) : AppError(detail)
}
```

---

## ⭐ BASE API: `data/remote/datasource/BaseRemoteDataSource.kt`
Semua datasource extend ini → centralisasi safe-call & mapping exception → `AppError`.
```kotlin
package com.project.tickr.data.remote.datasource

import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

abstract class BaseRemoteDataSource(protected val client: SupabaseClient) {

    protected val db: Postgrest get() = client.postgrest

    /** Bungkus call Supabase, map exception ke AppError. */
    protected suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> =
        try {
            DataResult.Success(block())
        } catch (e: Exception) {
            DataResult.Error(e.toAppError())
        }

    private fun Exception.toAppError(): AppError = when {
        message?.contains("401") == true || message?.contains("JWT") == true ->
            AppError.Unauthorized
        message?.contains("network", true) == true ||
            message?.contains("timeout", true) == true ->
            AppError.Network
        this is kotlinx.serialization.SerializationException ->
            AppError.Serialization(message ?: "serialization")
        else -> AppError.Unknown(message ?: this::class.simpleName ?: "unknown")
    }
}
```
> Sesuaikan deteksi exception dengan tipe error nyata dari supabase-kt (mis. `RestException`, `HttpRequestException`) setelah cek API — perbaiki mapping bila perlu.

---

## 📦 DTO — ⚠️ KONFIRMASI SCHEMA DULU

**JANGAN asumsi nama kolom.** Sebelum tulis DTO, tampilkan rencana field per table & **minta konfirmasi** schema aktual dari user. Ekspektasi awal (silakan koreksi):

| Table | DTO | Field (snake_case) |
|-------|-----|--------------------|
| `profiles` | `ProfileDto` | id, username, full_name, avatar_url, updated_at |
| `items` | `ItemDto` | id, profile_id, category_id, name, description, created_at |
| `categories` | `CategoryDto` | id, name, color/icon, created_at |
| `notifications_log` | `NotificationLogDto` | id, profile_id, title/message, is_read, created_at |

Contoh pola DTO:
```kotlin
package com.project.tickr.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    @SerialName("id") val id: String,
    @SerialName("profile_id") val profileId: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("created_at") val createdAt: String
)
```

---

## 🔁 DATASOURCE — pola CRUD
```kotlin
package com.project.tickr.data.remote.datasource

import com.project.tickr.core.result.DataResult
import com.project.tickr.data.remote.dto.ItemDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.query.Columns

class ItemRemoteDataSource(client: SupabaseClient) : BaseRemoteDataSource(client) {

    private val table = "items"

    suspend fun getAll(): DataResult<List<ItemDto>> = safeApiCall {
        db.from(table).select().decodeList<ItemDto>()
    }

    suspend fun getById(id: String): DataResult<ItemDto> = safeApiCall {
        db.from(table).select { filter { eq("id", id) } }.decodeSingle<ItemDto>()
    }

    suspend fun insert(dto: ItemDto): DataResult<ItemDto> = safeApiCall {
        db.from(table).insert(dto) { select() }.decodeSingle<ItemDto>()
    }

    suspend fun update(id: String, dto: ItemDto): DataResult<ItemDto> = safeApiCall {
        db.from(table).update(dto) { filter { eq("id", id) }; select() }.decodeSingle<ItemDto>()
    }

    suspend fun delete(id: String): DataResult<Unit> = safeApiCall {
        db.from(table).delete { filter { eq("id", id) } }; Unit
    }
}
```
> Verifikasi API postgrest-kt (`select`, `filter`, `decodeList`, `insert`) sesuai versi terpasang.

---

## 🔄 MAPPER
```kotlin
package com.project.tickr.data.mapper

import com.project.tickr.data.remote.dto.ItemDto
import com.project.tickr.domain.model.Item

fun ItemDto.toDomain() = Item(
    id = id, profileId = profileId, categoryId = categoryId,
    name = name, description = description, createdAt = createdAt
)

fun Item.toDto() = ItemDto(
    id = id, profileId = profileId, categoryId = categoryId,
    name = name, description = description, createdAt = createdAt
)
```

## 🧩 DOMAIN CONTRACTS (minimal)
```kotlin
// domain/model/Item.kt — PURE Kotlin, no annotations
package com.project.tickr.domain.model
data class Item(
    val id: String, val profileId: String, val categoryId: String?,
    val name: String, val description: String?, val createdAt: String
)

// domain/repository/ItemRepository.kt
package com.project.tickr.domain.repository
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.Item
interface ItemRepository {
    suspend fun getItems(): DataResult<List<Item>>
    suspend fun getItem(id: String): DataResult<Item>
    suspend fun createItem(item: Item): DataResult<Item>
    suspend fun updateItem(item: Item): DataResult<Item>
    suspend fun deleteItem(id: String): DataResult<Unit>
}
```

## 🏗️ REPOSITORY IMPL
```kotlin
package com.project.tickr.data.repository

import com.project.tickr.core.result.DataResult
import com.project.tickr.data.mapper.toDomain
import com.project.tickr.data.mapper.toDto
import com.project.tickr.data.remote.datasource.ItemRemoteDataSource
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.repository.ItemRepository

class ItemRepositoryImpl(
    private val remote: ItemRemoteDataSource
) : ItemRepository {

    override suspend fun getItems(): DataResult<List<Item>> =
        when (val r = remote.getAll()) {
            is DataResult.Success -> DataResult.Success(r.data.map { it.toDomain() })
            is DataResult.Error -> r
        }

    override suspend fun getItem(id: String) = remote.getById(id).mapData { it.toDomain() }
    override suspend fun createItem(item: Item) = remote.insert(item.toDto()).mapData { it.toDomain() }
    override suspend fun updateItem(item: Item) = remote.update(item.id, item.toDto()).mapData { it.toDomain() }
    override suspend fun deleteItem(id: String) = remote.delete(id)
}

// Helper extension (taruh di core/result/DataResult.kt)
inline fun <T, R> DataResult<T>.mapData(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Error -> this
}
```

## 🧩 KOIN — `data/di/DataModule.kt` (minimal)
```kotlin
package com.project.tickr.data.di

import com.project.tickr.core.network.SupabaseProvider
import com.project.tickr.data.remote.datasource.*
import com.project.tickr.data.repository.*
import com.project.tickr.domain.repository.*
import org.koin.dsl.module

val dataModule = module {
    single { SupabaseProvider.client }

    single { ItemRemoteDataSource(get()) }
    single { ProfileRemoteDataSource(get()) }
    single { CategoryRemoteDataSource(get()) }
    single { NotificationRemoteDataSource(get()) }

    single<ItemRepository> { ItemRepositoryImpl(get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
}
```
> Jika Koin belum di-init, **JANGAN** ubah entry point sekarang — cukup buat module ini agar siap dipakai. Catat ke user kalau perlu `startKoin`.

---

## ✅ ACCEPTANCE CRITERIA (Data Layer)

- [ ] Engine OkHttp + **Chucker** terpasang di Android; trafik Supabase muncul di notifikasi/in-app Chucker.
- [ ] iOS pakai engine Darwin (compile sukses, tanpa Chucker).
- [ ] **Ktor Logging plugin** aktif → log request/response di Logcat/console.
- [ ] Hanya **satu** SupabaseClient (sumber tunggal `SupabaseProvider`).
- [ ] `BaseRemoteDataSource` jadi base API tunggal; semua datasource extend & pakai `safeApiCall`.
- [ ] DTO 4 table sesuai **schema aktual** (sudah dikonfirmasi user).
- [ ] Domain model = pure Kotlin (no `@Serializable`, no import framework).
- [ ] Repository impl mengembalikan **domain model**, bukan DTO.
- [ ] `dataModule` Koin wiring lengkap untuk data layer.
- [ ] Project compile di Android (& iOS metadata) tanpa error.
- [ ] TIDAK ada dependency baru ditambah; config Supabase/BuildKonfig tidak diubah.

---

## 🔢 EXECUTION ORDER (verify compile tiap step)

1. **Core**: `DataResult.kt` (+ `mapData`), `AppError.kt`.
2. **Network base**: `HttpEngineFactory` (expect) → actual Android (OkHttp+Chucker) + `ChuckerContext` → actual iOS (Darwin) → `SupabaseProvider` (+ migrasi/ hapus `SupabaseClient.kt` lama) → Ktor Logging. **Verifikasi property `httpEngine` supabase-kt dulu.**
3. **CHECKPOINT**: minta user set `ChuckerContext.applicationContext` di `Application.onCreate()` & test 1 call → pastikan Chucker menangkap log. **Tunggu konfirmasi.**
4. **Schema confirmation**: tampilkan rencana DTO 4 table → **tunggu konfirmasi user** sebelum lanjut.
5. **DTO** → 4 file.
6. **Domain contracts** → model + repository interface (4 entity).
7. **BaseRemoteDataSource** → lalu datasource per-entity.
8. **Mapper** → 4 file.
9. **Repository impl** → 4 file.
10. **DataModule** Koin.
11. **Final**: verify acceptance criteria + compile.

---

**END OF DATA LAYER SPEC**
