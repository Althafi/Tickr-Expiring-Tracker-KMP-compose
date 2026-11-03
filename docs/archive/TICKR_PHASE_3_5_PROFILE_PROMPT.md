# 📋 Template Prompt — Tickr Phase 3.5 (UI Layer · Compose Multiplatform)
## 🧩 Cluster: Profil · Edit Profil · Ubah Kata Sandi · Hasil (Gagal/Berhasil)

> **Cara pakai:** Ini lanjutan template Phase 3.5 yang sudah diisi untuk **flow Profil**.
> Bagian TETAP (peran, constraints, token, theme, anti-token-leak) **tidak diubah**.
> Bagian 🟨 sudah diisi sesuai 5 screen dari design. Kerjakan **per screen** sesuai
> urutan di "Urutan Pengerjaan".
>
> **Flow ini berisi 5 layar:** Profil → Edit Profil → Ubah Kata Sandi → (Gagal | Berhasil).

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
- Untuk layar berikutnya, rujuk brief dengan LABEL pendek (mis. "Profil-1"),
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
- Pola key: <screen>_<peran>  (mis. profile_logout, password_cta_submit).
- contentDescription pakai prefix cd_  (mis. cd_profile_avatar).

DRAWABLE (placeholder):
- Nama deskriptif: ill_<konteks>, ic_<nama>  (mis. ill_password_failed).
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

# 🟨 BAGIAN YANG DIISI PER SCREEN — CLUSTER PROFIL

> **Urutan Pengerjaan (kirim satu per satu, JANGAN sekaligus):**
> 1. `Profil` (ProfileRoute/Screen)
> 2. `Edit Profil` (EditProfileRoute/Screen)
> 3. `Ubah Kata Sandi` + state Gagal/Berhasil (ChangePasswordRoute/Screen)
>
> Salin **satu blok screen** di bawah ke chat baru saat mengerjakannya.

---

## 🧭 STRATEGI NAVIGASI & TRANSISI (TETAP untuk cluster ini)

```
Daftar Destination (tambahkan di Destination sealed class bila belum ada):
- Destination.Profile          route = "profile"
- Destination.EditProfile      route = "edit_profile"
- Destination.ChangePassword   route = "change_password"
(Hasil Gagal/Berhasil BUKAN destination terpisah — lihat catatan di blok screen 3.)

ALUR NAVIGASI:
Profile --(Edit Profil di-tap)--> EditProfile
EditProfile --(tombol "Ubah Kata Sandi")--> ChangePassword
ChangePassword --(sukses, dari tombol "Kembali ke Profil")--> popBackStack ke Profile
ChangePassword --(gagal → "Coba Lagi")--> kembali ke form (reset state, bukan pop)
EditProfile/ChangePassword --(back arrow / sistem back)--> popBackStack

TRANSISI (best practice — terapkan di NavHost via enter/exit/popEnter/popExitTransition):
- PUSH antar layar hierarki (Profile→Edit→ChangePassword):
  SLIDE HORIZONTAL — masuk slideInHorizontally { it } + fadeIn,
  layar lama slideOutHorizontally { -it/4 } + fadeOut (parallax halus).
  Pop = kebalikannya (slideIn dari -it/4, slideOut ke it).
  Durasi 300ms, easing FastOutSlowInEasing.
- TRANSISI STATE form → hasil (Gagal/Berhasil) di DALAM ChangePassword:
  pakai AnimatedContent / Crossfade (fadeIn+scaleIn dari 0.92f),
  BUKAN navigasi route — agar back tetap kembali ke Profile, bukan ke form.
- Bungkus semua transisi nilainya via token durasi:
  TickrTheme bisa diberi MotionTokens (durationShort=200, durationMedium=300).
  // TODO(user): bila MotionTokens belum ada, tambahkan sebagai token, jangan magic number.
- Hormati reduce-motion bila tersedia (expect/actual isReduceMotionEnabled()).
```

---

## ============================================================
## 📱 SCREEN 1 — PROFIL  (Label brief: Profil-1)
## ============================================================

### 🧭 Scope Layar Saat Ini

