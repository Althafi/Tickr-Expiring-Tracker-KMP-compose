# Tickr: Migrasi ke Offline-First, Tanpa User, dengan Pengaturan Tema dan Bahasa

Rencana bertahap untuk mengubah Tickr dari app yang bergantung pada Supabase (login, profil, cloud) menjadi app yang:

- **Offline-first**: semua data tersimpan di perangkat, tidak butuh internet sama sekali.
- **Tanpa user**: tidak ada login, register, profil, avatar, ganti password, atau konsep `userId`.
- **Notifikasi lokal saja**: pengingat dijadwalkan di perangkat. Tidak ada push, tidak ada server, tidak ada FCM/APNs.
- **Tab Pengaturan** menggantikan tab Profil, hanya berisi: tema (terang/gelap), bahasa (Indonesia/English), dan informasi aplikasi.

Arsitektur (Clean Architecture + MVI), library inti, dan seluruh fitur inti yang sudah ada tetap dipertahankan.

> Status: rencana. Belum ada kode yang diubah.
> Dasar: pembacaan kode branch `main` (commit `e2d8752`).
> Revisi 2: menambahkan hapus-profil-total, Pengaturan (tema + bahasa + info app), dan fase multi-bahasa.

---

## 1. Apakah bisa? Ya

Arsitektur yang ada membuat ini murah di sisi data, tetapi **ada satu bagian yang jauh lebih besar dari perkiraan awal: multi-bahasa**.

| Kebutuhan | Tingkat kesulitan | Alasan |
|---|---|---|
| Offline-first (DB lokal) | Sedang | Domain hanya bergantung pada interface repository. Supabase hanya menempel di 9 file data layer. Logika bisnis sudah di Kotlin. |
| Tanpa user dan profil | Sedang | Mekanis tapi lebar: `userId` tersebar di sekitar 30 file. Ditangani dengan strategi dua tahap. |
| Notifikasi lokal | Sedang | Butuh kode per-platform (`expect/actual`), tapi tidak butuh server. |
| Tema terang/gelap | **Mudah** | `TickrDarkColors` dan parameter `darkTheme` di `TickrTheme` **sudah ada**. Tinggal dihubungkan ke pengaturan, plus penanganan system bar. |
| Multi-bahasa (ID/EN) | **Paling besar** | Hanya 18 dari sekitar 46 file UI memakai `stringResource`. Teks Indonesia juga tertanam di ViewModel, UiState, use case, `DateTimeUtilImpl`, daftar satuan, dan nama kategori. Lihat Fase 6. |

### Soal notifikasi: ya, cukup lokal

Tidak perlu push. App ini tidak punya data yang berubah dari luar; kapan sebuah barang kedaluwarsa sudah diketahui perangkat saat barang dicatat. Maka pengingat cukup dijadwalkan di perangkat:

- Android: notifikasi lokal lewat `WorkManager`/`AlarmManager`. Butuh izin `POST_NOTIFICATIONS` di Android 13+.
- iOS: `UNUserNotificationCenter` dengan trigger terjadwal.
- Tanpa akun, tanpa token, tanpa Firebase, tanpa koneksi internet.

Batasan jujur: Android tidak menjamin ketepatan menit pada `WorkManager` (mode hemat daya bisa menggeser beberapa menit; untuk pengingat harian pukul 08:00 ini tidak masalah). iOS membatasi maksimal **64** notifikasi tertunda per app.

---

## 2. Keputusan arsitektur

Rekomendasi sudah dipilih. Ubah hanya jika ada alasan kuat.

| Topik | Keputusan | Alasan |
|---|---|---|
| Database | **SQLDelight 2.x** (fallback: Room KMP) | Matang untuk KMP, SQL eksplisit, query bisa jadi `Flow`, tidak butuh KSP. Risiko kompatibilitas dicek di Fase 0. |
| Pengganti `userId` | Konstanta sementara `LOCAL_USER_ID`, lalu **dihapus total** di Fase 9 | Semua ViewModel tetap jalan selama migrasi, tiap fase bisa di-commit. Hasil akhir: tidak ada `userId` sama sekali. |
| Profil / avatar | **Dihapus total** (model, repository, use case, tabel, layar, DiceBear) | Sesuai permintaan. |
| Pengaturan | `AppSettings(themeMode, language)` disimpan di `SharedPreferences` / `NSUserDefaults` | Tidak butuh library baru. |
| Primary key | `Long` autoincrement | `Destination.ItemDetail(Long)` dan seluruh model sudah memakai `Long`. |
| Tanggal | Tetap `String` ISO | Domain model dan `DateTimeUtil` sudah berbasis String. |
| Gambar produk | Salin ke folder privat app, simpan path **relatif** di kolom `image_url` | Nama field tetap, Coil 3 bisa memuat file. |
| Bahasa | Compose Resources: `values/` (Indonesia, default, file yang sekarang) + `values-en/` | Tidak butuh library baru. Pergantian bahasa di dalam app tanpa restart (lihat Fase 6). |
| Nama kategori default | Kolom `key` stabil (`food`, `beauty`, ...) dan nama ditampilkan dari resource | Nama yang di-seed ke DB tidak bisa ikut berganti bahasa kalau disimpan sebagai teks. |
| Satuan (`Pcs`, `Gram`, ...) | Simpan **key** (`PCS`, `GRAM`, ...), tampilkan dari resource | Alasan sama dengan kategori. |
| Notifikasi | Lokal terjadwal, `expect/actual` | Lihat bagian 1. |
| Sinkronisasi cloud | Di luar cakupan | Lihat bagian 9. |

---

## 3. Apa yang dipertahankan, diubah, dihapus

### Dipertahankan

