# 📋 Template Prompt — Tickr Phase 3.5 (UI Layer · Compose Multiplatform)

> **Cara pakai:** Ini template **reusable per-screen**. Isi bagian bertanda
> `🟨 ISI INI` sesuai layar yang sedang dikerjakan, lalu kirim ke agent.
> Bagian yang TIDAK bertanda kuning bersifat tetap (constraint global) dan
> sebaiknya tidak diubah agar konsistensi arsitektur terjaga.
>
> **Layar aktif di dokumen ini:** **Onboarding (3 screen carousel)** —
> diisi dari hasil analisis gambar `Splash Screen 1/2/3`.

---

## 🎯 Konteks & Peran (TETAP)

```
Bertindaklah sebagai Senior Compose Multiplatform Engineer dengan keahlian:
- Compose Multiplatform (commonMain) + Material 3
- Arsitektur MVI/UDF (state down, events up)
- Design system tokens, theming dark/light, dan aksesibilitas WCAG AA

Anda mengimplementasikan UI Layer (Phase 3.5) di atas Presentation Layer
(Phase 3) yang SUDAH SELESAI. JANGAN mengubah ViewModel, UiState, Action, atau
Event yang sudah ada. UI hanya boleh: collect state, kirim onAction, observe events.
```

---

## ⚠️ Aturan Wajib / Constraints (TETAP)

```
1. SEMUA UI di commonMain (Compose Multiplatform). Tidak ada kode UI di
   androidMain/iosMain kecuali expect/actual untuk platform-specific
   (safe-area, font loader, image/camera picker, share, dsb).
2. Pola tiap layar WAJIB dua lapis:
   - <Feature>Route   → stateful: koinViewModel + collectAsState + LaunchedEffect(events)
   - <Feature>Screen  → stateless: hanya (state, (Action) -> Unit), wajib @Preview-able
3. Zero hardcode visual: SEMUA warna/spacing/ukuran/bentuk lewat design tokens
   (TickrTheme.colors / .spacing / .typography / .shapes). Tidak ada Color(0x..),
   .dp magic number, atau MaterialTheme default yang menimpa token.
4. Zero hardcode string: semua teks via resource (lihat "Strategi String & Drawable").
5. Patuhi kontrak existing: Destination, Navigator, UiText.asString(), UiEvent.
6. Target ketuk minimal 44x44dp. contentDescription wajib utk ikon/gambar bermakna.
7. Kerjakan PER SCREEN. JANGAN membuat layar lain selain yang diminta di blok
   "Scope Layar Saat Ini". Tunggu instruksi terpisah untuk layar berikutnya.
8. Setiap nilai string & nama drawable diberi komentar // TODO(user): sesuaikan
```

---

## 🔒 Strategi Anti-Kebocoran Token Saat Membaca Gambar (TETAP)

```
ATURAN PEMBACAAN GAMBAR:
- JANGAN load/parse file gambar mentah ke konteks untuk men-generate kode.
- Gambar HANYA dideskripsikan SEKALI sebagai "Design Token Brief" tekstual ringkas
  (maks ~15 baris). Setelah itu, kode dibuat HANYA dari brief tekstual + token.
- Jika butuh detail visual baru, MINTA user menyebutkan elemen spesifik secara teks
  (mis. "warna tombol X", "posisi indikator"), JANGAN scan ulang seluruh gambar.
- Untuk layar berikutnya, rujuk brief dengan LABEL pendek (mis. "Onboarding-2"),
  bukan dengan melampirkan/membuka ulang gambar.
- Output kode harus self-contained dari token + brief sehingga gambar tak perlu dibuka lagi.
```

---

## 📐 Design Token Brief (TETAP — sumber: tickr-design-prompt-rev.md)