```
Nama layar       : Profil (root tab Profil di bottom nav)
Label brief      : Profil-1
Jumlah composable: 1 Screen + 1 Route + komponen ProfileMenuItem + ProfileHeader
Catatan          : HANYA kerjakan layar ini. Bottom nav sudah ada (Phase sebelumnya);
                   layar ini adalah konten tab "Profil".
```

### 🔍 Analisis Gap Lintas Layer

```
PERIKSA dulu apakah Profile sudah punya kontrak di Phase 3.
- Jika SUDAH ada (ProfileUiState/Action/Event): "Kontrak sudah ada di Phase 3,
  tidak ada gap" → langsung implementasi UI, rujuk tabel State/Action/Event Phase 3.
- Jika BELUM ada, tambahkan mengikuti pola MVI Phase 3:

DOMAIN/DATA: gunakan existing (GetUserProfileUseCase / ObserveUserUseCase, LogoutUseCase).
  Bila belum ada LogoutUseCase, tambahkan UseCase tipis (suspend, clear session).

PRESENTATION (pola MVI Phase 3):
- ProfileUiState(
      name: String, email: String, avatarUrl: String?,
      isLoading: Boolean = false
  )
- ProfileAction:
      EditProfile, OpenHelp, Logout,
      ConnectGoogle, OpenNotificationSettings, OpenPrivacyPolicy  // ← coming soon
- ProfileEvent: NavigateToEditProfile, NavigateToHelp, NavigateToAuth(setelah logout)
- ProfileViewModel: Logout → LogoutUseCase() → emit NavigateToAuth
- Destination.Profile, Destination.EditProfile, Destination.Help

UI LAYER: deliverable utama layar ini.
```

### 📱 Spesifikasi Detail Layar

```
TOP/HEADER:
- TANPA top app bar back (ini root tab). Header = ProfileHeader terpusat:
  [Avatar bulat ~96dp dengan border tipis + badge edit kecil di kanan-bawah
   (lingkaran primaryBrand + ic_edit putih, ukuran ~28dp, klik = Action.EditProfile)]
  → [name: productName/SemiBold textPrimary]
  → [email: body textSecondary]
- Avatar: ill_avatar_default (placeholder figur hewan/panda) bila avatarUrl null.

LIST MENU (Card surface, border tipis, radius card token, itemGap antar item):
Setiap item = ProfileMenuItem(icon leading bulat soft-bg, label, chevron trailing).
1. "Hubungkan Google"        ic_link        → DISABLED (coming soon)
2. "Pengaturan Notifikasi"   ic_bell        → DISABLED (coming soon)
3. "Kebijakan Privasi"       ic_shield_check→ DISABLED (coming soon)
4. "Bantuan"                 ic_help_circle → AKTIF → Action.OpenHelp

ATURAN ITEM DISABLED (coming soon):
- enabled = false → tidak bisa di-klik (clickable di-skip / Modifier tanpa onClick).
- Visual: konten alpha ~0.45f, chevron diganti badge kecil "Segera" (chip Pill,
  bg primaryBrand alpha 0.12, teks primaryBrand, typography countdown/13sp).
- semantics { disabled() } + contentDescription menyebut "segera hadir".
- // TODO(user): hapus flag disabled saat fitur siap.

TOMBOL LOGOUT:
- "Keluar dari Akun" full-width, style DESTRUCTIVE/text-button:
  bg critical (light #FEECEC / dark token), teks + ic_logout warna critical text (#DC2626/dark).
  Radius button token, tinggi ≥48dp. Klik = Action.Logout.
- // TODO(user): bila perlu konfirmasi, tambahkan dialog sebelum emit Logout.

LAYOUT: screenPadding token, scroll vertikal (Column + verticalScroll/LazyColumn),
  background token. Logout diberi jarak lebih dari list (spacing besar).

AKSESIBILITAS: cd_profile_avatar, cd_profile_edit_badge; target ketuk ≥44dp;
  item disabled tetap punya semantics yang jelas.
```

### 🔤 String & Drawable Example untuk Layar Ini