- Layer presentation dan UI untuk: Onboarding, Home, Tambah Barang, Kadaluwarsa, Detail Produk, Riwayat Konsumsi, Bantuan.
- Library: Kotlin Multiplatform, Compose Multiplatform, Material 3, Navigation Compose, Koin, Coroutines, kotlinx-datetime, Coil 3, kotlinx-serialization.
- `DataResult`, `AppError`, `DateTimeUtil` (isinya sebagian diubah, lihat Fase 6), `Validators`.
- Tema `TickrTheme`, `TickrColors` (terang dan gelap), tipografi, spacing, shapes.

### Fitur yang berubah

| Sebelum | Sesudah |
|---|---|
| Login / Register / Google / Lupa password | **Hilang** |
| Tab **Profil** (nama, email, avatar, Edit Profil, Ganti Password, Keluar) | Tab **Pengaturan** (tema, bahasa, info app, bantuan) |
| Sapaan Home "Selamat Datang, {nama}" | "Selamat Datang" tanpa nama |
| Avatar DiceBear (butuh internet) | **Hilang** |
| Gambar di Supabase Storage | File lokal |
| Notifikasi: belum berfungsi | Notifikasi lokal terjadwal + layar riwayat notifikasi |
| Teks Indonesia saja | Indonesia dan English, dipilih di Pengaturan |
| Tema mengikuti sistem saja | Sistem / Terang / Gelap, dipilih di Pengaturan |

### Diubah

| File / area | Perubahan |
|---|---|
| `data/di/DataModule.kt` | Binding repository diarahkan ke implementasi lokal. |
| `data/repository/{Item,Category,Notification}RepositoryImpl.kt` | Ditulis ulang di atas SQLDelight. Interface domain tetap. |
| `domain/usecase/item/UploadProductImageUseCase.kt` | Memakai interface `ImageStorage` di domain (sekarang memanggil `StorageRemoteDataSource` langsung dari domain, melanggar aturan layer). |
| `App.kt` | Gate: `seen` ? Home : Onboarding. Membaca `AppSettings` untuk tema dan bahasa. |
| `OnboardingViewModel`, `OnboardingEvent`, `OnboardingRoute` | Selesai onboarding langsung ke Home. |
| `HomeViewModel`, `GreetingHeader`, `HomeUiState` | Hapus `getProfile` dan `userName`. |
| `MainShell.kt` | Tab `PROFILE` menjadi `SETTINGS`. |
| `HelpViewModel` | FAQ diperbarui (hapus soal kata sandi dan akun), dipindah ke string resource. |
| `TickrNavGraph.kt`, `Destination.kt` | Hapus rute auth, profil, edit profil, ganti password. Tambah `Notifications`, `About` (opsional, bisa satu layar dengan Pengaturan). |
| `AndroidManifest.xml` | Tambah `POST_NOTIFICATIONS`. `INTERNET` dihapus di Fase 9. |
| `TickrApplication.kt` | Hapus `ChuckerContext`. Tambah pembuatan channel notifikasi. |
| `README.md` | Disesuaikan di Fase 9. |

### Dihapus (Fase 4 untuk fitur, Fase 9 untuk dependensi)

- **Auth:** `ui/screen/auth/*`, `ui/screen/changepassword/*`, `presentation/{auth,login,register,changepassword}/*`, `AuthErrorStore`, `domain/usecase/auth/*`, `AuthRepository` + `AuthRepositoryImpl` + `FakeAuthRepository`, `AuthUser`, `UserSession`, `AuthError`, `PasswordStrength`.
- **Profil dan avatar:** `ui/screen/profile/*`, `ui/screen/editprofile/*` (termasuk `AvatarPickerSheet`, `dicebearUrl`), `presentation/{profile,editprofile}/*`, `Profile`, `ProfileRepository` + impl, `ProfileRemoteDataSource`, `ProfileDto`, `ProfileMapper`, `domain/usecase/profile/*`.
- **Remote:** `data/remote/*`, `core/network/*`, `HttpEngineFactory` (expect/actual), `ChuckerContext`.
- **Dependensi:** Supabase (postgrest, auth, storage), Ktor client, Chucker, BuildKonfig, `coil-network-ktor`.
- **Rute dan VM yang sudah mati sebelum migrasi** (tidak punya layar): `Destination.{Auth,ItemList,ItemCreate,ItemEdit,CategoryList}`, `ItemList/Form/DetailViewModel`, `CategoryList/FormViewModel`. Hapus hanya setelah dikonfirmasi tidak dipakai.

---

## 4. Arsitektur target

```
UI (Compose)           tidak berubah, + layar Pengaturan, Notifikasi
   │
Presentation (MVI)     tidak berubah, + SettingsViewModel, NotificationsViewModel
   │
Domain                 use case + interface repository
   │                   + SettingsRepository, ImageStorage, ReminderScheduler, AppInfoProvider
Data                   Repository ──► SQLDelight (item, category, notification_log)
                                  ├─► SettingsStore (SharedPreferences / NSUserDefaults)
                                  ├─► ImageStorage (file lokal)
                                  └─► ReminderScheduler (notifikasi lokal)
```

Struktur baru di `commonMain`:

```
data/local/{db,datasource,image,settings}/
shared/src/commonMain/sqldelight/com/project/tickr/db/{Item,Category,NotificationLog}.sq
composeResources/values/strings.xml        # Indonesia (default)
composeResources/values-en/strings.xml     # English
```

---

## 5. Skema database lokal

Tanpa `user_id`, tanpa tabel `profile`.