```kotlin
// === COLORS (Light)        | (Dark — luminance dinaikkan utk kontras WCAG AA) ===
primaryBrand   = #0D6759     | #14A085
primaryPressed = #08443B     | #0D6759
secondaryAccent= #FA9A08     | #FFB13D
background      = #FAFAFA     | #101713
surface         = #FFFFFF     | #1A221E
textPrimary     = #1A1A2E     | #ECEFEC
textSecondary   = #6B7280     | #9BA39C

// Semantik "lampu lalu lintas" (light bg/text → dark bg/text)
critical  #FEECEC / #DC2626  →  #3A1E1E / #FF6B6B   (Hari Ini / < 3 hari)
warning   #FEF6E0 / #D97706  →  #3A3017 / #FFC257   (4–7 hari lagi)
safe      #E8F8EE / #16A34A  →  #16291D / #4ADE80   (> 7 hari lagi)

// === TYPOGRAPHY === Plus Jakarta Sans (fallback Inter). Bobot: Medium/SemiBold/Bold
displayDate     = Bold     30sp   (tracking rapat)
onboardingTitle = Bold     26sp   (rata tengah)
productName     = SemiBold 17sp
body            = Medium   14sp
countdown       = SemiBold 13sp   (warna ikut urgensi)

// === SHAPE === card/bottomSheet=20dp · button=16dp · chip=Pill · thumbnail=14dp
// === SPACING === base 8pt · screenPadding=20dp · cardInner=16dp · itemGap=12dp
// === ELEVATION === soft: 0 4px 16px rgba(13,103,89,0.08) — hanya tombol utama.
//                   Card pakai border tipis / flat elevation.
// === ICON === Lucide / Phosphor, outline, stroke 2px, rounded line caps
```

---

## 🗂️ Struktur File Theme & Navigasi (TETAP — buat sekali, reuse semua screen)

```
commonMain/ui/theme/
├── TickrColors.kt        // data class TickrColors + TickrLightColors / TickrDarkColors
├── TickrTypography.kt    // Typography Material3 + custom styles (displayDate, dst)
├── TickrShapes.kt        // 20/16/14dp, Pill
├── TickrSpacing.kt       // data class TickrSpacing (token spacing)
├── SemanticColors.kt     // mapping critical/warning/safe (light + dark)
└── TickrTheme.kt         // CompositionLocal + TickrTheme {} wrapper, isSystemInDarkTheme()

commonMain/ui/navigation/
├── ComposeNavigator.kt   // implementasi interface Navigator (Phase 3)
└── TickrNavGraph.kt      // NavHost; tambahkan composable per screen di sini
```

### Pola TickrTheme (TETAP)

```kotlin
@Composable
fun TickrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) TickrDarkColors else TickrLightColors
    CompositionLocalProvider(
        LocalTickrColors provides colors,
        LocalTickrSpacing provides TickrSpacing(),
        LocalTickrTypography provides tickrTypography(),
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterial3ColorScheme(darkTheme),
            shapes = TickrShapes,
        ) { content() }
    }
}

// Accessor object → pakai: TickrTheme.colors.primaryBrand, TickrTheme.spacing.screen, dst.
object TickrTheme {
    val colors: TickrColors      @Composable get() = LocalTickrColors.current
    val spacing: TickrSpacing    @Composable get() = LocalTickrSpacing.current
    val typography: TickrTypography @Composable get() = LocalTickrTypography.current
}
```

---

## 🔤 Strategi String & Drawable (TETAP — example, user sesuaikan)

```
STRING (Compose Multiplatform Resources / strings.commonMain.xml):
- Buat dengan nilai EXAMPLE; tandai setiap entry // TODO(user): sesuaikan.
- Pola key: <screen>_<peran>  (mis. onboarding_title_1, auth_cta_google).
- contentDescription pakai prefix cd_  (mis. cd_onboarding_next).

DRAWABLE (placeholder):
- Nama deskriptif: ill_<konteks>, ic_<nama>  (mis. ill_onboarding_welcome).
- Sebelum aset final masuk, render placeholder Box (primaryBrand alpha 0.1 + label)
  agar kode tetap compile. Tandai // TODO(user): ganti aset final.
```

---

## ✅ Acceptance Criteria Global (TETAP)

```
[ ] Route/Screen split benar; Screen punya @Preview light & dark
[ ] Semua warna/spacing/teks via token & resource (zero hardcode)
[ ] Theme light & dark keduanya benar (uji isSystemInDarkTheme)
[ ] Target ketuk ≥44dp; contentDescription ada
[ ] Tidak ada perubahan pada ViewModel/State/Action/Event Phase 3
[ ] Gap data/domain/presentation (bila ada) ditambahkan mengikuti pola Phase 1/2/3
[ ] Navigasi memakai Destination + Navigator (tanpa magic string)
```