```xml
<!-- TODO(user): sesuaikan semua nilai di bawah -->
<string name="profile_menu_connect_google">Hubungkan Google</string>
<string name="profile_menu_notifications">Pengaturan Notifikasi</string>
<string name="profile_menu_privacy">Kebijakan Privasi</string>
<string name="profile_menu_help">Bantuan</string>
<string name="profile_logout">Keluar dari Akun</string>
<string name="profile_coming_soon_badge">Segera</string>
<string name="cd_profile_avatar">Foto profil pengguna</string>
<string name="cd_profile_edit_badge">Ubah profil</string>
<string name="cd_profile_coming_soon">Fitur ini segera hadir</string>
```

```
DRAWABLE (placeholder — TODO(user): ganti aset final):
- ill_avatar_default  (figur hewan/panda default)
- ic_edit, ic_link, ic_bell, ic_shield_check, ic_help_circle, ic_logout
```

### ✅ Acceptance Criteria Spesifik Layar Ini

```
[ ] Header avatar+nama+email tampil; badge edit memicu Action.EditProfile
[ ] 3 menu (Google, Notifikasi, Privasi) DISABLED + badge "Segera", tak bisa diklik
[ ] Menu "Bantuan" aktif → Action.OpenHelp → NavigateToHelp
[ ] Logout memicu Action.Logout → LogoutUseCase → NavigateToAuth
[ ] Item disabled punya semantics disabled() + alpha redup
[ ] Gap presentation Profile ditambahkan bila belum ada di Phase 3
```

---

## ============================================================
## 📱 SCREEN 2 — EDIT PROFIL  (Label brief: Profil-2)
## ============================================================

### 🧭 Scope Layar Saat Ini

```
Nama layar       : Edit Profil
Label brief      : Profil-2
Jumlah composable: 1 Screen + 1 Route + komponen LabeledTextField + InfoBox
Catatan          : HANYA kerjakan layar ini.
```

### 🔍 Analisis Gap Lintas Layer

```
PRESENTATION (pola MVI Phase 3 — tambahkan bila belum ada):
- EditProfileUiState(
      name: String, email: String, avatarUrl: String?,
      isSaving: Boolean = false,
      nameError: UiText? = null, emailError: UiText? = null
  )
- EditProfileAction:
      NameChanged(String), EmailChanged(String),
      ChangePhoto, OpenChangePassword, Save, Back
- EditProfileEvent:
      NavigateBack, NavigateToChangePassword, ShowSaveSuccess(snackbar)
- EditProfileViewModel: Save → validasi → UpdateProfileUseCase() →
      emit ShowSaveSuccess lalu NavigateBack (atau tetap, sesuai keputusan UX)
- UpdateProfileUseCase / pickImage (expect/actual image picker) bila perlu.

⚠️ Konfirmasi ke user: apakah "Simpan Perubahan" langsung balik ke Profil
   atau tetap di layar + snackbar sukses? Default: snackbar lalu balik.
```

### 📱 Spesifikasi Detail Layar

```
TOP APP BAR:
- Back arrow (kiri) = Action.Back → NavigateBack (transisi pop slide).
- Judul "Edit Profil" rata tengah, primaryBrand, SemiBold.

KONTEN (scroll, screenPadding):
- Avatar terpusat ~96dp + badge KAMERA kecil kanan-bawah (lingkaran primaryBrand
  + ic_camera putih) → klik = Action.ChangePhoto (buka image picker expect/actual).
- Label "Ubah Foto" (body textSecondary) di bawah avatar, rata tengah.
- Field "Nama Lengkap": label kecil di atas (body textSecondary) +
  TextField surface, border tipis, leading ic_user, radius button token.
- Field "Alamat Email": label + TextField leading ic_mail, keyboard email.
  // TODO(user): tentukan apakah email read-only / editable.
- Tombol OUTLINE "Ubah Kata Sandi" (full-width, border primaryBrand, teks primaryBrand,
  leading ic_lock-refresh) → Action.OpenChangePassword → NavigateToChangePassword (slide).
- InfoBox (Card soft-bg, leading ic_info): teks bantuan kegunaan profil
  (body textSecondary). Lihat string edit_profile_info.

TOMBOL UTAMA (pinned bawah / akhir konten):
- "Simpan Perubahan" full-width primaryBrand, tinggi 56dp, radius button token,
  soft shadow elevation token. enabled = !isSaving; saat isSaving tampilkan
  CircularProgressIndicator kecil. Klik = Action.Save.

AKSESIBILITAS: cd_edit_change_photo, cd_back; field punya error supporting text
  (warna critical) bila nameError/emailError != null; target ≥44dp.
ANIMASI: tombol scale 0.97x saat pressed.
```