```sql
-- Category.sq
CREATE TABLE category (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    key        TEXT UNIQUE,            -- 'food' | 'beauty' | 'medicine' | 'other'; NULL untuk kategori buatan pengguna
    name       TEXT NOT NULL,          -- nama tampil untuk kategori buatan pengguna; fallback untuk default
    icon_name  TEXT NOT NULL,
    color_hex  TEXT NOT NULL,
    created_at TEXT NOT NULL
);

-- Item.sq
CREATE TABLE item (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER REFERENCES category(id) ON DELETE SET NULL,
    name        TEXT NOT NULL,
    barcode     TEXT,
    expiry_date TEXT NOT NULL,          -- 'YYYY-MM-DD'
    image_url   TEXT,                   -- path relatif file lokal
    notes       TEXT,
    is_consumed INTEGER NOT NULL DEFAULT 0,
    quantity    INTEGER NOT NULL DEFAULT 1,
    unit        TEXT NOT NULL DEFAULT 'PCS',   -- KEY satuan, bukan teks tampil
    created_at  TEXT NOT NULL,
    updated_at  TEXT NOT NULL
);
CREATE INDEX item_expiry_idx   ON item(expiry_date);
CREATE INDEX item_consumed_idx ON item(is_consumed);

-- NotificationLog.sq
CREATE TABLE notification_log (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id        INTEGER NOT NULL REFERENCES item(id) ON DELETE CASCADE,
    scheduled_date TEXT NOT NULL,
    is_sent        INTEGER NOT NULL DEFAULT 0,
    created_at     TEXT NOT NULL
);
```

Catatan:
- Aktifkan `PRAGMA foreign_keys = ON` saat membuka database agar `CASCADE` bekerja.
- Domain: `Category` mendapat field `key: String?` dan kehilangan `userId`; `Item` kehilangan `userId`.
- **Kategori default** di-seed saat database pertama dibuat (idempoten, cek `count() == 0`). Salin `icon_name` dari data Supabase yang sekarang agar pemetaan ikon di UI tidak putus. Warna disamakan dengan `DonutCategoryColors`:

  | key | Nama ID (fallback `name`) | Nama EN | Warna |
  |---|---|---|---|
  | `food` | Makanan & Minuman | Food & Drink | `#0D6759` |
  | `beauty` | Kecantikan | Beauty | `#FA9A08` |
  | `medicine` | Obat & Vitamin | Medicine & Vitamins | `#5EC9B7` |
  | `other` | Lainnya | Others | `#6B7280` |

- **Satuan** (key → ID / EN): `PCS` Pcs/Pcs, `GRAM` Gram/Gram, `KILOGRAM` Kilogram/Kilogram, `LITER` Liter/Liter, `MILLILITER` Mililiter/Milliliter, `BOTTLE` Botol/Bottle, `PACK` Bungkus/Pack, `DOZEN` Lusin/Dozen. `UNIT_OPTIONS` di `AddItemUiState.kt` menjadi daftar key.

---

## 6. Rancangan layar Pengaturan

Menggantikan tab Profil. Tetap 4 tab: Beranda, Kadaluwarsa, Wawasan (placeholder), **Pengaturan**.

```
┌──────────────────────────────────┐
│  Pengaturan                      │
│                                  │
│  TAMPILAN                        │
│  ┌────────────────────────────┐  │
│  │ Tema            Sistem  ▸  │  │   ← sheet/dialog: Sistem · Terang · Gelap
│  │ Bahasa       Indonesia  ▸  │  │   ← sheet/dialog: Sistem · Indonesia · English
│  └────────────────────────────┘  │
│                                  │
│  BANTUAN                         │
│  ┌────────────────────────────┐  │
│  │ Bantuan (FAQ)           ▸  │  │   ← layar Help yang sudah ada
│  │ Tentang Aplikasi        ▸  │  │
│  └────────────────────────────┘  │
│                                  │
│          Tickr  v1.0.4           │
└──────────────────────────────────┘
```

**Tentang Aplikasi** berisi: nama app, versi (+ build), deskripsi singkat, catatan bahwa data tersimpan di perangkat, dan (opsional) lisensi library. Versi diambil dari platform lewat `AppInfoProvider` (`expect/actual`: `PackageInfo` di Android, `CFBundleShortVersionString` di iOS), **bukan** string yang ditulis tangan seperti `"Versi 1.0.4 • © 2024 Tickr Team"` di onboarding sekarang.

Domain:

```kotlin
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AppLanguage(val tag: String?) { SYSTEM(null), INDONESIAN("id"), ENGLISH("en") }
data class AppSettings(val themeMode: ThemeMode = ThemeMode.SYSTEM, val language: AppLanguage = AppLanguage.SYSTEM)

interface SettingsRepository {
    val settings: Flow<AppSettings>          // reaktif (StateFlow di balik layar)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: AppLanguage)
}
```

Use case: `ObserveSettingsUseCase`, `SetThemeModeUseCase`, `SetLanguageUseCase`. ViewModel: `SettingsViewModel` dengan `SettingsUiState/Action/Event` (pola MVI yang sama).

Opsional, tidak diminta, tapi wajar bila nanti diinginkan: sakelar "Pengingat kedaluwarsa" (on/off) dan jam pengingat. Tidak termasuk rencana ini.

---

## 7. Langkah demi langkah

Prinsip: **tiap fase berakhir dengan app yang bisa dibuild dan dijalankan**, di-commit sendiri. Kode Supabase baru dihapus di Fase 9.

Ukuran: S = kurang dari setengah hari, M = sekitar 1 hari, L = beberapa hari.

---

### Fase 0: Persiapan dan spike (S)