---

## 📤 Format Output yang Diminta (TETAP)

```
1. Mulai dengan ringkasan 3–5 baris rencana implementasi.
2. Urutan file: theme (jika belum ada) → gap data/domain/presentation → UI screen.
3. Sertakan @Preview (light + dark) untuk <Feature>Screen.
4. Komentar // TODO(user) di setiap string & drawable example.
5. JANGAN implementasi layar lain di luar Scope Layar Saat Ini.
```

---
---

# 🟨 BAGIAN YANG DIISI PER SCREEN

## 🧭 Scope Layar Saat Ini  🟨 ISI INI

```
Nama layar       : Onboarding (3 halaman carousel)
Label brief      : Onboarding-1, Onboarding-2, Onboarding-3
Jumlah composable: 1 Screen (HorizontalPager) + 1 Route
                   + komponen: PageIndicator, OnboardingPage (per-halaman),
                     CircleArrowButton (hal 1-2), PrimaryButton (hal 3),
                     DecorativeBlobs (background blob hijau pastel)
Catatan          : HANYA kerjakan layar ini. Jangan sentuh Auth/Home/dll.
```

## 🔍 Analisis Gap Lintas Layer  🟨 ISI INI

> Onboarding BELUM ada kontrak di Phase 3 → tambahkan mengikuti pola existing.

```
DATA LAYER:
- OnboardingPreferencesRepository {
      fun hasSeenOnboarding(): Flow<Boolean>
      suspend fun setSeen()
  }
  Implementasi: multiplatform-settings (expect/actual) atau
  DataStore (Android) + UserDefaults (iOS).

DOMAIN LAYER (UseCase tipis, tetap ikuti pola Phase 2):
- GetOnboardingSeenUseCase()  → Flow<Boolean>
- CompleteOnboardingUseCase() → suspend, set flag true

PRESENTATION LAYER (ikuti pola MVI Phase 3):
- OnboardingUiState(currentPage: Int = 0, pages: List<OnboardingPage>, isLastPage: Boolean)
- OnboardingAction: PageChanged(Int), Next, Skip, Finish
- OnboardingEvent : NavigateToAuth   (atau NavigateToHome bila sudah login)
- OnboardingViewModel(completeOnboarding): Finish → CompleteOnboardingUseCase() → emit NavigateToAuth
- Destination.Onboarding : Destination { override val route = "onboarding" }
- DI: factory { OnboardingViewModel(get()) } di presentationModule
- Routing awal: GetOnboardingSeenUseCase → startDestination
  = Onboarding (belum) / Auth (sudah, belum login) / Home (sudah login)

UI LAYER: implementasi Compose (deliverable utama layar ini).

⚠️ KONFIRMASI KE USER (penting):
   - Gambar berlabel "Splash Screen 1/2/3" tetapi secara fungsional ini adalah
     ONBOARDING CAROUSEL (3 halaman swipe-able dengan PageIndicator + tombol Next/
     Lanjutkan), BUKAN Splash statis bertimer. Apakah Splash statis (logo + auto-
     redirect) tetap dibutuhkan terpisah SEBELUM onboarding ini?
   - TIDAK ada tombol "Skip"/"Lewati" yang terlihat di ketiga halaman pada gambar.
     Action.Skip tetap disiapkan di kontrak, namun UI-nya OPSIONAL — konfirmasi
     apakah perlu ditampilkan.
```

## 📱 Spesifikasi Detail Layar  🟨 ISI INI