### 🔤 String & Drawable Example untuk Layar Ini

```xml
<!-- TODO(user): sesuaikan semua nilai di bawah -->
<string name="edit_profile_title">Edit Profil</string>
<string name="edit_profile_change_photo">Ubah Foto</string>
<string name="edit_profile_label_name">Nama Lengkap</string>
<string name="edit_profile_label_email">Alamat Email</string>
<string name="edit_profile_change_password">Ubah Kata Sandi</string>
<string name="edit_profile_info">Informasi profil Anda akan digunakan untuk memudahkan proses pengelolaan stok dan pengingat tanggal kedaluwarsa.</string>
<string name="edit_profile_save">Simpan Perubahan</string>
<string name="cd_edit_change_photo">Ubah foto profil</string>
<string name="cd_back">Kembali</string>
```

```
DRAWABLE (placeholder — TODO(user): ganti aset final):
- ic_camera, ic_user, ic_mail, ic_lock_refresh, ic_info, ic_arrow_back
```

### ✅ Acceptance Criteria Spesifik Layar Ini

```
[ ] Back arrow → NavigateBack (transisi pop slide)
[ ] Avatar + badge kamera → Action.ChangePhoto (picker expect/actual)
[ ] Field Nama & Email two-way binding ke state + error supporting text
[ ] Tombol outline "Ubah Kata Sandi" → NavigateToChangePassword (slide)
[ ] "Simpan Perubahan" → Action.Save, loading state saat isSaving
[ ] Gap presentation EditProfile ditambahkan bila belum ada
```

---

## ============================================================
## 📱 SCREEN 3 — UBAH KATA SANDI + HASIL (Gagal/Berhasil)
## (Label brief: Profil-3-form, Profil-3-fail, Profil-3-success)
## ============================================================

### 🧭 Scope Layar Saat Ini

```
Nama layar       : Ubah Kata Sandi (form) + 2 state hasil (Gagal, Berhasil)
Label brief      : Profil-3-form / Profil-3-fail / Profil-3-success
Jumlah composable: 1 Screen + 1 Route + sub-state komponen:
                   ChangePasswordForm, ChangePasswordResult(success/fail),
                   PasswordTextField (dgn toggle visibility)
Catatan          : Gagal/Berhasil adalah STATE di dalam screen ini (AnimatedContent),
                   BUKAN route terpisah. Top bar judul berganti "Ubah Kata Sandi"
                   (form) → "Keamanan Akun" (hasil) sesuai design.
```

### 🔍 Analisis Gap Lintas Layer

```
DOMAIN/DATA: ChangePasswordUseCase(current, new): suspend → Result<Unit>
  (validasi panjang/komposisi di domain/presentation, panggil AuthRepository).

PRESENTATION (pola MVI Phase 3):
- ChangePasswordStep: Form | Success | Failure   (enum/sealed untuk AnimatedContent)
- ChangePasswordUiState(
      step: ChangePasswordStep = Form,
      currentPassword: String, newPassword: String, confirmPassword: String,
      currentVisible: Boolean, newVisible: Boolean, confirmVisible: Boolean,
      isSubmitting: Boolean = false,
      errorMessage: UiText? = null    // pesan gagal utk layar Failure
  )
- ChangePasswordAction:
      CurrentChanged(String), NewChanged(String), ConfirmChanged(String),
      ToggleCurrentVisibility, ToggleNewVisibility, ToggleConfirmVisibility,
      Submit, Retry (Failure→Form), BackToProfile (Success→pop), Back
- ChangePasswordEvent: NavigateBackToProfile
- ChangePasswordViewModel:
      Submit → validasi (min 8 char + huruf+angka, new==confirm) →
        ChangePasswordUseCase → sukses: step=Success | gagal: step=Failure+errorMessage
      Retry → step=Form (pertahankan/clear field sesuai keputusan UX)
      BackToProfile → emit NavigateBackToProfile

⚠️ Konfirmasi ke user: apakah field di-clear saat "Coba Lagi"? Default: pertahankan.
```