- [ ] `git switch -c feat/offline-first` dan `git tag pre-offline-first`.
- [ ] **Spike 1: SQLDelight.** Project memakai Kotlin 2.4.0, AGP 9.2.1, dan plugin `com.android.kotlin.multiplatform.library` (bukan `com.android.library` biasa). Di branch percobaan: tambahkan plugin SQLDelight terbaru yang stabil, buat satu tabel, pastikan `:shared:compileAndroidMain` dan `:shared:compileKotlinIosSimulatorArm64` lolos. Jika gagal tanpa solusi cepat: fallback Room KMP (Fase 1 berubah, sisanya tetap).
- [ ] **Spike 2: ganti bahasa saat runtime.** Buktikan pada Compose Multiplatform 1.11.1 bahwa `stringResource` bisa berganti ID ↔ EN dari dalam app tanpa restart, di Android **dan** iOS. Ikuti pendekatan resmi "manage local resource environment" di dokumentasi Compose Multiplatform (override locale lalu paksa recomposition dengan `key(language)`); sesuaikan dengan API versi 1.11.1. Hasil spike ini menentukan desain Fase 6.
- [ ] **Spike 3: tema gelap.** Jalankan app dengan sistem dalam mode gelap dan lihat sendiri seluruh layar utama. Ini menentukan seberapa besar pekerjaan di Fase 5.
- [ ] Putuskan nasib data Supabase yang ada (bagian 9). Default: tidak dimigrasi.

Selesai jika: Spike 1 dan 2 lolos di Android dan iOS (atau fallback sudah diputuskan), dan daftar masalah tema gelap tercatat.

---

### Fase 1: Fondasi database lokal (M)

- [ ] `libs.versions.toml` + `shared/build.gradle.kts`: SQLDelight (`runtime`, `coroutines-extensions`, driver Android di `androidMain`, driver native di `iosMain`; periksa kebutuhan linker `-lsqlite3` pada framework static `Shared`). Gunakan versi yang lolos spike, jangan menyalin angka dari dokumen ini.
- [ ] Blok `sqldelight { databases { create("TickrDatabase") { packageName.set("com.project.tickr.db") } } }`.
- [ ] Tulis `Item.sq`, `Category.sq`, `NotificationLog.sq` (bagian 5) beserta query yang dibutuhkan repository saat ini: select all/by id, insert, update, delete, `markConsumed`, `count`, dan `selectByItem` untuk notifikasi.
- [ ] `expect/actual DatabaseDriverFactory`. Android memakai `AndroidContextHolder` yang sudah ada.
- [ ] `localModule` Koin (database singleton), daftarkan di `initKoin()`.
- [ ] Seeding 4 kategori default (idempoten). `PRAGMA foreign_keys = ON`.
- [ ] Gunakan migrasi SQLDelight (`.sqm`) sejak versi pertama; jangan pernah hapus-dan-buat-ulang database.

Selesai jika: app berjalan seperti biasa (masih Supabase), database terbentuk di perangkat dan berisi 4 kategori.

---

### Fase 2: Repository lokal (L)

Inti migrasi. Interface domain **tidak diubah**.

- [ ] `const val LOCAL_USER_ID = "local"` di `core/` (sementara, dihapus di Fase 9).
- [ ] `*LocalDataSource` untuk item, category, notification. Semua operasi di `Dispatchers.Default/IO`, dibungkus helper yang mengubah exception menjadi `DataResult.Error(AppError.Unknown(...))` (gaya sama dengan `BaseRemoteDataSource.safeApiCall`, tanpa HTTP).
- [ ] Mapper baris DB → domain. Isi `Item.userId = LOCAL_USER_ID` sementara.
- [ ] Implementasi baru dengan nama baru agar yang lama tetap ada sampai Fase 9 (`LocalItemRepository`, `LocalCategoryRepository`, `LocalNotificationRepository`):
  - `createItem`: isi `createdAt/updatedAt` via `DateTimeUtil`; kembalikan item yang baru disimpan (`last_insert_rowid()`).
  - `updateItem`: perbarui `updated_at`.
  - `getItemsByUser(userId)` dan `getCategoriesByUser(userId)`: abaikan parameter, kembalikan semua.
- [ ] Ganti binding di `DataModule`. `ProfileRepository` **belum** dihapus di fase ini (masih dipakai Home/Profil); sementara arahkan ke implementasi sederhana yang mengembalikan profil kosong, atau biarkan ke Supabase sampai Fase 4.
- [ ] `GetCurrentUserIdUseCase` mengembalikan `LOCAL_USER_ID` tanpa memanggil `AuthRepository`. Ini membuat semua `getCurrentUserId() ?: return` di ViewModel tetap bekerja.
- [ ] Tes repository dengan driver in-memory (Fase 10).

Selesai jika: tambah, edit, tandai dikonsumsi, hapus barang, dan statistik Home semuanya bekerja dengan internet dimatikan.

---

### Fase 3: Gambar produk lokal (M)

- [ ] Interface domain:
  ```kotlin
  interface ImageStorage {
      suspend fun saveProductImage(sourcePath: String): DataResult<String>  // path relatif
      suspend fun delete(path: String): DataResult<Unit>
      fun resolve(path: String): String                                     // path relatif -> absolut
  }
  ```
- [ ] `expect/actual`:
  - Android: baca dari URI (`readImageBytes` sudah ada), resize (sisi terpanjang sekitar 1280 px, JPEG kualitas sekitar 80), tulis ke `filesDir/product_images/<uuid>.jpg`.
  - iOS: tulis ke `Application Support/product_images/`. Simpan **path relatif** karena path container iOS bisa berubah antar-update.
- [ ] `UploadProductImageUseCase` dipakai ulang dengan `ImageStorage` (boleh di-rename `SaveProductImageUseCase`; jika di-rename, perbarui `AddItemViewModel` dan Koin).
- [ ] `deleteItem` dan penggantian foto menghapus file lama (cegah file yatim).
- [ ] Semua `AsyncImage` Coil menerima hasil `resolve(path)`. Letakkan resolve di satu tempat (mapper/helper), bukan di tiap composable.

Selesai jika: foto tampil di Home, Kadaluwarsa, dan Detail dalam mode pesawat, dan tetap ada setelah app ditutup paksa.

---

### Fase 4: Hapus login, profil, dan avatar (M)

