# Tickr App

Tickr adalah aplikasi pencatat barang kedaluwarsa berbasis **Kotlin Multiplatform (KMP)** dengan UI **Compose Multiplatform**, untuk Android dan iOS dari satu codebase. Pengguna mencatat barang beserta tanggal kedaluwarsanya, lalu Tickr mengingatkan sebelum barang itu terbuang.

Tickr bersifat **offline-first**: semua data tersimpan di perangkat. Tidak ada akun, login, server, atau koneksi internet yang dibutuhkan.

---

## Daftar Isi

- [Fitur](#fitur)
- [Arsitektur](#arsitektur)
- [Struktur Proyek](#struktur-proyek)
- [Tech Stack](#tech-stack)
- [Data Lokal](#data-lokal)
- [Pengingat Kedaluwarsa](#pengingat-kedaluwarsa)
- [Bahasa dan Tema](#bahasa-dan-tema)
- [Alur Navigasi](#alur-navigasi)
- [Setup dan Menjalankan](#setup-dan-menjalankan)
- [Tes](#tes)
- [Keterbatasan yang Diketahui](#keterbatasan-yang-diketahui)
- [Dokumentasi Lain](#dokumentasi-lain)

---

## Fitur

| Fitur | Keterangan |
|---|---|
| Onboarding | Carousel 3 layar, tampil sekali |
| Beranda | Barang yang akan kedaluwarsa per kategori, donat konsumsi, tren barang terbuang |
| Tambah barang | Nama, kategori, jumlah, satuan, tanggal kedaluwarsa, foto dari galeri |
| Kedaluwarsa | Daftar dengan pencarian, filter kategori, tingkat urgensi, dan rentang waktu |
| Detail barang | Foto, sisa hari, tandai sudah dikonsumsi, hapus |
| Riwayat konsumsi | Barang yang sudah dikonsumsi |
| Notifikasi | Pengingat lokal H-7, H-3, H-1, dan hari-H, plus riwayatnya |
| Pengaturan | Tema (Sistem / Terang / Gelap), bahasa (Sistem / Indonesia / English), Bantuan, Tentang |
| Wawasan | Belum tersedia |

Daftar barang di Beranda, Kedaluwarsa, dan Riwayat konsumsi diperbarui otomatis ketika data berubah, tanpa perlu menarik untuk memuat ulang.

---

## Arsitektur

**Clean Architecture** dengan pola **MVI** di lapisan presentasi.

```
UI (Compose)  ──►  Presentation (ViewModel, MVI)  ──►  Domain (use case, model, interface)  ──►  Data (SQLDelight, file, platform)
```

- **Aliran data satu arah.** UI mengirim `Action`, ViewModel menghasilkan `UiState` baru dan `Event` sekali pakai.
- **Domain tidak mengenal implementasi.** Domain hanya memakai interface (`ItemRepository`, `ImageStorage`, `ReminderScheduler`, `SettingsRepository`, dan sebagainya).
- **KMP-first.** Logika bisnis, ViewModel, dan UI berada di `commonMain`. Kode platform hanya untuk database driver, penyimpanan foto, notifikasi, pengaturan bahasa, dan tema sistem.
- **Reaktif.** Repository mengekspos `Flow` dari SQLDelight, sehingga layar ikut berubah ketika barang ditambah, diubah, dikonsumsi, atau dihapus dari mana pun.
- **Injeksi dependensi** dengan Koin.

---

## Struktur Proyek

```
Tickr App/
├── androidApp/              # Entry point Android (Activity, Application, tema, manifest)
├── iosApp/                  # Entry point iOS (SwiftUI wrapper)
├── shared/
│   ├── schemas/             # Snapshot skema SQLDelight (untuk migrasi)
│   └── src/
│       ├── commonMain/      # Kode bersama + sqldelight/ + composeResources/ (string id/en)
│       ├── androidMain/     # Aktual Android (driver DB, foto, WorkManager, izin, locale)
│       ├── iosMain/         # Aktual iOS (driver DB, foto, UNUserNotificationCenter, locale)
│       └── androidHostTest/ # Tes JVM (database, repository, i18n, pengingat)
└── gradle/libs.versions.toml
```

Isi `commonMain/kotlin/com/project/tickr/`:

```
├── App.kt                  # Root composable: tema, bahasa, onboarding gate, navigasi
├── core/                   # DataResult/AppError, DateTimeUtil, validator
├── data/
│   ├── di/                 # DataModule, LocalModule (Koin)
│   ├── local/              # datasource, db (driver + seeder), image, reminder, settings
│   ├── mapper/             # Baris SQLDelight -> model domain
│   └── repository/         # Local*Repository, SettingsRepositoryImpl
├── domain/
│   ├── model/              # Item, Category, ExpiringItem, Urgency, AppSettings, Reminder, ...
│   ├── reminder/           # ReminderRules (aturan jadwal, murni dan teruji)
│   ├── repository/         # Interface repository
│   ├── scheduler/          # ReminderScheduler (interface)
│   ├── storage/            # ImageStorage (interface)
│   └── usecase/            # Per fitur: item, category, home, expiry, consumed, reminder, settings, ...
├── presentation/           # Per fitur: Action, Event, UiState, ViewModel
└── ui/
    ├── i18n/               # Penerapan bahasa pilihan pengguna
    ├── navigation/         # TickrNavGraph, MainShell (bottom nav + FAB)
    ├── screen/             # Layar per fitur
    ├── common/             # ProductImage, picker foto/tanggal, izin notifikasi, tema platform
    └── theme/              # TickrTheme, TickrColors, tipografi
```

---

## Tech Stack

| Bagian | Teknologi |
|---|---|
| Bahasa | Kotlin 2.4.0 (Multiplatform) |
| UI | Compose Multiplatform 1.11.1, Material 3 |
| Navigasi | navigation-compose |
| DI | Koin 4.2.1 |
| Database | SQLDelight 2.4.1 (SQLite) |
| Gambar | Coil 3 (memuat file lokal) |
| Tanggal | kotlinx-datetime |
| Notifikasi Android | WorkManager 2.12.0 |
| Notifikasi iOS | UNUserNotificationCenter |
| Build | AGP 9.2.1, Gradle version catalog |

Tidak ada dependensi jaringan. Aplikasi tidak meminta izin `INTERNET`.

---

## Data Lokal

Database SQLite dikelola SQLDelight. Skema ada di `shared/src/commonMain/sqldelight/com/project/tickr/db/` dan snapshot-nya di `shared/schemas/`.

| Tabel | Isi |
|---|---|
| `item` | Barang: nama, kategori, jumlah, satuan, tanggal kedaluwarsa (ISO), path foto, status konsumsi |
| `category` | Empat kategori bawaan (Makanan, Kecantikan, Obat, Lainnya) diisi saat pertama kali dijalankan |
| `notification_log` | Riwayat pengingat yang sudah jatuh tempo, unik per (barang, tanggal) |

Hal yang perlu diketahui:

- **Kategori dan satuan disimpan sebagai key** (`food`, `beauty`, `medicine`, `other`; `PCS`, `DOZEN`, dan seterusnya), lalu ditampilkan lewat string resource. Karena itu ganti bahasa tidak mengubah data.
- **Foto produk** disalin ke penyimpanan privat aplikasi dan diperkecil (maksimal 1280 px, JPEG). Database hanya menyimpan path relatif `product_images/<uuid>.jpg`. Foto dihapus ketika barang dihapus atau fotonya diganti.
- **Pengaturan** (tema, bahasa, status onboarding) disimpan di `SharedPreferences` (Android) dan `NSUserDefaults` (iOS).
- Perubahan skema harus lewat migrasi `.sqm`, bukan mengedit file `.sq` begitu saja, supaya data pengguna lama tidak hilang.

---

## Pengingat Kedaluwarsa

Aturan ada di `domain/reminder/ReminderRules`: untuk tiap barang yang belum dikonsumsi, pengingat dikirim pada **H-7, H-3, H-1, dan hari kedaluwarsa pukul 08:00 waktu lokal**. Pengingat yang waktunya sudah lewat dilewati.

| Platform | Mekanisme |
|---|---|
| Android | `WorkManager` periodik harian pukul 08:00 membaca database dan menampilkan pengingat hari itu. Tetap jalan setelah app ditutup dan setelah reboot. |
| iOS | `UNCalendarNotificationTrigger`. Maksimal 60 pengingat terdekat dijadwalkan (batas sistem 64) dan dibangun ulang setiap app dibuka atau data berubah. |

Satu jalur pemrosesan (`ProcessDueRemindersUseCase`) dipakai kedua platform, dan `notification_log` mencatat setiap pengingat tepat sekali. Izin notifikasi diminta sekali, setelah barang pertama berhasil disimpan.

---

## Bahasa dan Tema

- **Bahasa:** Indonesia dan English lewat Compose Resources (`composeResources/values/` dan `values-en/`). Pilihan "Sistem" mengikuti bahasa perangkat; jika bahasa perangkat tidak didukung, English dipakai. Teks notifikasi juga mengikuti pilihan ini.
- **Tema:** Sistem, Terang, atau Gelap. Warna diakses lewat token semantik di `TickrColors` (termasuk `onBrand` dan `onCritical`).

Menambah string: tambahkan di **kedua** file `strings.xml`. Tes `StringResourcesTest` memastikan kunci id dan en sama, placeholder cocok, dan tidak ada string yang tidak terpakai.

---

## Alur Navigasi

```
Onboarding (sekali) ──► Home (MainShell)
                          ├── Tab Beranda ──► Notifikasi, Riwayat konsumsi, Detail barang
                          ├── Tab Kedaluwarsa ──► Detail barang
                          ├── Tab Wawasan (belum tersedia)
                          ├── Tab Pengaturan ──► Bantuan, Tentang
                          └── FAB ──► Sheet Tambah Barang
```

Rute didefinisikan di `presentation/navigation/Destination.kt`.

---

## Setup dan Menjalankan

### Prasyarat

- Android Studio dengan plugin Kotlin Multiplatform
- JDK 17+
- Xcode 15+ untuk target iOS

Tidak ada kunci API, `local.properties` khusus, atau akun layanan yang perlu disiapkan. Cukup clone lalu build.

```bash
git clone <repo-url>
cd "Tickr App"

# Android
./gradlew :androidApp:assembleDebug

# iOS: buka proyek di Xcode lalu Run
open iosApp/iosApp.xcodeproj
```

Untuk memeriksa kompilasi iOS tanpa Xcode:

```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

## Tes

```bash
./gradlew :shared:testAndroidHostTest
```

Tes JVM mencakup database (SQLDelight di memori), repository, penyimpanan dan perkecilan gambar, pengaturan, i18n (paritas string dan resolusi bahasa), aturan pengingat, dan pemrosesan pengingat.

Yang belum bisa diuji otomatis dan perlu dicek manual di perangkat: ganti bahasa saat app berjalan (terutama iOS), tampilan tema gelap, notifikasi yang benar-benar muncul pukul 08:00, dan alur izin notifikasi.

---

## Keterbatasan yang Diketahui

- Data hanya ada di satu perangkat. Tidak ada sinkronisasi atau cadangan antar perangkat.
- iOS: picker foto dan date picker masih berupa stub.
- iOS: notifikasi tidak tampil saat app sedang terbuka (belum ada `UNUserNotificationCenterDelegate`), dan riwayat baru tercatat saat app dibuka berikutnya.
- Android: waktu notifikasi dapat bergeser beberapa menit karena WorkManager dan Doze.
- Belum ada pengaturan jam atau saklar untuk pengingat.

---

## Dokumentasi Lain

- [`TICKR_OFFLINE_FIRST_MIGRATION.md`](TICKR_OFFLINE_FIRST_MIGRATION.md): rencana dan keputusan migrasi ke offline-first.
- [`docs/archive/`](docs/archive): dokumen desain fase lama saat Tickr masih memakai Supabase. Disimpan sebagai arsip dan **tidak lagi menggambarkan kode saat ini**.