### 📱 Spesifikasi Detail Layar

```
=== STATE: FORM (Profil-3-form) ===
TOP APP BAR: back arrow (Action.Back) + judul "Ubah Kata Sandi" (textPrimary, center).
HERO: ikon bulat soft-bg (primaryBrand alpha 0.1) + ic_shield_lock primaryBrand,
  → judul "Amankan Akun Anda" (onboardingTitle-ish, primaryBrand, center)
  → subtitle body textSecondary (center).
FIELDS (3x PasswordTextField, label kecil di atas masing-masing):
  1. "Kata Sandi Saat Ini"     → visualTransformation password + trailing ic_eye toggle
  2. "Kata Sandi Baru"         → idem
  3. "Konfirmasi Kata Sandi Baru" → idem
  Toggle eye = Action.Toggle*Visibility; ikon ic_eye / ic_eye_off.
INFO BOX (Card soft-bg, ic_info): "Gunakan minimal 8 karakter dengan kombinasi
  huruf dan angka untuk keamanan maksimal." (body textSecondary).
TOMBOL UTAMA: "Perbarui Kata Sandi" full-width primaryBrand 56dp + trailing
  ic_shield_arrow, soft shadow. enabled=!isSubmitting; loading saat submit.
  Klik = Action.Submit.

=== STATE: FAILURE (Profil-3-fail) ===
TOP APP BAR: back arrow + judul "Keamanan Akun" (primaryBrand, center).
KONTEN center:
  - Ilustrasi ill_password_failed (shield merah dgn ✕). cd wajib.
  - Judul "Gagal Memperbarui Kata Sandi" (Bold ~22sp textPrimary, center).
  - Body = errorMessage (fallback string default), body textSecondary, center.
TOMBOL: "Coba Lagi" full-width primaryBrand 56dp → Action.Retry (kembali ke Form).
  Di bawahnya link teks "Bantuan" (primaryBrand, SemiBold) → Action ke Help (opsional).
  // TODO(user): hubungkan "Bantuan" ke Destination.Help bila diinginkan.

=== STATE: SUCCESS (Profil-3-success) ===
TOP APP BAR: back arrow + judul "Keamanan Akun".
KONTEN center:
  - Ilustrasi ill_password_success (shield hijau ✓ + konfeti). cd wajib.
  - Judul "Kata Sandi Berhasil Diperbarui" (Bold ~22sp textPrimary, center).
  - Body sukses (body textSecondary, center).
TOMBOL: "Kembali ke Profil" full-width primaryBrand 56dp → Action.BackToProfile
  → Event.NavigateBackToProfile (popBackStack ke Profile).

=== TRANSISI ANTAR STATE ===
- AnimatedContent(targetState = state.step):
  Form↔Failure↔Success pakai fadeIn()+scaleIn(0.92f) ↔ fadeOut()+scaleOut.
  Durasi durationMedium token. BUKAN navigasi route.
- Back arrow di state apa pun → Action.Back → popBackStack (kembali ke Edit/Profil).

AKSESIBILITAS: cd_password_toggle (announce show/hide), cd ilustrasi hasil,
  target ≥44dp, kontras WCAG AA light & dark, fokus pindah ke tombol utama saat
  state hasil muncul (announce hasil via liveRegion).
ANIMASI: tombol scale 0.97x saat pressed; konfeti success boleh statis (aset) atau
  animasi ringan — // TODO(user): pilih aset/animasi final.
```

### 🔤 String & Drawable Example untuk Layar Ini