- [ ] `App.kt`: `startDestination = if (seen) Destination.Home else Destination.Onboarding`. Hapus `ObserveSessionUseCase` dari file ini.
- [ ] Onboarding: ganti event `NavigateToAuth` menjadi `NavigateToHome`, navigasi ke Home dengan `popUpToInclusive`.
- [ ] Home: hapus `getProfile` dan `userName` dari `HomeViewModel`/`HomeUiState`; `GreetingHeader` menampilkan sapaan tanpa nama (sementara teks tetap Indonesia, dipindah ke resource di Fase 6).
- [ ] Hapus layar dan VM: auth, changepassword, profile, editprofile (daftar di bagian 3). Hapus `dicebearUrl`.
- [ ] `MainShell.kt`: ganti `BottomTab.PROFILE` dengan `SETTINGS`, isi sementara `ComingSoonPlaceholder` (diganti di Fase 5).
- [ ] `TickrNavGraph.kt` + `Destination.kt`: hapus rute `Login`, `Register`, `AuthSuccess`, `AuthFailed`, `Auth`, `Profile`, `EditProfile`, `ChangePassword`.
- [ ] Domain/data profil: hapus `Profile`, `ProfileRepository` + impl, use case profil, DTO, mapper, `ProfileRemoteDataSource`, dan binding Koin-nya.
- [ ] `PresentationModule.kt`: hapus VM yang dihapus, `AuthErrorStore`; sesuaikan jumlah `get()` di konstruktor yang parameternya berkurang.
- [ ] `HelpViewModel`: buang FAQ soal kata sandi/akun dan perbarui jawaban "data tersimpan di perangkat Anda" (teks akhir menyusul di Fase 6).
- [ ] Hapus string resource auth/profil dari `strings.xml` (`login_`, `register_`, `password_`, `pw_`, `auth_`, `security_`, `edit_`, `profile_`).

Selesai jika: instal bersih → onboarding → langsung Home tanpa layar login; buka ulang app → langsung Home; tab keempat menampilkan placeholder.

---

### Fase 5: Pengaturan dan tema (M)

- [ ] Domain: `ThemeMode`, `AppLanguage`, `AppSettings`, `SettingsRepository`, 3 use case (bagian 6).
- [ ] Data: `SettingsRepositoryImpl` (`expect/actual` atau satu kelas dengan pembungkus prefs per platform). Harus **reaktif**: pakai `MutableStateFlow` yang diinisialisasi dari prefs dan ditulis ulang saat berubah. Catatan: `OnboardingPreferencesRepositoryImpl` yang ada memakai `flowOf(...)` sekali baca, itu tidak cukup untuk pengaturan.
- [ ] `AppInfoProvider` (`expect/actual`) untuk nama app dan versi.
- [ ] `SettingsViewModel` (+ UiState/Action/Event). `SettingsScreen`/`SettingsRoute` sesuai rancangan bagian 6; pemilih tema dan bahasa berupa bottom sheet atau dialog dengan radio. Layar **Tentang Aplikasi**.
- [ ] Hubungkan tema di `App.kt`:
  ```kotlin
  val settings by observeSettings().collectAsState(initial = AppSettings())
  val dark = when (settings.themeMode) {
      ThemeMode.SYSTEM -> isSystemInDarkTheme()
      ThemeMode.LIGHT  -> false
      ThemeMode.DARK   -> true
  }
  TickrTheme(darkTheme = dark) { ... }
  ```
  Baca pengaturan **sebelum** menampilkan `NavHost` (sama seperti `startDestination` yang kini ditunggu), agar tidak berkedip dari tema/bahasa yang salah.
- [ ] **System bar Android:** `enableEdgeToEdge()` di `MainActivity` mengikuti mode sistem. Jika pengguna memilih tema berbeda dari sistem, ikon status bar dan navigation bar bisa tidak terbaca. Tambahkan `expect/actual SystemBarsEffect(darkTheme)` yang mengatur `isAppearanceLightStatusBars` / `isAppearanceLightNavigationBars`.
- [ ] **iOS:** atur `overrideUserInterfaceStyle` pada window agar elemen native (date picker, keyboard, alert) ikut tema pilihan. `PlatformDatePicker.ios.kt` memakai komponen native sehingga tidak otomatis mengikuti `MaterialTheme`.
- [ ] **Audit tema gelap** (dari spike 3): ada 27 pemakaian `Color.White`/`Color(0x...)` hardcoded di `ui/screen` dan `ui/navigation`. Sebagian besar wajar (teks di atas tombol berwarna brand). Periksa terutama `AddItemSheet.kt:392` (`.background(Color.White)`), donut kategori, kartu urgensi, skeleton, banner urgensi, dan `HomeEmptyState`/`ExpiryEmptyState`. Ganti dengan token `TickrTheme.colors` bila perlu.

Selesai jika: ganti tema dan bahasa-placeholder dari Pengaturan langsung terlihat, tersimpan setelah app ditutup paksa, dan semua layar terbaca di mode gelap.

---

### Fase 6: Multi-bahasa Indonesia dan English (L)

Bagian terbesar. Kerjakan per batch layar, jangan sekaligus. Berdasarkan spike 2.

**6.1 Infrastruktur**
- [ ] Mekanisme override locale (hasil spike 2) dibungkus composable `AppLocaleProvider(language) { ... }` di `App.kt`; `AppLanguage.SYSTEM` mengikuti bahasa perangkat.
- [ ] Buat `composeResources/values-en/strings.xml`. File `values/strings.xml` yang ada tetap jadi bahasa Indonesia (default). Pastikan locale `id` jatuh ke `values/` (tidak perlu folder `values-in`, tetapi buktikan di spike).
- [ ] Perluas `UiText` (sekarang hanya `Raw`) agar ViewModel tidak lagi mengirim kalimat jadi:
  ```kotlin
  sealed interface UiText {
      data class Raw(val value: String) : UiText
      data class Res(val res: StringResource, val args: List<Any> = emptyList()) : UiText
  }
  @Composable fun UiText.asString(): String
  ```
  `asString()` yang sekarang bukan `@Composable`; pemanggilnya perlu disesuaikan.