```
LAYOUT UMUM (semua halaman onboarding — sesuai analisis gambar):
- Background TickrTheme.colors.background (putih nyaris bersih, #FAFAFA).
- 2-3 blob hijau pastel dekoratif sangat soft (primaryBrand alpha sangat rendah):
    • blob besar di POJOK KANAN-ATAS,
    • blob besar di POJOK KIRI-BAWAH / KANAN-BAWAH (lingkaran terpotong tepi layar).
  Implementasi via Canvas/Box, di belakang konten, tidak mengganggu kontras teks.
- Struktur vertikal (rata tengah horizontal):
    [spacer atas] →
    [area ILUSTRASI ~50-55% tinggi: figur + blob hijau organik di belakang figur
     + tanaman pot kecil di kiri bawah figur, gaya flat outline hijau] →
    [BODY TEXT rata-tengah, 2-3 baris, textPrimary] →
    [spacer fleksibel] →
    [PageIndicator] →
    [TOMBOL AKSI] →
    [FOOTER versi/copyright].
- HorizontalPager(pagerState) 3 halaman; swipe & tombol SINKRON.
- ILUSTRASI: di belakang setiap figur ada bentuk "blob" organik hijau muda
  (bukan lingkaran sempurna) sebagai backdrop figur.

POSISI PAGEINDICATOR & TOMBOL (berbeda per halaman — perhatikan gambar):
- Hal 1 & 2: PageIndicator berada DI ATAS tombol panah bundar; tombol panah
  bundar diletakkan di bawah, agak ke bawah layar (rata tengah).
- Hal 3   : PageIndicator berada TEPAT DI BAWAH body text (di atas ilustrasi area
  bawah), lalu tombol FULL-WIDTH "Lanjutkan" di bagian bawah. (Pada gambar hal 3,
  indikator muncul lebih ke atas, dekat teks, karena tombol sudah full-width.)
  → Agar konsisten & sederhana, boleh tempatkan PageIndicator di posisi tetap
    (di atas area tombol) untuk ketiga halaman; PRIORITAS: indikator selalu terlihat
    & sinkron. Catat // TODO(user): sesuaikan posisi presisi bila perlu.

PAGEINDICATOR (sesuai gambar):
- 3 indikator horizontal, rata tengah, gap kecil (~6-8dp).
- AKTIF  = "pill" memanjang horizontal, warna primaryBrand (#0D6759),
           lebar ±20-24dp, tinggi ±6-8dp, sudut Pill.
- INAKTIF= dot bulat kecil abu (textSecondary dengan alpha rendah, ±6-8dp).
- Animasi transisi lebar pill saat halaman berpindah (animateDpAsState).

HALAMAN 1 (Onboarding-1):
- Ilustrasi: ill_onboarding_welcome
  (figur PEREMPUAN berbandana/hairband hijau, baju hijau, MELAMBAIKAN TANGAN,
   ada tanaman pot kecil di kiri; backdrop blob hijau muda)
- Body    : onboarding_title_1  (3 baris, rata tengah)
- Indikator: pill aktif di posisi-1 (kiri)
- Aksi    : tombol BUNDAR hijau (primaryBrand) Ø56dp, ikon panah → putih,
            soft shadow; klik = Action.Next (pager + 1)

HALAMAN 2 (Onboarding-2):
- Ilustrasi: ill_onboarding_eco
  (figur LAKI-LAKI baju hijau bersimbol RECYCLE, MELAMBAIKAN TANGAN sambil
   MENULIS di atas LAPTOP/kertas, tanaman pot kecil di kiri; backdrop blob hijau)
- Body    : onboarding_title_2  (3 baris, rata tengah)
- Indikator: pill aktif di posisi-2 (tengah)
- Aksi    : tombol BUNDAR hijau Ø56dp panah → ; klik = Action.Next

HALAMAN 3 (Onboarding-3, isLast):
- Ilustrasi: ill_onboarding_notify
  (figur PEREMPUAN berbandana hijau, tersenyum, menyentuh PONSEL; ada
   GELEMBUNG NOTIFIKASI dengan ikon LONCENG + badge angka "1" di kanan atas figur;
   tanaman pot kecil di kiri; backdrop blob hijau)
- Body    : onboarding_title_3  (3 baris, rata tengah)
- Indikator: pill aktif di posisi-3 (kanan) — pada gambar diletakkan di bawah teks
- Aksi    : tombol FULL-WIDTH "Lanjutkan" (tinggi 56dp, radius 16dp,
            background primaryBrand, teks putih SemiBold ~17sp, soft shadow);
            klik = Action.Finish → Event.NavigateToAuth

FOOTER (SEMUA halaman): app_version_copyright
  (cth "Versi 1.0.4 • © 2024 Tickr Team"), warna textSecondary, ukuran kecil
  (~12-13sp), rata tengah, di bawah area tombol.

AKSESIBILITAS: contentDescription pada setiap ilustrasi & tombol; target ketuk
  ≥44dp (tombol bundar 56dp sudah memenuhi); kontras teks-bg WCAG AA light & dark.
ANIMASI: pager fling default; tombol panah scale 0.95x saat pressed;
  pill PageIndicator animasi lebar saat halaman aktif berubah.
```

