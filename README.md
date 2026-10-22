# Tickr App

Tickr adalah aplikasi manajemen barang kadaluarsa berbasis **Kotlin Multiplatform (KMP)** dengan UI **Compose Multiplatform**, menargetkan Android dan iOS dari satu codebase. Aplikasi ini membantu pengguna melacak tanggal kedaluwarsa produk, mendapat peringatan dini, dan memantau statistik konsumsi.

---

## Daftar Isi

- [Fitur Utama](#fitur-utama)
- [Arsitektur](#arsitektur)
- [Struktur Proyek](#struktur-proyek)
- [Tech Stack & Dependensi](#tech-stack--dependensi)
- [Alur Navigasi](#alur-navigasi)
- [Layer Detail](#layer-detail)
  - [Data Layer](#data-layer)
  - [Domain Layer](#domain-layer)
  - [Presentation Layer (MVI)](#presentation-layer-mvi)
  - [UI Layer](#ui-layer)
- [Setup & Menjalankan](#setup--menjalankan)
- [Konfigurasi Supabase](#konfigurasi-supabase)

---

## Fitur Utama

| Fitur | Status |
|---|---|
| Onboarding carousel (3 layar, swipe animasi) | Selesai |
| Autentikasi — Login, Register, Lupa Password | Selesai |
| Beranda — ringkasan item mendekati kedaluarsa + statistik | Selesai |
| Tambah Barang — bottom sheet dengan upload foto, kategori, satuan | Selesai |
| Kadaluwarsa — daftar item dengan filter, search, urgency badge | Selesai |
| Detail Produk — hero image, info box, urgency banner | Selesai |
| Profil — lihat & edit profil, ganti password | Selesai |
| Riwayat Konsumsi | Selesai |
| Wawasan / Insight | Coming soon |

---

## Arsitektur

Proyek mengikuti **Clean Architecture** dengan 4 layer yang jelas:

```
UI Layer  ──►  Presentation Layer (MVI)  ──►  Domain Layer  ──►  Data Layer
 Compose          ViewModel + State            Use Cases         Repository + Remote
 Screens          Action / Event / UiState     Domain Models     Supabase / Ktor
```

**Prinsip utama:**
- **Unidirectional Data Flow** — UI mengirim `Action`, ViewModel merespons dengan `UiState` baru dan `Event` sekali-pakai.
- **Dependency Inversion** — Domain layer hanya bergantung pada interface (`Repository`), tidak tahu implementasi.
- **KMP-first** — Semua logika bisnis, ViewModel, dan Use Case ada di `commonMain` dan berjalan di Android maupun iOS.

---

## Struktur Proyek

```
Tickr App/
├── androidApp/             # Entry point Android (Activity, Application, Koin init)
├── iosApp/                 # Entry point iOS (SwiftUI wrapper)
├── shared/
│   └── src/
│       ├── commonMain/     # Semua kode KMP (data, domain, presentation, ui)
│       ├── androidMain/    # Implementasi platform Android (HttpEngine, DateUtils, ImagePicker)
│       └── iosMain/        # Implementasi platform iOS
└── gradle/
    └── libs.versions.toml  # Version catalog semua dependensi
```

### Layout `commonMain`

```
com.project.tickr/
├── App.kt                          # Root @Composable, routing gate (Onboarding → Login → Home)
├── core/
│   ├── network/                    # SupabaseProvider, HttpEngineFactory (expect/actual)
│   ├── platform/                   # ImageReader, PlatformInitializer (expect/actual)
│   ├── result/                     # DataResult<T>, AppError (sealed class)
│   ├── util/                       # DateTimeUtil (expect/actual)
│   └── validation/                 # ValidationResult, Validators
├── data/
│   ├── di/                         # DataModule (Koin)
│   ├── mapper/                     # DTO → Domain model mappers
│   ├── remote/
│   │   ├── datasource/             # RemoteDataSource per entitas (Item, Category, Profile, Storage)
│   │   └── dto/                    # Data Transfer Objects (Supabase JSON)
│   └── repository/                 # RepositoryImpl + FakeAuthRepository (untuk testing)
├── domain/
│   ├── di/                         # DomainModule (Koin)
│   ├── model/                      # Domain models: Item, Category, Profile, Urgency, dll.
│   ├── repository/                 # Repository interfaces
│   └── usecase/                    # Use cases per fitur (auth, item, expiry, home, profile, dll.)
├── presentation/
│   ├── di/                         # PresentationModule (Koin)
│   ├── common/                     # UiText, AuthErrorStore, MVI interfaces
│   ├── navigation/                 # Destination (sealed interface), Navigator interface
│   └── [feature]/                  # Satu folder per fitur: Action, Event, UiState, ViewModel
└── ui/
    ├── navigation/                 # TickrNavGraph, MainShell (bottom nav + FAB)
    ├── screen/                     # Layar-layar Compose per fitur
    │   ├── auth/                   # Login, Register, RegisterSuccess, RegisterFailed
    │   ├── onboarding/
    │   ├── home/                   # HomeScreen + AddItemSheet
    │   ├── expiry/                 # ExpiryScreen + detail/
    │   ├── profile/
    │   ├── editprofile/
    │   ├── changepassword/
    │   ├── consumed/
    │   └── help/
    ├── common/                     # ImagePickerLauncher, PlatformDatePicker (expect/actual)
    └── theme/                      # TickrTheme, TickrColors, Typography, Spacing, Shapes
```

---

## Tech Stack & Dependensi

| Kategori | Library | Versi |
|---|---|---|
| Language | Kotlin Multiplatform | 2.4.0 |
| UI | Compose Multiplatform | 1.11.1 |
| UI Components | Material 3 | 1.11.0-alpha07 |
| Navigation | Navigation Compose (KMP) | 2.9.2 |
| Backend / Auth | Supabase KMP (postgrest, auth, storage) | 3.6.0 |
| HTTP Client | Ktor (OkHttp Android, Darwin iOS) | 3.5.0 |
| DI | Koin | 4.2.1 |
| Async | Kotlin Coroutines | 1.11.0 |
| Date & Time | kotlinx-datetime | 0.8.0 |
| Image Loading | Coil 3 + Ktor network fetcher | 3.1.0 |
| Debug (HTTP) | Chucker (Android only, no-op release) | 4.0.0 |
| Build Config | BuildKonfig (inject env keys) | 0.21.2 |

**Platform targets:**
- Android: minSdk 26, compileSdk 37, targetSdk 36
- iOS: via Kotlin/Native (iosSimulatorArm64, iosArm64)

---

## Alur Navigasi

```
App Start
   │
   ├── Onboarding belum dilihat ──► OnboardingScreen
   │                                      │ selesai
   │                                      ▼
   ├── Belum login ─────────────────► LoginScreen
   │                                  │         │
   │                             sukses     daftar baru
   │                                │         │
   │                            HomeScreen  RegisterScreen
   │                                         │
   │                                   RegisterSuccessScreen
   │
   └── Sudah login ─────────────────► HomeScreen (MainShell)
                                            │
                                    ┌───────┴────────────────────┐
                                    │                            │
                              BottomNav                      BottomNav
                          ┌─────────────────────┐            (Profile)
                          │                     │               │
                      Beranda             Kadaluwarsa      ProfileScreen
                          │                     │               ├── EditProfile
                    AddItemSheet          ItemDetailScreen      ├── ChangePassword
                                                                └── Help / ConsumedItems
```

`MainShell` mengelola 4 tab (Beranda, Kadaluwarsa, Wawasan, Profil) dan satu **FAB notch** di tengah bottom bar untuk membuka `AddItemSheet`.

---

## Layer Detail

### Data Layer

**Remote Data Sources** berkomunikasi langsung ke Supabase menggunakan `supabase-kt`:

| DataSource | Tabel Supabase |
|---|---|
| `ItemRemoteDataSource` | `items` |
| `CategoryRemoteDataSource` | `categories` |
| `ProfileRemoteDataSource` | `profiles` |
| `NotificationRemoteDataSource` | `notification_logs` |
| `StorageRemoteDataSource` | Supabase Storage bucket `product-images` |

**Repository Implementations** membungkus data source dan memetakan DTO → Domain model via mapper.  
`FakeAuthRepository` tersedia untuk keperluan testing/preview tanpa koneksi ke Supabase.

**DI** disediakan via `DataModule` (Koin), dimuat di `initKoin()` bersama `domainModule` dan `presentationModule`.

---

### Domain Layer

Domain layer **tidak bergantung** pada implementasi apapun — hanya berisi interface, model, dan use case murni.

**Domain Models utama:**

| Model | Keterangan |
|---|---|
| `Item` | Entitas produk: nama, tanggal kedaluarsa, kategori, jumlah, satuan, URL gambar |
| `Category` | Kategori produk milik user |
| `Profile` | Data profil pengguna |
| `ExpiringItem` | Item yang mendekati kedaluarsa, lengkap dengan `daysUntil` |
| `Urgency` | `CRITICAL` (≤3 hari), `WARNING` (4–7 hari), `SAFE` (>7 hari) |
| `ConsumptionStats` | Statistik: total item, item dikonsumsi, item kedaluarsa |
| `UserSession` | Sesi login aktif |

**Use Cases dikelompokkan per fitur:**

```
auth/       → Login, Register, SignOut, ObserveSession, ChangePassword, ValidateEmail/Name/Password
item/       → GetItems, GetItem, CreateItem, UpdateItem, DeleteItem, MarkConsumed, UploadProductImage
category/   → GetCategories, CreateCategory, UpdateCategory, DeleteCategory
home/       → GetExpiringItemsGrouped, GetCategoryConsumption, GetWasteTrend, AddItem
expiry/     → GetExpiryListItems, GetConsumptionStats
profile/    → GetProfile, UpsertProfile
onboarding/ → GetOnboardingSeen, CompleteOnboarding
notification/ → GetNotifications, ScheduleNotification, DeleteNotification
consumed/   → GetConsumedItems
```

---

### Presentation Layer (MVI)

Setiap fitur memiliki tiga file kontrak MVI:

- **`[Feature]Action`** — intent dari UI ke ViewModel (sealed interface)
- **`[Feature]UiState`** — state yang di-render UI (data class)
- **`[Feature]Event`** — efek sekali-pakai (navigasi, snackbar, dll.)

`ViewModel` mengekspos `uiState: StateFlow<UiState>` dan `events: SharedFlow<Event>`, menerima `onAction(action)`.

Contoh pola untuk `HomeViewModel`:
```
UI kirim HomeAction.Refresh
  → ViewModel update state ke Loading
  → panggil use case
  → emit HomeUiState(items = ..., stats = ...)
```

`AuthErrorStore` adalah shared singleton untuk propagasi error auth lintas ViewModel.

---

### UI Layer

Setiap screen terdiri dari dua file:

- **`[Screen]Route`** — mengambil ViewModel dari Koin, menghubungkan state & event ke composable
- **`[Screen]Screen`** — composable murni, hanya menerima state & callback

**Komponen per fitur:**

| Layar | Komponen utama |
|---|---|
| Home | `GreetingHeader`, `ExpiringSection`, `ConsumptionDonutCard`, `WasteInsightCard`, `HomeSkeletonContent` |
| Kadaluwarsa | `ExpiryItemCard`, `ExpirySearchBar`, `CategoryFilterRow`, `UrgencyLegend`, `ExpiryFilterSheet`, `StatCardPair`, `ExpirySkeletonContent` |
| Detail Produk | `DetailHero`, `UrgencyBanner`, `InfoBoxPair`, `StorageNoteBox`, `DetailSkeletonContent` |
| Auth | `TickrTextField`, `PasswordTextField`, `PasswordStrengthMeter`, `SuccessAnimation` |
| Profile | `ProfileHeader`, `ProfileMenuItem` |
| Edit Profile | `AvatarPickerSheet` |

**Tema** (`ui/theme/`):  
`TickrTheme` menyediakan `TickrColors` (semantic color tokens), `TickrTypography`, `TickrSpacing`, dan `TickrShapes` yang diakses via `TickrTheme.colors`, `.typography`, dll.

**Transisi navigasi** semuanya dikonfigurasi di `TickrNavGraph` dengan durasi 300 ms menggunakan `slideIn/Out` + `fadeIn/Out`.

---

## Setup & Menjalankan

### Prasyarat

- Android Studio Meerkat atau lebih baru (dengan plugin KMP)
- Xcode 15+ (untuk target iOS)
- JDK 17+

### Clone & Konfigurasi

```bash
git clone <repo-url>
cd "Tickr App"
```

Buat file `local.properties` di root proyek dan tambahkan kunci Supabase (lihat bagian bawah):

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
```

### Menjalankan App

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS — buka folder iosApp di Xcode, lalu Run
open iosApp/iosApp.xcodeproj
```

### Menjalankan Tes

```bash
# Android host tests
./gradlew :shared:testAndroidHostTest

# iOS simulator tests
./gradlew :shared:iosSimulatorArm64Test
```

---

## Konfigurasi Supabase

Aplikasi menggunakan Supabase sebagai backend. Berikut yang perlu disiapkan:

1. **Autentikasi** — aktifkan Email/Password Auth di dashboard Supabase.
2. **Tabel database** — buat tabel `items`, `categories`, `profiles`, `notification_logs` dengan skema yang sesuai dengan DTO di `data/remote/dto/`.
3. **Storage bucket** — buat bucket bernama `product-images` dengan akses **public** untuk upload dan tampil gambar produk.
4. **Row Level Security (RLS)** — pastikan policy RLS sudah diset agar setiap user hanya bisa mengakses data miliknya sendiri.

Kunci `SUPABASE_URL` dan `SUPABASE_ANON_KEY` diinjeksikan ke build via **BuildKonfig** dari `local.properties` dan tersedia sebagai konstanta di `BuildConfig` pada runtime.