- [ ] `ErrorMessageProvider.provide(AppError)` mengembalikan `UiText`, bukan `String`.
- [ ] Pluralisasi (mis. "1 barang" vs "2 barang"/"1 item" vs "2 items") memakai `plurals` resource, bukan string biasa.

**6.2 Pindahkan teks hardcoded di UI** (kerjakan satu per satu, build di antaranya)

Cari kandidat dengan:
```
grep -rnE 'Text\("|text = "|label = "|placeholder = "|contentDescription = "|title = "' shared/src/commonMain/kotlin
```
Sekitar 18 file yang masih relevan setelah Fase 4: `MainShell`, `TickrTopBar`, `GreetingHeader`, `ExpiringSection`, `ConsumptionDonutCard`, `WasteInsightCard`, `HomeEmptyState`, `AddItemSheet`, `ExpiryScreen`, `ExpiryFilterSheet`, `ExpiryEmptyState`, `ExpiryItemCard`, `ItemDetailScreen`, `UrgencyBanner`, `StorageNoteBox`, `ConsumedItemsScreen`, `OnboardingScreen`, `HelpScreen`. Jumlah pastinya mungkin lebih banyak karena pola pencarian di atas belum menangkap semua.
- [ ] Batch A: navigasi dan Home.
- [ ] Batch B: Tambah Barang.
- [ ] Batch C: Kadaluwarsa + Detail + Konsumsi.
- [ ] Batch D: Onboarding + Bantuan + Pengaturan + Tentang.
- [ ] Hapus semua komentar `TODO(user): ... string resource ...` yang selesai.

**6.3 Teks di luar UI (sering terlewat)**
- [ ] `OnboardingUiState`: judul 3 halaman → `StringResource` (sekarang `String`).
- [ ] `HelpViewModel`: FAQ → `StringResource` untuk pertanyaan dan jawaban.
- [ ] Pesan error dan validasi di ViewModel/use case (`AddItemViewModel`, `AddItemUseCase`, `ExpiryViewModel`, `ExpiryDetailViewModel`, `ConsumedItemsViewModel`, `ErrorMessageProvider`, `Validators`): ganti dengan `UiText.Res`. Pesan "Sesi habis, silakan masuk kembali" **dihapus**, bukan diterjemahkan (konsep sesi sudah tidak ada).
- [ ] `DateTimeUtilImpl.formatDisplayDate` (nama hari dan bulan Indonesia tertanam) dan `ExpiryItemCard.kt:148` (daftar bulan sendiri): pindahkan nama hari/bulan ke `string-array`/string resource dan format di lapisan UI. `DateTimeUtil` cukup mengembalikan komponen tanggal, tidak lagi teks tampilan.
- [ ] Nama kategori default: tampilkan dari resource berdasarkan `Category.key`; jika `key == null` pakai `name`. Tambahkan `categoryKey` pada `ExpiringItem` (sekarang hanya `categoryName`). Alternatif lebih cepat tetapi kurang bersih: menimpa `name` kategori default di DB tiap kali bahasa berganti.
- [ ] Satuan: tampilkan dari resource berdasarkan key.
- [ ] Teks di `app_version_copyright`: pakai format dengan argumen versi dari `AppInfoProvider`, tanpa tahun tertanam.
- [ ] Teks notifikasi (Fase 7) harus ikut bahasa pilihan app.

**6.4 Pengaman**
- [ ] Tes otomatis: kunci di `values/strings.xml` dan `values-en/strings.xml` harus identik (mencegah terjemahan terlewat).
- [ ] Tinjau terjemahan English; teks Indonesia saat ini sebagian besar ditulis dari mockup dan berisi banyak `TODO(user)`, jadi tinjau juga versi Indonesianya sekalian.

Selesai jika: ganti bahasa dari Pengaturan mengubah seluruh teks (termasuk nama bulan, satuan, kategori, FAQ, error, onboarding) seketika tanpa restart, dan tidak ada teks Indonesia tersisa saat bahasa English.

---

### Fase 7: Notifikasi lokal (L)

Menutup dua TODO yang ada: lonceng Home (`HomeEvent.NavigateToNotifications`) dan penjadwalan.

- [ ] Domain: interface `ReminderScheduler` (`schedule`, `cancel(itemId)`, `cancelAll`, `hasPermission`).
- [ ] Aturan pengingat (use case, mudah dites): untuk tiap item belum dikonsumsi, pengingat pada **H-7, H-3, H-1, dan hari-H pukul 08:00 waktu lokal**, hanya yang waktunya belum lewat. Selaras dengan ambang `Urgency` (≤3 kritis, 4–7 peringatan).
- [ ] Android:
  - `NotificationChannel` dibuat di `TickrApplication.onCreate`.
  - Izin `POST_NOTIFICATIONS` di manifest + permintaan runtime (API 33+).
  - Rekomendasi: satu `WorkManager` periodik harian yang membaca database dan memunculkan notifikasi untuk item yang memenuhi aturan. Lebih sederhana dan tahan reboot dibanding `AlarmManager`. (Menambah dependensi `androidx.work`.)
