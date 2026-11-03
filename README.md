# Tickr

Tickr adalah aplikasi pencatat barang kedaluwarsa berbasis **Kotlin Multiplatform (KMP)** dengan UI **Compose Multiplatform**, untuk Android dan iOS dari satu codebase. Pengguna mencatat barang beserta tanggal kedaluwarsanya, lalu Tickr mengingatkan sebelum barang itu terbuang dan merangkum kebiasaan hemat pengguna.

Tickr bersifat **offline-first**: semua data tersimpan di perangkat. Tidak ada akun, login, atau server. Satu-satunya fitur yang memakai internet adalah **isi otomatis dari barcode** (opsional); seluruh fitur lain, termasuk Wawasan dan pengingat, berjalan tanpa koneksi.

---

## Daftar Isi

- [Fitur](#fitur)
- [Arsitektur](#arsitektur)
- [Struktur Proyek](#struktur-proyek)
- [Tech Stack](#tech-stack)
- [Data Lokal](#data-lokal)
- [Foto Produk](#foto-produk)
- [Scan Barcode](#scan-barcode)
- [Wawasan](#wawasan)
- [Pengingat Kedaluwarsa](#pengingat-kedaluwarsa)
- [Splash dan Animasi](#splash-dan-animasi)
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
| Splash | Splash sistem berlogo daun, dilanjutkan splash beranimasi di dalam app |
| Onboarding | Carousel 3 layar dengan ilustrasi vektor beranimasi, tampil sekali |
| Beranda | Barang yang akan kedaluwarsa per kategori, donat konsumsi, tren barang terbuang |
| Tambah barang | Nama, kategori, jumlah, satuan, tanggal kedaluwarsa, foto (kamera atau galeri), isi otomatis dari barcode |
| Kedaluwarsa | Daftar dengan pencarian, filter kategori, tingkat urgensi, dan rentang waktu |
| Detail barang | Foto, sisa hari, tandai sudah dikonsumsi, hapus |
| Riwayat konsumsi | Barang yang sudah dikonsumsi |
| Wawasan | Skor hemat, statistik, tren 6 bulan, streak, urgensi, kategori, kebiasaan, tips, dan pencapaian; dihitung di perangkat |
| Notifikasi | Pengingat lokal H-7, H-3, H-1, dan hari-H, plus riwayatnya |
| Pengaturan | Tema (Sistem / Terang / Gelap), bahasa (Sistem / Indonesia / English), Bantuan, Tentang |
| Ekspor dan impor data | Simpan semua barang dan foto ke satu file JSON, pulihkan dari file itu |

Daftar barang di Beranda, Kedaluwarsa, Riwayat konsumsi, dan Wawasan diperbarui otomatis ketika data berubah, tanpa perlu menarik untuk memuat ulang.

Setiap aksi memberi umpan balik lewat snackbar dengan ikon centang atau silang beranimasi: barang disimpan, ditandai dikonsumsi (dengan tombol **Urungkan**), dihapus, serta pesan error bila gagal. Kegagalan menyimpan barang tampil di dalam sheet Tambah Barang, karena snackbar tertutup oleh sheet.

---

## Arsitektur

**Clean Architecture** dengan pola **MVI** di lapisan presentasi.

```
UI (Compose)  ──►  Presentation (ViewModel, MVI)  ──►  Domain (use case, model, interface)  ──►  Data (SQLDelight, file, jaringan, platform)
```

- **Aliran data satu arah.** UI mengirim `Action`, ViewModel menghasilkan `UiState` baru dan `Event` sekali pakai.
- **Domain tidak mengenal implementasi.** Domain hanya memakai interface (`ItemRepository`, `ImageStorage`, `ReminderScheduler`, `SettingsRepository`, `ProductLookupRepository`, `ConnectivityProvider`, dan sebagainya).
- **KMP-first.** Logika bisnis, ViewModel, dan UI berada di `commonMain`. Kode platform hanya untuk database driver, penyimpanan foto, notifikasi, kamera, pemindai barcode, pengaturan bahasa, dan tema sistem.
- **Reaktif.** Repository mengekspos `Flow` dari SQLDelight, sehingga layar ikut berubah ketika barang ditambah, diubah, dikonsumsi, atau dihapus dari mana pun.
- **Fungsi murni untuk aturan.** Perhitungan Wawasan (`InsightCalculator`) dan jadwal pengingat (`ReminderRules`) tidak menyentuh database atau platform, sehingga mudah diuji.
- **Injeksi dependensi** dengan Koin.

---

## Struktur Proyek

```
Tickr/
├── androidApp/              # Entry point Android (Activity, Application, tema, splash, manifest)
├── iosApp/                  # Entry point iOS (SwiftUI wrapper)
├── shared/
│   ├── schemas/             # Snapshot skema SQLDelight (untuk migrasi)
│   └── src/
│       ├── commonMain/      # Kode bersama + sqldelight/ + composeResources/ (string id/en, vektor)
│       ├── androidMain/     # Aktual Android (driver DB, foto, kamera, barcode, WorkManager, izin, locale)
│       ├── iosMain/         # Aktual iOS (driver DB, foto, UNUserNotificationCenter, locale)
│       └── androidHostTest/ # Tes JVM (database, repository, i18n, pengingat, wawasan, barcode)
└── gradle/libs.versions.toml
```

Isi `commonMain/kotlin/com/project/tickr/`:

```
├── App.kt                  # Root composable: tema, bahasa, splash, onboarding gate, navigasi
├── core/                   # DataResult/AppError, DateTimeUtil, validator
├── data/
│   ├── backup/             # Format dan codec file cadangan
│   ├── di/                 # DataModule, LocalModule (Koin)
│   ├── local/              # datasource, db (driver + seeder), image, reminder, settings
│   ├── mapper/             # Baris SQLDelight -> model domain
│   ├── remote/             # Pencarian produk dari barcode (Open Food Facts), cek koneksi
│   └── repository/         # Local*Repository, SettingsRepositoryImpl
├── domain/
│   ├── insight/            # InsightCalculator (murni)
│   ├── model/              # Item, Category, InsightData, ProductInfo, AppSettings, Reminder, ...
│   ├── reminder/           # ReminderRules (aturan jadwal, murni dan teruji)
│   ├── repository/         # Interface repository
│   ├── scheduler/          # ReminderScheduler (interface)
│   ├── storage/            # ImageStorage (interface)
│   └── usecase/            # Per fitur: item, category, home, expiry, consumed, insight, product, reminder, settings, backup, ...
├── presentation/           # Per fitur: Action, Event, UiState, ViewModel
└── ui/
    ├── i18n/               # Penerapan bahasa pilihan pengguna
    ├── navigation/         # TickrNavGraph, MainShell (bottom nav + FAB)
    ├── screen/             # Layar per fitur (home, expiry, insight, settings, onboarding, splash, ...)
    ├── common/             # ProductImage, picker foto, kamera, barcode, date picker, snackbar, ikon status
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
| Jaringan | Ktor 3.5.0 (OkHttp di Android, Darwin di iOS), hanya untuk pencarian barcode |
| Pemindai barcode | Google code scanner (ML Kit / Play services) di Android |
| Serialisasi | kotlinx-serialization-json (cadangan data dan respons pencarian produk) |
| Tanggal | kotlinx-datetime |
| Notifikasi Android | WorkManager 2.12.0 |
| Notifikasi iOS | UNUserNotificationCenter |
| Splash Android | androidx.core:core-splashscreen 1.0.1 |
| Build | AGP 9.2.1, Gradle version catalog, minSdk 26 |

Izin Android yang dideklarasikan: `INTERNET` dan `ACCESS_NETWORK_STATE` (pencarian barcode), `POST_NOTIFICATIONS` (pengingat). Aplikasi **tidak** meminta izin `CAMERA`.

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
- **Foto produk** disalin ke penyimpanan privat aplikasi dan diperkecil (maksimal 1280 px, JPEG, orientasi EXIF dikoreksi). Database hanya menyimpan path relatif `product_images/<uuid>.jpg`. Foto dihapus ketika barang dihapus atau fotonya diganti.
- **Pengaturan** (tema, bahasa, status onboarding) disimpan di `SharedPreferences` (Android) dan `NSUserDefaults` (iOS).
- Perubahan skema harus lewat migrasi `.sqm`, bukan mengedit file `.sq` begitu saja, supaya data pengguna lama tidak hilang.

### Cadangan data

Karena tidak ada server, menghapus aplikasi berarti menghapus datanya. Ada dua lapis perlindungan:

1. **Cadangan otomatis sistem.** Android: Auto Backup ke Google Drive pengguna (`allowBackup="true"`), berlaku bila pengguna mengaktifkannya di perangkat. iOS: ikut cadangan iCloud atau perangkat. Ini tidak dapat dijamin dari sisi aplikasi.
2. **Ekspor dan impor manual** di Pengaturan, bagian Data. Ekspor menghasilkan satu file `tickr-backup-YYYY-MM-DD.json` berisi kategori, semua barang, dan foto (base64). Pengguna memilih sendiri lokasinya (Android: pemilih file sistem, iOS: share sheet). Impor **mengganti seluruh data** setelah konfirmasi.

Impor bersifat aman: file divalidasi penuh dulu (identitas, versi, tanggal, jumlah, base64), foto ditulis ke lokasi baru, lalu database diganti dalam satu transaksi. Bila ada langkah yang gagal, data lama dan fotonya tidak berubah. File dari versi Tickr yang lebih baru ditolak dengan pesan khusus. Format didefinisikan di `data/backup/BackupFormat.kt`; naikkan `CURRENT_VERSION` bila bentuknya berubah.

---

## Foto Produk

Mengetuk kotak foto di sheet Tambah Barang membuka pilihan **Ambil foto** atau **Pilih dari galeri**.

| | Android | iOS |
|---|---|---|
| Kamera | `ActivityResultContracts.TakePicture` lewat `FileProvider` (file sementara di `cache/camera/`), memakai aplikasi kamera sistem sehingga **tanpa izin `CAMERA`** | Belum dibuat (stub) |
| Galeri | `PickVisualMedia` (Android 13+) atau `GetContent("image/*")` | Belum dibuat (stub) |

Foto dari kedua sumber masuk ke `ImageStorage` yang sama (diperkecil lalu disimpan lokal). Membatalkan pemilih tidak menghapus foto yang sudah dipilih sebelumnya.

---

## Scan Barcode

Di sheet Tambah Barang, tombol **Scan barcode** mengisi nama, merek, jumlah, satuan, kategori, dan foto produk secara otomatis.

1. Pemindai Google code scanner membaca EAN-13, EAN-8, UPC-A, atau UPC-E. UI pemindai dan izin kamera ditangani Google Play services.
2. `ConnectivityProvider` memeriksa koneksi lebih dulu. Bila offline, pengguna diberi tahu dan mengisi manual.
3. `OpenFactsApi` mencari barcode berurutan ke Open Food Facts, Open Beauty Facts, lalu Open Products Facts; sumber yang menemukan produk juga menjadi petunjuk kategorinya.
4. Foto kemasan diunduh dan disimpan lokal lewat `DownloadProductImageUseCase`, jadi setelah itu tidak lagi bergantung pada internet.

Data publik sering tidak lengkap. Produk yang tidak ditemukan tetap dicatat barcode-nya dan sisanya diisi manual. Tidak ada kunci API yang dibutuhkan.

---

## Wawasan

Tab Wawasan merangkum kebiasaan pengguna **sepenuhnya di perangkat** (`InsightCalculator`, fungsi murni). Definisinya:

- **Dikonsumsi:** barang yang ditandai habis; tanggalnya dari saat ditandai.
- **Terbuang:** belum dikonsumsi dan tanggal kedaluwarsanya sudah lewat; dihitung pada bulan kedaluwarsanya.
- **Aktif:** belum dikonsumsi dan belum lewat.
- **Skor Hemat:** `dikonsumsi × 100 / (dikonsumsi + terbuang)`, ditampilkan bersama tingkatnya (Master Hemat, Hemat, Lumayan, Perlu perhatian). Belum ada skor sebelum ada barang yang selesai.

Kartu yang tampil: skor, statistik (tercatat, aktif, dikonsumsi, terbuang), tren 6 bulan, streak hari tanpa barang terbuang, sebaran urgensi, barang paling mendesak, kategori, kebiasaan, tips, dan pencapaian (barang pertama, 10 barang tercatat, hemat 10 dan 50, bulan tanpa terbuang, streak 7 dan 30 hari, dan seterusnya).

---

## Pengingat Kedaluwarsa

Aturan ada di `domain/reminder/ReminderRules`: untuk tiap barang yang belum dikonsumsi, pengingat dikirim pada **H-7, H-3, H-1, dan hari kedaluwarsa pukul 08:00 waktu lokal**. Pengingat yang waktunya sudah lewat dilewati, termasuk yang jatuh sebelum barang dicatat.

| Platform | Mekanisme |
|---|---|
| Android | `WorkManager` periodik harian pukul 08:00 membaca database dan menampilkan pengingat hari itu. Tetap jalan setelah app ditutup dan setelah reboot. |
| iOS | `UNCalendarNotificationTrigger`. Maksimal 60 pengingat terdekat dijadwalkan (batas sistem 64) dan dibangun ulang setiap app dibuka atau data berubah. |

Satu jalur pemrosesan (`ProcessDueRemindersUseCase`) dipakai kedua platform, dan `notification_log` mencatat setiap pengingat tepat sekali. Izin notifikasi diminta sekali, setelah barang pertama berhasil disimpan.

---

## Splash dan Animasi

- **Splash sistem (Android):** `installSplashScreen()` dengan tema `Theme.Tickr.Starting` menampilkan logo daun di atas latar terang atau gelap sesuai mode sistem, sehingga tidak ada lagi layar kosong sebelum Compose siap.
- **Splash dalam app:** `TickrSplash` menyambungnya dengan daun yang muncul membal, tulang daun yang tergambar bertahap, lalu tulisan "Tickr". Layar tujuan (Onboarding atau Home) baru dimuat setelah intro selesai, lalu splash memudar.
- **Ikon app:** adaptive icon berupa daun Tickr (hijau `#0D6759`, tulang daun oranye), sama dengan logo di splash. Ikon iOS memakai gambar yang sama.
- **Ilustrasi onboarding:** tiga adegan digambar sebagai vector drawable per bagian (`composeResources/drawable/ill*_*.xml`, kanvas 500x500 yang ditumpuk persis), dianimasikan di `OnboardingIllustrations.kt`: intro bertahap, lambaian, kedipan, napas, daun bergoyang, emblem daur ulang berputar, tangan menulis, lonceng berdering, dan riak notifikasi. Animasi diputar ulang setiap halaman menjadi aktif.
- **Ikon status:** `AnimatedStatusIcon` menggambar cincin, lalu centang (sukses) atau silang (error) bertahap di snackbar.

---

## Bahasa dan Tema

- **Bahasa:** Indonesia dan English lewat Compose Resources (`composeResources/values/` dan `values-en/`). Pilihan "Sistem" mengikuti bahasa perangkat; jika bahasa perangkat tidak didukung, English dipakai. Teks notifikasi juga mengikuti pilihan ini.
- **Tema:** Sistem, Terang, atau Gelap. Warna diakses lewat token semantik di `TickrColors` (termasuk `onBrand` dan `onCritical`).

Menambah string: tambahkan di **kedua** file `strings.xml`. Tes `StringResourcesTest` memastikan kunci id dan en sama, placeholder cocok, dan tidak ada string yang tidak terpakai.

---

## Alur Navigasi

```
Splash ──► Onboarding (sekali) ──► Home (MainShell)
                                     ├── Tab Beranda ──► Notifikasi, Riwayat konsumsi, Detail barang
                                     ├── Tab Kedaluwarsa ──► Detail barang
                                     ├── Tab Wawasan
                                     ├── Tab Pengaturan ──► Bantuan, Tentang
                                     └── FAB ──► Sheet Tambah Barang ──► Pilihan foto, Scan barcode
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

Build debug jauh lebih lambat daripada rilis (JIT, tanpa baseline profile). Di perangkat kelas menengah-bawah, start dingin build debug pernah terukur sekitar 10 detik dan turun menjadi sekitar 3 detik setelah dikompilasi AOT. Nilai kinerja sebaiknya diukur dengan build rilis.

---

## Tes

```bash
./gradlew :shared:testAndroidHostTest
```

Tes JVM mencakup database (SQLDelight di memori), repository, ekspor/impor cadangan (termasuk file rusak, versi lebih baru, dan rollback), penyimpanan dan perkecilan gambar, pengaturan, i18n (paritas string dan resolusi bahasa), aturan pengingat, pemrosesan pengingat, perhitungan Wawasan, pencarian barcode dan alur Tambah Barang dengan barcode, antrean pesan snackbar, dan **graf DI** (semua ViewModel diselesaikan dari modul Koin asli).

Yang belum bisa diuji otomatis dan perlu dicek manual di perangkat: ganti bahasa saat app berjalan (terutama iOS), tampilan tema gelap, notifikasi yang benar-benar muncul pukul 08:00, alur izin notifikasi, serta tampilan dan gerak splash, ilustrasi onboarding, dan kamera.

---

## Keterbatasan yang Diketahui

- Data hanya ada di satu perangkat. Tidak ada sinkronisasi; pindah perangkat lewat ekspor dan impor.
- File cadangan memuat semua foto dalam satu file, jadi ukurannya bisa besar bila barangnya sangat banyak.
- Tidak ada layar untuk mengedit barang yang sudah dicatat (hanya tambah, tandai dikonsumsi, dan hapus).
- Splash beranimasi, ilustrasi onboarding vektor, dan foto dari kamera baru ditulis dan belum diuji di perangkat.
- iOS: pemilih foto, kamera, pemindai barcode, dan date picker masih berupa stub. Di iOS, scan barcode melaporkan gagal sehingga pengguna mengisi manual.
- iOS: ekspor dan impor (share sheet dan document picker) sudah dikompilasi tetapi belum pernah dijalankan di perangkat.
- iOS: notifikasi tidak tampil saat app sedang terbuka (belum ada `UNUserNotificationCenterDelegate`), dan riwayat baru tercatat saat app dibuka berikutnya.
- Android: scan barcode membutuhkan Google Play services.
- Android: waktu notifikasi dapat bergeser beberapa menit karena WorkManager dan Doze.
- Belum ada pengaturan jam atau saklar untuk pengingat.

---

## Dokumentasi Lain

- [`TICKR_OFFLINE_FIRST_MIGRATION.md`](TICKR_OFFLINE_FIRST_MIGRATION.md): rencana dan keputusan migrasi ke offline-first.
- [`docs/archive/`](docs/archive): dokumen desain fase lama saat Tickr masih memakai Supabase. Disimpan sebagai arsip dan **tidak lagi menggambarkan kode saat ini**.