```xml
<!-- TODO(user): sesuaikan semua nilai di bawah -->
<string name="password_title">Ubah Kata Sandi</string>
<string name="password_hero_title">Amankan Akun Anda</string>
<string name="password_hero_subtitle">Perbarui kata sandi secara berkala untuk menjaga data inventaris tetap aman.</string>
<string name="password_label_current">Kata Sandi Saat Ini</string>
<string name="password_label_new">Kata Sandi Baru</string>
<string name="password_label_confirm">Konfirmasi Kata Sandi Baru</string>
<string name="password_rule_info">Gunakan minimal 8 karakter dengan kombinasi huruf dan angka untuk keamanan maksimal.</string>
<string name="password_cta_submit">Perbarui Kata Sandi</string>

<!-- State hasil -->
<string name="security_title">Keamanan Akun</string>
<string name="password_fail_title">Gagal Memperbarui Kata Sandi</string>
<string name="password_fail_body">Maaf, terjadi kendala saat mencoba memperbarui kata sandi Anda. Pastikan koneksi internet stabil atau coba gunakan kata sandi yang berbeda.</string>
<string name="password_fail_retry">Coba Lagi</string>
<string name="password_fail_help">Bantuan</string>
<string name="password_success_title">Kata Sandi Berhasil Diperbarui</string>
<string name="password_success_body">Bagus! Kata sandi Anda telah berhasil diubah. Sekarang akun Anda lebih aman dan siap digunakan kembali.</string>
<string name="password_success_back">Kembali ke Profil</string>

<!-- contentDescription -->
<string name="cd_password_toggle">Tampilkan atau sembunyikan kata sandi</string>
<string name="cd_password_fail_illustration">Ilustrasi gagal memperbarui kata sandi</string>
<string name="cd_password_success_illustration">Ilustrasi kata sandi berhasil diperbarui</string>
```

```
DRAWABLE (placeholder — TODO(user): ganti aset final):
- ic_shield_lock        (hero form)
- ic_eye, ic_eye_off    (toggle visibility)
- ic_shield_arrow       (trailing tombol submit)
- ill_password_failed   (shield merah ✕)
- ill_password_success  (shield hijau ✓ + konfeti)
```

### ✅ Acceptance Criteria Spesifik Layar Ini

```
[ ] 3 field password dengan toggle visibility independen (state per field)
[ ] Validasi (≥8 char, huruf+angka, new==confirm) sebelum Submit
[ ] Submit sukses → step Success (AnimatedContent), gagal → step Failure
[ ] "Coba Lagi" → Action.Retry kembali ke Form (tanpa pindah route)
[ ] "Kembali ke Profil" → NavigateBackToProfile (popBackStack ke Profile)
[ ] Hasil Gagal/Berhasil adalah STATE, bukan Destination terpisah
[ ] Top bar judul: "Ubah Kata Sandi" (form) → "Keamanan Akun" (hasil)
[ ] Gap domain/presentation ChangePassword ditambahkan
```

---

## ============================================================
## 📱 SCREEN BONUS — BANTUAN (Help) — REKOMENDASI (tidak ada di gambar)
## ============================================================

> Layar Bantuan ditarget oleh menu "Bantuan" di Profil tetapi belum ada di design.
> Berikut **rekomendasi konten & struktur** mengikuti design system Tickr.
> Kerjakan SETELAH 3 screen utama, sebagai layar terpisah.

### 🧭 Scope Layar Saat Ini

```
Nama layar       : Bantuan (Help / FAQ + Kontak)
Label brief      : Help-1
Jumlah composable: 1 Screen + 1 Route + komponen FaqExpandableItem + HelpContactRow
Catatan          : Konten berbasis rekomendasi; user sesuaikan teks final.
```

### 🔍 Analisis Gap Lintas Layer

```
PRESENTATION (pola MVI Phase 3 — layar baru, tambahkan):
- HelpUiState(faqs: List<FaqItem>, expandedId: String? = null)
  FaqItem(id, question: UiText, answer: UiText)  // statik dari resource, tanpa data layer
- HelpAction: ToggleFaq(String), ContactEmail, ContactWhatsApp, Back
- HelpEvent: OpenExternal(url/mailto), NavigateBack
- HelpViewModel: ToggleFaq → update expandedId; Contact* → emit OpenExternal
- expect/actual openUrl(url)/openEmail(mailto) utk buka aplikasi eksternal.
- Destination.Help

(Tanpa data/domain layer kompleks — FAQ statik via resource. Bila nanti FAQ
 dari server, baru tambahkan repository/usecase.)
```