- [ ] iOS: `UNUserNotificationCenter` + `UNCalendarNotificationTrigger`; minta otorisasi; jadwalkan yang terdekat saja (batas 64) dan jadwalkan ulang setiap app dibuka.
- [ ] Siklus hidup: jadwalkan saat barang ditambah; jadwal ulang saat tanggal diubah; batalkan saat dikonsumsi atau dihapus.
- [ ] Teks notifikasi ikut `AppLanguage` pilihan. Notifikasi dibuat di luar Compose, jadi baca bahasa dari `SettingsRepository` lalu ambil string dengan locale tersebut (cek di spike 2 apakah `getString` mendukung locale eksplisit).
- [ ] Tulis ke `notification_log` sebagai riwayat.
- [ ] Layar **Notifikasi** (daftar dari `notification_log`), tambah `Destination.Notifications`, sambungkan ke lonceng Home (`HomeRoute.kt:22`).
- [ ] Minta izin pada momen yang tepat (setelah onboarding atau saat barang pertama ditambah), bukan saat app pertama dibuka tanpa konteks. Jika ditolak, app tetap berfungsi dan menampilkan petunjuk singkat.

Selesai jika: barang yang kedaluwarsa besok memunculkan notifikasi pada jadwalnya, juga setelah app ditutup dan setelah perangkat di-reboot, dengan bahasa sesuai pilihan.

---

### Fase 8: Reaktivitas dengan Flow (M), opsional

Sekarang `MainShell` memanggil `Refresh` manual di `HomeVM` dan `ExpiryVM` setelah barang disimpan.

- [ ] Tambah `observeItems()` / `observeCategories()` di repository (SQLDelight `asFlow().mapToList(...)`); metode `suspend` lama dipertahankan.
- [ ] `HomeViewModel`, `ExpiryViewModel`, `ConsumedItemsViewModel` mengumpulkan flow; hapus `Refresh` manual di `MainShell`.

Boleh ditunda setelah rilis pertama. Perbaikan kualitas, bukan syarat fungsional.

---

### Fase 9: Pembersihan (M)

Hanya setelah Fase 1–7 terbukti jalan.

- [ ] Hapus semua yang ada di daftar "Dihapus" bagian 3 yang masih tersisa.
- [ ] `libs.versions.toml` dan `shared/build.gradle.kts`: hapus Supabase, Ktor client, Chucker, BuildKonfig (termasuk blok `buildkonfig { }` dan pembacaan `local.properties`), `coil-network-ktor`.
- [ ] Hapus `ChuckerContext`, `HttpEngineFactory`.
- [ ] `AndroidManifest.xml`: hapus `INTERNET`. Izin baca media bisa dihapus jika picker memakai Photo Picker sistem.
- [ ] Hapus `SUPABASE_URL`/`SUPABASE_KEY` dari `local.properties` (sudah di `.gitignore`).
- [ ] **Hapus `userId` total**: `LOCAL_USER_ID`, `GetCurrentUserIdUseCase`, parameter `userId` di `ItemRepository.getItemsByUser`, `CategoryRepository.getCategoriesByUser`, `AddItemParams`, use case `Get*ByUser*`, `GetExpiringItemsGroupedUseCase`, `GetCategoryConsumptionUseCase`, `GetWasteTrendUseCase`, `GetConsumedItemsUseCase`, `GetConsumptionStatsUseCase`, `GetExpiryListItemsUseCase`, serta field `Item.userId` / `Category.userId`. Ganti nama yang menyesatkan (`getItemsByUser` → `getItems`). Gunakan *safe delete*/rename IDE dan biarkan compiler menunjukkan sisanya.
- [ ] Hapus `AppError.Network` dan `AppError.Unauthorized` bila tidak dipakai lagi.
- [ ] `README.md`: fitur, arsitektur, struktur folder, tech stack, bagian setup (hapus Supabase dan `local.properties`), bagian tes.
- [ ] Arsipkan dokumen fase lama (`TICKR_*.md`, `DATA_LAYER_PHASE1_DOCUMENTATION.md`) yang menyebut Supabase.

Selesai jika: build Android dan iOS lolos, dan `grep -rniE "supabase|ktor|chucker|userId|profile" shared androidApp` tidak menemukan sisa yang bermakna.

---

### Fase 10: Tes dan QA (M, dikerjakan paralel sejak Fase 2)

Saat ini hanya ada 3 file tes placeholder (masing-masing 11 baris).

**Unit test (driver in-memory):**
- [ ] CRUD repository item, kategori; cascade hapus item menghapus `notification_log`-nya.
- [ ] Seeding kategori idempoten.
- [ ] `daysToUrgency` (batas 3, 4, 7, 8 hari).
- [ ] `GetWasteTrendUseCase` (bulan lalu 0, bulan ini 0, naik, turun, pergantian tahun Januari).
- [ ] `AddItemUseCase` validasi.
- [ ] Aturan jadwal pengingat (H-7/H-3/H-1/H-0, yang sudah lewat dilewati).
- [ ] `SettingsRepository`: nilai default, penyimpanan, emisi flow.
- [ ] Kunci `values/strings.xml` == kunci `values-en/strings.xml`.

Untuk driver in-memory di `commonTest`, siapkan `expect fun createTestDriver()`; di Android host test bisa memakai JDBC SQLite.

**QA manual (Android dan iOS):**
- [ ] Instal bersih: onboarding → Home, tanpa layar login.
- [ ] **Mode pesawat dari awal sampai akhir:** tambah, edit, konsumsi, hapus barang; semua gambar tampil.
- [ ] Tutup paksa lalu buka lagi: data, tema, dan bahasa utuh.
- [ ] Tema: Sistem/Terang/Gelap di setiap layar dan sheet; ikon status bar terbaca; date picker mengikuti tema (khususnya iOS); tema pilihan berbeda dari tema sistem.
- [ ] Bahasa: tiap layar dalam ID dan EN; tidak ada teks terpotong akibat teks English lebih panjang/pendek; nama bulan/hari, satuan, kategori, FAQ, error, onboarding; notifikasi mengikuti bahasa pilihan.
- [ ] Ubah bahasa sistem perangkat saat pilihan app = "Sistem": app ikut berubah.
- [ ] 500–1000 barang: scroll tetap mulus.
- [ ] Tanggal di sekitar tengah malam dan saat zona waktu berubah.
- [ ] Izin notifikasi ditolak: app tetap berfungsi, ada petunjuk jelas.
- [ ] Reboot perangkat: pengingat tetap jalan.
- [ ] Ukuran font sistem besar.