## 🔤 String & Drawable Example untuk Layar Ini  🟨 ISI INI

```xml
<!-- TODO(user): sesuaikan semua nilai di bawah (teks diambil persis dari mockup) -->
<string name="onboarding_title_1">Yuk, kelola bahan makanan, kosmetik, hingga stok tokomu dengan lebih pintar. Katakan selamat tinggal pada barang kedaluwarsa!</string>
<string name="onboarding_title_2">Satu catatan kecil darimu, langkah besar untuk kurangi sampah dan hemat isi dompet. Pantau semua tanggal kedaluwarsa dengan mudah.</string>
<string name="onboarding_title_3">Terima notifikasi sebelum terlambat. Nikmati kemudahan melacak tanpa takut kecolongan lagi. Mulai sekarang!</string>
<string name="onboarding_continue">Lanjutkan</string>
<string name="app_version_copyright">Versi 1.0.4 • © 2024 Tickr Team</string>
<string name="cd_onboarding_next">Lanjut ke halaman berikutnya</string>
<string name="cd_onboarding_welcome">Ilustrasi sambutan Tickr</string>
<string name="cd_onboarding_eco">Ilustrasi ramah lingkungan dan pencatatan</string>
<string name="cd_onboarding_notify">Ilustrasi notifikasi pengingat kedaluwarsa</string>
```

```
DRAWABLE (placeholder — TODO(user): ganti aset final):
- ill_onboarding_welcome   (figur perempuan melambai, bandana hijau, tanaman)
- ill_onboarding_eco       (figur laki-laki kaos recycle, menulis di laptop, melambai)
- ill_onboarding_notify    (figur perempuan + ponsel + gelembung lonceng notifikasi badge "1")
- ic_arrow_right           (ikon panah → untuk tombol bundar hal 1-2; Lucide/Phosphor outline 2px)
```

## ✅ Acceptance Criteria Spesifik Layar Ini  🟨 ISI INI

```
[ ] 3 onboarding screen tampil & swipe-able via HorizontalPager
[ ] Tombol panah bundar (hal 1-2) & "Lanjutkan" full-width (hal 3) sinkron dengan pager
[ ] PageIndicator: pill aktif hijau memanjang + 2 dot abu, update sesuai halaman aktif
[ ] Background menampilkan blob hijau pastel dekoratif (kanan-atas & bawah) tanpa merusak kontras
[ ] Setiap halaman: ilustrasi + body text rata-tengah + footer versi/copyright tampil benar
[ ] Halaman terakhir: Finish memicu CompleteOnboardingUseCase + OnboardingEvent.NavigateToAuth
[ ] Gap data/domain/presentation Onboarding ditambahkan mengikuti pola Phase 1/2/3
[ ] @Preview light & dark untuk OnboardingScreen (per-halaman atau ketiganya)
```

---

## 🔁 Cara Adaptasi Template ini ke Screen Lain

1. Ganti **Scope Layar Saat Ini** dengan nama layar baru (mis. Auth, Home).
2. Pada **Analisis Gap**: layar yang sudah ada di Phase 3 (Auth/Home/ItemList/
   ItemDetail/ItemForm/CategoryList/CategoryForm/Profile) biasanya **tidak butuh
   gap** — cukup tulis "Kontrak sudah ada di Phase 3, tidak ada gap" dan rujuk
   tabel State/Action/Event dari guide Phase 3.
3. Isi **Spesifikasi Detail** dari layar terkait di `tickr-design-prompt-rev.md`
   (Layar 2 Auth, Layar 3 Home, dst) + Design Token Brief.
4. Isi **String & Drawable Example** sesuai elemen layar.
5. Sesuaikan **Acceptance Criteria Spesifik**.
6. Constraint global, token, theme, dan strategi anti-token-leak TIDAK perlu diubah.