### 📱 Spesifikasi Detail Layar

```
TOP APP BAR: back arrow + judul "Bantuan" (primaryBrand, center).
KONTEN (scroll, screenPadding):
- Hero ringkas: ic_help_circle bulat soft-bg + subtitle pengantar (textSecondary).
- SECTION "Pertanyaan Umum (FAQ)":
  daftar FaqExpandableItem (Card surface border tipis, radius card token).
  Tap = Action.ToggleFaq → expand/collapse jawaban (animateContentSize).
  Chevron rotate 180° saat expand.
- SECTION "Hubungi Kami":
  HelpContactRow:
    • ic_mail  "Email Dukungan"   → Action.ContactEmail (mailto)
    • ic_chat  "WhatsApp"          → Action.ContactWhatsApp (wa.me)
  // TODO(user): isi alamat email & nomor WA final.
- Footer kecil: app_version_copyright (reuse string), textSecondary, center.

AKSESIBILITAS: FAQ item announce expanded/collapsed; target ≥44dp; cd ikon kontak.
ANIMASI: animateContentSize untuk expand; chevron rotate token.
```

### 🔤 String & Drawable Example untuk Layar Ini

```xml
<!-- TODO(user): sesuaikan semua nilai (FAQ rekomendasi, ganti sesuai produk) -->
<string name="help_title">Bantuan</string>
<string name="help_subtitle">Temukan jawaban cepat atau hubungi tim kami bila butuh bantuan lebih lanjut.</string>
<string name="help_section_faq">Pertanyaan Umum</string>
<string name="help_section_contact">Hubungi Kami</string>

<string name="help_faq_q1">Bagaimana cara menambahkan barang baru?</string>
<string name="help_faq_a1">Tekan tombol tambah (+) di bilah bawah, isi nama barang, kategori, dan tanggal kedaluwarsa, lalu simpan.</string>
<string name="help_faq_q2">Kapan saya menerima pengingat kedaluwarsa?</string>
<string name="help_faq_a2">Tickr mengirim notifikasi bertahap sesuai tingkat urgensi: aman, peringatan, dan kritis menjelang tanggal kedaluwarsa.</string>
<string name="help_faq_q3">Apakah data saya tersimpan aman?</string>
<string name="help_faq_a3">Data inventaris Anda tersimpan pada perangkat dan akun Anda. Perbarui kata sandi secara berkala lewat menu Keamanan Akun.</string>
<string name="help_faq_q4">Bagaimana cara mengubah kata sandi?</string>
<string name="help_faq_a4">Buka Profil, pilih Edit Profil, lalu tekan Ubah Kata Sandi dan ikuti instruksinya.</string>

<string name="help_contact_email">Email Dukungan</string>
<string name="help_contact_whatsapp">WhatsApp</string>
<string name="cd_help_faq_toggle">Buka atau tutup jawaban</string>
```

```
DRAWABLE (placeholder — TODO(user): ganti aset final):
- ic_help_circle, ic_chevron_down, ic_mail, ic_chat
```

### ✅ Acceptance Criteria Spesifik Layar Ini

```
[ ] FAQ expand/collapse dengan animateContentSize + chevron rotate
[ ] Kontak Email & WhatsApp memicu OpenExternal (expect/actual)
[ ] Back arrow → NavigateBack
[ ] Footer versi reuse string app_version_copyright
[ ] Gap presentation Help ditambahkan (FAQ statik via resource)
```

---

## 🔁 Catatan Adaptasi & Keputusan UX yang Perlu Dikonfirmasi User

```
1. Edit Profil → email read-only atau editable? Simpan = balik + snackbar?
2. Ubah Kata Sandi → "Coba Lagi" mempertahankan atau clear field?
3. Logout → perlu dialog konfirmasi sebelum keluar?
4. Bantuan → alamat email & nomor WhatsApp final.
5. 3 menu coming soon → konfirmasi label badge ("Segera" vs "Segera Hadir").
6. MotionTokens (durasi transisi) → tambahkan ke design token bila belum ada,
   agar durasi slide/crossfade tidak jadi magic number.
```