---

## 8. Urutan commit yang disarankan

```
chore: add sqldelight and local database foundation
feat: add local repositories for item, category, notification
feat: store product images locally via ImageStorage
refactor: remove login, profile and avatar; open Home after onboarding
feat: settings screen with theme mode and about page
feat: i18n infrastructure and runtime language switch
feat: externalize UI strings to resources (id/en)        # beberapa commit per batch
feat: localize non-UI text (dates, units, categories, errors)
feat: local expiry reminders and notification screen
feat: observe items reactively with Flow                 # opsional
chore: remove supabase, ktor, chucker and buildkonfig
refactor: remove userId from domain signatures
docs: update README for offline-first
test: add repository, settings and i18n key-parity tests
```

---

## 9. Risiko, pertanyaan terbuka, dan jalan ke depan

### Risiko

| Risiko | Dampak | Mitigasi |
|---|---|---|
| SQLDelight belum mendukung Kotlin 2.4.0 + AGP 9.2.1 + plugin KMP-library | Fase 1 terblokir | Spike 1; fallback Room KMP. |
| Pergantian bahasa saat runtime tidak mulus di salah satu platform (terutama iOS) | Fase 6 terhambat | Spike 2 sebelum menulis ulang ratusan string. Fallback: bahasa berlaku setelah restart app dengan pesan jelas. |
| Cakupan multi-bahasa lebih besar dari terlihat | Fase 6 molor | Kerjakan per batch layar; gunakan grep di 6.2; tes kesamaan kunci mencegah terjemahan terlewat. |
| Nama kategori/satuan tersimpan sebagai teks | Tidak ikut berganti bahasa | Skema memakai `key` sejak awal (bagian 5). |
| **Kehilangan data = kehilangan segalanya.** Tanpa cloud, hapus app berarti data hilang | Pengguna kecewa | Android `allowBackup="true"` sudah aktif sehingga Auto Backup mencakup DB dan `filesDir`; iOS ikut backup perangkat. Pertimbangkan Ekspor/Impor (JSON) di masa depan. |
| Path gambar iOS berubah antar-update | Gambar hilang | Simpan path relatif, resolve saat runtime (Fase 3). |
| Batas 64 notifikasi iOS; ketepatan menit WorkManager | Pengingat kurang presisi | Jadwalkan yang terdekat, jadwal ulang saat app dibuka; jam pengingat harian cukup toleran. |
| Tema pilihan ≠ tema sistem membuat ikon system bar dan komponen native tidak terbaca | UI cacat | Fase 5: `SystemBarsEffect` (Android) dan `overrideUserInterfaceStyle` (iOS). |
| Menghapus `userId` menyentuh sekitar 30 file | Regresi | Dilakukan terakhir, dibantu compiler dan tes. |
| Skema DB berubah di rilis mendatang | Crash saat update | Migrasi SQLDelight sejak versi pertama. |

### Pertanyaan yang perlu Anda putuskan

1. **Data Supabase yang sudah ada, perlu dimigrasi?** Jika belum rilis dan datanya hanya uji coba, tidak (default). Jika perlu, tambah satu langkah impor sekali pakai sebelum Fase 9.
2. **Pilihan bahasa:** cukup "Indonesia / English", atau juga "Ikuti sistem"? (Rencana ini memakai tiga opsi dengan default "Sistem".)
3. **Pengaturan:** perlu sakelar on/off pengingat dan jam pengingat? (Tidak termasuk rencana; mudah ditambah.)
4. **Kebijakan Privasi:** menu yang sekarang dinonaktifkan di Profil. Dibuat sebagai halaman statis singkat ("semua data tersimpan di perangkat Anda"), atau dihapus?
5. **Notifikasi dan Flow (Fase 7 dan 8):** dikerjakan sebelum rilis pertama atau setelahnya?

### Jalan ke depan (di luar cakupan)

Karena domain hanya bergantung pada interface, sinkronisasi opsional ke cloud nanti dapat ditambah tanpa membongkar ulang: repository menulis ke DB lokal (sumber kebenaran), lalu `SyncService` terpisah mendorong perubahan bila pengguna memilih login. Itu berarti konsep akun akan kembali, jadi jangan dikerjakan sekarang.

---

## 10. Definisi selesai

- [ ] Instal bersih dan seluruh fitur berfungsi dengan mode pesawat dari awal.
- [ ] Tidak ada layar atau kode login, register, profil, avatar, ganti password, maupun `userId`.
- [ ] Tab Pengaturan: ganti tema (Sistem/Terang/Gelap) dan bahasa (Sistem/Indonesia/English) berlaku seketika dan tersimpan; halaman Tentang Aplikasi menampilkan versi yang benar.
- [ ] Tidak ada teks Indonesia tersisa saat bahasa English, termasuk nama bulan/hari, satuan, kategori, error, FAQ, onboarding, dan notifikasi.
- [ ] Notifikasi pengingat lokal berjalan, termasuk setelah reboot.
- [ ] Data, tema, dan bahasa tetap setelah app ditutup paksa.
- [ ] Tidak ada dependensi atau referensi ke Supabase, Ktor, atau Chucker.
- [ ] Android dan iOS dapat dibuild dari bersih.
- [ ] Tes domain, repository, pengaturan, dan kesamaan kunci string hijau.
- [ ] README mencerminkan kondisi terbaru.
