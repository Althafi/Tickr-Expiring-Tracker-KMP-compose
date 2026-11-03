# 📋 Prompt — Tickr Phase 3.5 · Layar **Auth** (Login · Register · Success)

> **Cara pakai:** Prompt ini sudah **terisi** untuk layar Auth (mengikuti template
> reusable Phase 3.5). Bagian **TETAP** = constraint global (jangan diubah agar
> konsistensi arsitektur terjaga). Bagian bertanda `🟨` = isian spesifik layar Auth.
>
> **Prasyarat:** Onboarding (Phase 3.5) **SUDAH SELESAI**. Tombol "Lanjutkan" pada
> Onboarding halaman terakhir harus diarahkan ke `Destination.Login` (lihat
> "Integrasi dengan Onboarding").

---

## 🎯 Konteks & Peran (TETAP)

```
Bertindaklah sebagai Senior Compose Multiplatform Engineer dengan keahlian:
- Compose Multiplatform (commonMain) + Material 3
- Arsitektur MVI/UDF (state down, events up)
- Design system tokens, theming dark/light, dan aksesibilitas WCAG AA

Anda mengimplementasikan UI Layer (Phase 3.5) di atas Presentation Layer
(Phase 3). Untuk layar Auth, kontrak Presentation/Domain/Data DITAMBAHKAN pada
prompt ini (lihat "Analisis Gap Lintas Layer") karena skema login berubah:
login memakai (Nama Lengkap / Email) + Password, register memakai form
Nama Lengkap + Email + Kata Sandi. UI hanya boleh: collect state, kirim
onAction, observe events.
```

---

## ⚠️ Aturan Wajib / Constraints (TETAP)

```
1. SEMUA UI di commonMain (Compose Multiplatform). Tidak ada kode UI di
   androidMain/iosMain kecuali expect/actual untuk platform-specific
   (safe-area, font loader, image picker, share, secure storage sesi, dsb).
2. Pola tiap layar WAJIB dua lapis:
   - <Feature>Route   → stateful: koinViewModel + collectAsState + LaunchedEffect(events)
   - <Feature>Screen  → stateless: hanya (state, (Action) -> Unit), wajib @Preview-able
3. Zero hardcode visual: SEMUA warna/spacing/ukuran/bentuk lewat design tokens
   (TickrTheme.colors / .spacing / .typography / .shapes). Tidak ada Color(0x..),
   .dp magic number, atau MaterialTheme default yang menimpa token.
4. Zero hardcode string: semua teks via resource (lihat "Strategi String & Drawable").
5. Patuhi kontrak existing: Destination, Navigator, UiText.asString(), UiEvent.
6. Target ketuk minimal 44x44dp. contentDescription wajib utk ikon/gambar bermakna.
7. Kerjakan PER SCREEN dalam scope Auth: Login + Register + Success (satu fitur
   logis "auth"). JANGAN membuat layar lain (Home/ItemList/dll).
8. Setiap nilai string & nama drawable diberi komentar // TODO(user): sesuaikan
9. Login Google / OAuth di-SKIP dulu: tombol tampil tetapi handler hanya
   // TODO(user): implementasi Google/OAuth (Supabase) — jangan panggil apa pun.
```

---

## 🔒 Strategi Anti-Kebocoran Token Saat Membaca Gambar (TETAP)

```
ATURAN PEMBACAAN GAMBAR:
- JANGAN load/parse file gambar mentah ke konteks untuk men-generate kode.
- Gambar HANYA dideskripsikan SEKALI sebagai "Design Token Brief" tekstual ringkas.
  Setelah itu, kode dibuat HANYA dari brief tekstual + token.
- Jika butuh detail visual baru, MINTA user menyebutkan elemen spesifik secara teks
  (mis. "warna tombol X", "posisi divider"), JANGAN scan ulang seluruh gambar.
- Untuk layar berikutnya, rujuk brief dengan LABEL pendek (mis. "Auth-Login"),
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
critical  #FEECEC / #DC2626  →  #3A1E1E / #FF6B6B
warning   #FEF6E0 / #D97706  →  #3A3017 / #FFC257
safe      #E8F8EE / #16A34A  →  #16291D / #4ADE80

// === TYPOGRAPHY === Plus Jakarta Sans (fallback Inter). Bobot: Medium/SemiBold/Bold
displayDate     = Bold     30sp
onboardingTitle = Bold     26sp   (rata tengah) — dipakai juga utk judul Auth besar
productName     = SemiBold 17sp
body            = Medium   14sp
countdown       = SemiBold 13sp

// === SHAPE === card/bottomSheet=20dp · button=16dp · chip=Pill · thumbnail=14dp
//               textfield=16dp (samakan dgn button), outlined input border tipis
// === SPACING === base 8pt · screenPadding=20dp · cardInner=16dp · itemGap=12dp
// === ELEVATION === soft: 0 4px 16px rgba(13,103,89,0.08) — hanya tombol utama.
// === ICON === Lucide / Phosphor, outline, stroke 2px, rounded line caps
```

### 🎨 Design Token Brief — Tambahan khusus Auth (≤15 baris, dari gambar)

```
Auth-Login   : Surface card putih full-screen. Logo daun + wordmark "Tickr" di tengah-atas.
               Tombol "Masuk dengan Google" = OUTLINED putih, ikon Google, teks textPrimary.
               Divider "ATAU" (garis tipis + label textSecondary di tengah).
               2 OutlinedTextField: "Masukan Nama Lengkap / Email", "Masukan Password".
               Tombol primer "Masuk" = solid primaryBrand, full-width, radius 16, soft shadow.
               Tombol sekunder "Daftar jika belum punya akun" = OUTLINED primaryBrand full-width.
               Footer kecil: "Dengan melanjutkan, kamu menyetujui Syarat & Ketentuan
               serta Kebijakan Privasi kami." (link bergaris bawah, textSecondary).
Auth-Register: Logo kecil + judul "Daftar Akun Baru" (bold) + subjudul textSecondary.
               3 field BERLABEL di atas input + ikon leading: Nama Lengkap (ikon user),
               Alamat Email (ikon amplop), Kata Sandi (ikon gembok + trailing eye toggle).
               Tombol "Daftar Sekarang" solid primaryBrand full-width. Link "Masuk di sini"
               (primaryBrand) di bawah. Divider "Atau daftar dengan" + tombol Google outlined.
Auth-Success : Ikon ceklis besar lingkaran "safe" hijau, judul "Registrasi Berhasil!"
               (primaryBrand, bold), body textSecondary rata tengah. TRANSIENT — auto navigate.
```

---

## 🗂️ Struktur File Theme & Navigasi (TETAP — sudah dibuat di Onboarding, REUSE)

```
commonMain/ui/theme/   → SUDAH ADA (TickrColors/Typography/Shapes/Spacing/Semantic/Theme).
                          JANGAN buat ulang. Pakai TickrTheme.* accessor.
commonMain/ui/navigation/
├── ComposeNavigator.kt   // sudah ada
└── TickrNavGraph.kt      // TAMBAHKAN composable untuk Login, Register, Success di sini
```

---

## 🔤 Strategi String & Drawable (TETAP)

```
STRING (Compose Multiplatform Resources / strings.commonMain.xml):
- Buat dengan nilai EXAMPLE; tandai setiap entry // TODO(user): sesuaikan.
- Pola key: <screen>_<peran>  (mis. login_cta_submit, register_field_email).
- contentDescription pakai prefix cd_.

DRAWABLE (placeholder):
- Nama deskriptif: ill_<konteks>, ic_<nama> (mis. ic_logo_tickr, ic_google).
- Sebelum aset final masuk, render placeholder Box (primaryBrand alpha 0.1 + label)
  agar kode tetap compile. Tandai // TODO(user): ganti aset final.
```

---

## ✅ Acceptance Criteria Global (TETAP)

```
[ ] Route/Screen split benar; Screen punya @Preview light & dark
[ ] Semua warna/spacing/teks via token & resource (zero hardcode)
[ ] Theme light & dark keduanya benar
[ ] Target ketuk ≥44dp; contentDescription ada
[ ] Navigasi memakai Destination + Navigator (tanpa magic string)
[ ] Gap data/domain/presentation Auth ditambahkan mengikuti pola Phase 1/2/3
```

---

## 📤 Format Output yang Diminta (TETAP)

```
1. Mulai dengan ringkasan 3–5 baris rencana implementasi.
2. Urutan file: gap data → domain → presentation → UI screen (Login, Register, Success).
3. Sertakan @Preview (light + dark) untuk tiap <Feature>Screen.
4. Komentar // TODO(user) di setiap string & drawable example, dan di Google/OAuth.
5. JANGAN implementasi layar lain di luar scope Auth.
```

---
---

# 🟨 BAGIAN SPESIFIK LAYAR — AUTH

## 🧭 Scope Layar Saat Ini  🟨

```
Nama fitur       : Auth
Sub-layar        : Login, Register, RegisterSuccess
Label brief      : Auth-Login, Auth-Register, Auth-Success
Jumlah composable: 3 Route + 3 Screen + komponen reusable
                   (TickrTextField, PasswordTextField dgn validasi, GoogleButton,
                    PasswordStrengthMeter, SuccessAnimation)
Catatan          : HANYA kerjakan fitur Auth. Jangan sentuh Home/ItemList/dll.
                   Google/OAuth = tombol tampil + // TODO, TANPA implementasi.
```

## 🔗 Integrasi dengan Onboarding (sudah selesai)  🟨

```
- Onboarding halaman terakhir, Action.Finish → CompleteOnboardingUseCase()
  lalu emit OnboardingEvent.NavigateToAuth.
- ComposeNavigator menangani NavigateToAuth → navigator.navigate(Destination.Login)
  dengan popUpTo(Destination.Onboarding) { inclusive = true } agar back tidak
  kembali ke onboarding.
- Bila TickrNavGraph memakai handler event terpusat, pastikan rute "onboarding"
  meng-clear backstack saat menuju "login".
```

## 🔍 Analisis Gap Lintas Layer  🟨

> Skema auth berubah (email/nama + password). Tambahkan kontrak berikut mengikuti
> pola Phase 1/2/3. **Mode interactive/slicing: SEMUA repository pakai FAKE in-memory
> (lihat "Mode Interaktif"), TANPA koneksi DB/Supabase.**

```
DATA LAYER:
- AuthRepository {
      suspend fun login(identifier: String, password: String): Result<UserSession>
      suspend fun register(fullName: String, email: String, password: String): Result<UserSession>
      fun observeSession(): Flow<UserSession?>     // utk auto-detect sesi aktif
      suspend fun loginWithGoogle(): Result<UserSession> // TODO(user): OAuth nanti
  }
- UserSession(userId: String, email: String, fullName: String, accessToken: String)
- IMPLEMENTASI SAAT INI: FakeAuthRepository (in-memory):
    * login/register → delay(~1200ms) → emit UserSession dummy → set _session.value.
    * register email yang sudah "terdaftar" (set dummy) → Result.failure(EmailTaken).
    * loginWithGoogle → // TODO(user): Supabase OAuth; sekarang return failure/no-op.
  CATATAN: siapkan SupabaseAuthRepository sebagai // TODO(user) skeleton (komentar
  saja) supaya nanti tinggal swap di DI tanpa mengubah domain/presentation.

DOMAIN LAYER (UseCase tipis, ikuti pola Phase 2):
- LoginUseCase(repo)            → suspend (identifier, password) → Result<UserSession>
- RegisterUseCase(repo)         → suspend (fullName, email, password) → Result<UserSession>
- ObserveSessionUseCase(repo)   → Flow<UserSession?>
- ValidateEmailUseCase()        → ValidationResult (format email)
- ValidatePasswordUseCase()     → PasswordStrength (lihat aturan validasi di bawah)
- ValidateNameUseCase()         → ValidationResult (tidak kosong, ≥2 char)
- Error domain: AuthError { InvalidCredentials, EmailTaken, Network, Unknown }

PRESENTATION LAYER (MVI Phase 3):
// --- Login ---
- LoginUiState(
      identifier: String = "", password: String = "",
      isPasswordVisible: Boolean = false,
      identifierError: UiText? = null, passwordError: UiText? = null,
      isLoading: Boolean = false, isSubmitEnabled: Boolean = false)
- LoginAction: IdentifierChanged(String), PasswordChanged(String),
      TogglePasswordVisibility, Submit, GoogleClicked, NavigateToRegister
- LoginEvent: NavigateToHome, NavigateToRegister, ShowError(UiText)
- LoginViewModel(login, observeSession, validateEmail):
      Submit → validasi → LoginUseCase → sukses: NavigateToHome ; gagal: ShowError.

// --- Register ---
- RegisterUiState(
      fullName: String = "", email: String = "", password: String = "",
      isPasswordVisible: Boolean = false,
      fullNameError: UiText? = null, emailError: UiText? = null,
      passwordStrength: PasswordStrength = PasswordStrength.None,
      isLoading: Boolean = false, isSubmitEnabled: Boolean = false)
- RegisterAction: FullNameChanged(String), EmailChanged(String),
      PasswordChanged(String), TogglePasswordVisibility, Submit,
      GoogleClicked, NavigateToLogin
- RegisterEvent: NavigateToSuccess, NavigateToHome, ShowError(UiText)
- RegisterViewModel(register, validate*):
      Submit → validasi semua → RegisterUseCase → sukses → emit NavigateToSuccess.

// --- Session gate (auto-detect) ---
- ObserveSessionUseCase dipakai di Register/Login ViewModel ATAU di lapisan
  navigasi root: begitu observeSession() emit UserSession != null, arahkan ke Home.
  FLOW SUCCESS: register sukses → sesi aktif otomatis → tampilkan Success
  (≈1.5 dtk animasi) → karena sesi aktif terdeteksi → NavigateToHome (clear backstack).

- Destination.Login    : route = "login"
- Destination.Register : route = "register"
- Destination.AuthSuccess (opsional, transient) : route = "auth_success"
- Destination.Home     : route = "home"  (layar dikerjakan Phase berikutnya — cukup rute)
- DI: factory { LoginViewModel(get(), get(), get()) },
      factory { RegisterViewModel(get(), ...) }, single { FakeAuthRepository() } as AuthRepository
- Routing awal (root): GetOnboardingSeenUseCase + ObserveSessionUseCase →
      belum onboarding → Onboarding ; sudah onboarding & ada sesi → Home ;
      sudah onboarding & belum sesi → Login.

UI LAYER: deliverable utama (3 screen + komponen).
```

### 🔐 Aturan Validasi Password (best practice — TETAP untuk fitur ini)  🟨

```
PasswordStrength { None, Weak, Fair, Good, Strong }  + daftar requirement boolean.
Requirement yang DIVISUALKAN real-time di bawah field Kata Sandi (Register):
  [✓/○] Minimal 8 karakter
  [✓/○] Mengandung huruf besar & kecil
  [✓/○] Mengandung angka
  [✓/○] Mengandung simbol (!@#$%^&* dst)
- Tiap requirement = baris ikon check (animateColor: textSecondary → safe saat terpenuhi).
- Strength meter bar di atas list: segmen/progress yang warnanya beralih
  critical(Weak) → warning(Fair) → secondaryAccent(Good) → safe(Strong),
  lebar animasi (animateFloatAsState). Label kekuatan di samping ("Lemah/Cukup/Baik/Kuat").
- Submit Register hanya enabled bila nama valid, email valid, password ≥ Fair
  (atau sesuaikan: minimal 8 char + 1 angka + 1 huruf besar). // TODO(user): sesuaikan ambang.
- Login: TIDAK menampilkan strength meter (hanya field + toggle eye).
```

## 📱 Spesifikasi Detail Layar  🟨

```
=== Auth-Login (LoginScreen) ===
LAYOUT (scrollable column, screenPadding=20dp, rata tengah horizontal):
  [spacer atas] → [ic_logo_tickr + wordmark, tengah, ~120dp] → [gap besar]
  → GoogleButton "Masuk dengan Google" (outlined, ikon Google, full-width 56dp)
       onClick = LoginAction.GoogleClicked  // TODO(user): OAuth — sekarang no-op
  → Divider berlabel "ATAU" (garis kiri-kanan + teks textSecondary)
  → TickrTextField identifier  (placeholder "Masukan Nama Lengkap / Email")
       value=state.identifier, onValueChange=IdentifierChanged, error=identifierError
  → PasswordTextField password (placeholder "Masukan Password", trailing eye toggle)
       isVisible=state.isPasswordVisible, onToggle=TogglePasswordVisibility
  → [gap] → Tombol "Masuk" (solid primaryBrand, full-width 56dp, radius 16, soft shadow)
       enabled = state.isSubmitEnabled && !isLoading ; isLoading → CircularProgressIndicator
       onClick = LoginAction.Submit
  → Tombol "Daftar jika belum punya akun" (outlined primaryBrand, full-width 56dp)
       onClick = LoginAction.NavigateToRegister
  → [weight spacer] → Footer "Dengan melanjutkan, kamu menyetujui Syarat & Ketentuan
       serta Kebijakan Privasi kami." (textSecondary kecil, link underline; klik =
       // TODO(user): buka URL S&K / Privasi)
ANIMASI: tombol pressed scale 0.97x; error field → border critical + helper text.

=== Auth-Register (RegisterScreen) ===
LAYOUT (scrollable column, screenPadding=20dp):
  [ic_logo_tickr kecil, tengah] → judul "Daftar Akun Baru" (bold, tengah)
  → subjudul "Mulai kelola persediaan rumah Anda dengan lebih mudah." (textSecondary, tengah)
  → [gap] FIELD BERLABEL (label di atas input):
     "Nama Lengkap"  → TickrTextField leadingIcon=ic_user, placeholder "Contoh: Budi Santoso"
     "Alamat Email"  → TickrTextField leadingIcon=ic_mail, placeholder "nama@email.com",
                        keyboardType=Email, error=emailError
     "Kata Sandi"    → PasswordTextField leadingIcon=ic_lock, placeholder "Min. 8 karakter",
                        trailing eye toggle
        → DI BAWAHNYA: PasswordStrengthMeter (bar + 4 requirement, animasi sesuai aturan)
  → [gap] Tombol "Daftar Sekarang" (solid primaryBrand full-width 56dp, radius 16)
       enabled=isSubmitEnabled && !isLoading ; isLoading → spinner ; onClick=Submit
  → Baris "Sudah punya akun? Masuk di sini" (teks textSecondary + link primaryBrand)
       link onClick = RegisterAction.NavigateToLogin
  → Divider "Atau daftar dengan" → GoogleButton outlined "Google"
       onClick = GoogleClicked // TODO(user): OAuth — no-op
ANIMASI: requirement check & strength bar beranimasi saat mengetik.

=== Auth-Success (RegisterSuccessScreen) — TRANSIENT ===
LAYOUT (column, tengah penuh, background):
  [SuccessAnimation: lingkaran "safe" + ikon ceklis, scale-in + draw, ~120dp]
  → judul "Registrasi Berhasil!" (primaryBrand, bold, tengah)
  → body "Akunmu sudah siap. Sekarang kamu bisa mulai mencatat barang dan memantau
     tanggal kedaluwarsa agar tidak ada lagi yang mubazir." (textSecondary, tengah)
PERILAKU: LaunchedEffect(Unit) { delay(~1500ms); onAction(Finish) } ATAU langsung
  bergantung pada ObserveSessionUseCase → karena sesi aktif terdeteksi otomatis →
  RegisterEvent.NavigateToHome (popUpTo seluruh auth, inclusive=true).
  Catatan: tidak ada tombol; murni feedback + auto-redirect.

AKSESIBILITAS (semua): contentDescription pada logo, ikon Google, eye toggle,
  ceklis sukses; target ≥44dp; kontras WCAG AA light & dark.
```

## 🎞️ Mode Interaktif / Slicing (TANPA database)  🟨

```
TUJUAN: alur penuh dapat dijalankan & "berhasil" memakai FakeAuthRepository, tanpa
DB/Supabase. Semua hasil sukses/gagal disimulasikan.

PERILAKU SIMULASI:
- Login Submit: delay ~1200ms → bila identifier == "demo@tickr.app" & password apa pun
  ≥8 char → sukses (set sesi) → NavigateToHome ; selain itu → AuthError.InvalidCredentials
  → ShowError (snackbar/inline). // TODO(user): ganti aturan saat Supabase masuk.
- Register Submit: delay ~1200ms → bila email != "taken@tickr.app" → sukses → set sesi →
  NavigateToSuccess → (sesi terdeteksi) → NavigateToHome. Email "taken@tickr.app"
  → AuthError.EmailTaken.
- Google: no-op murni + // TODO(user).
- Home belum dibuat: sediakan placeholder composable HomePlaceholderScreen
  (teks "Home (Phase berikutnya)") agar navigasi sukses tidak crash. // TODO(user): hapus saat Home siap.

ANIMASI NAVIGASI / TRANSISI (best practice Compose Navigation):
- Gunakan NavHost dengan enterTransition/exitTransition konsisten:
  * Login ↔ Register : slideInHorizontally/slideOutHorizontally (push) + fade,
    spec tween(300, FastOutSlowInEasing). Maju ke kanan, kembali ke kiri.
  * → Success : fadeIn + scaleIn(0.92→1f) (tween 300) ; keluar fadeOut.
  * Success → Home : fadeIn + slideInVertically kecil ; popUpTo("login"){inclusive=true}
    membersihkan seluruh backstack auth.
- Tombol primer: Modifier scale via animateFloatAsState saat pressed (0.97x).
- Loading state: crossfade konten tombol (teks ↔ CircularProgressIndicator).
- Strength meter & requirement: animateColorAsState + animateFloatAsState.
- Hormati prefers-reduced-motion bila tersedia; jangan animasi >350ms.
```

## 🔤 String & Drawable Example untuk Layar Ini  🟨

```xml
<!-- TODO(user): sesuaikan semua nilai di bawah -->
<!-- Login -->
<string name="login_google_cta">Masuk dengan Google</string>
<string name="login_divider_or">ATAU</string>
<string name="login_field_identifier_hint">Masukan Nama Lengkap / Email</string>
<string name="login_field_password_hint">Masukan Password</string>
<string name="login_cta_submit">Masuk</string>
<string name="login_cta_register">Daftar jika belum punya akun</string>
<string name="login_footer_terms">Dengan melanjutkan, kamu menyetujui Syarat &amp; Ketentuan serta Kebijakan Privasi kami.</string>
<string name="login_error_invalid">Email/nama atau kata sandi salah.</string>

<!-- Register -->
<string name="register_title">Daftar Akun Baru</string>
<string name="register_subtitle">Mulai kelola persediaan rumah Anda dengan lebih mudah.</string>
<string name="register_label_name">Nama Lengkap</string>
<string name="register_hint_name">Contoh: Budi Santoso</string>
<string name="register_label_email">Alamat Email</string>
<string name="register_hint_email">nama@email.com</string>
<string name="register_label_password">Kata Sandi</string>
<string name="register_hint_password">Min. 8 karakter</string>
<string name="register_cta_submit">Daftar Sekarang</string>
<string name="register_have_account">Sudah punya akun? </string>
<string name="register_login_link">Masuk di sini</string>
<string name="register_divider_or">Atau daftar dengan</string>
<string name="register_google_cta">Google</string>
<string name="register_error_email_taken">Email ini sudah terdaftar.</string>

<!-- Password requirements -->
<string name="pw_req_length">Minimal 8 karakter</string>
<string name="pw_req_case">Huruf besar &amp; kecil</string>
<string name="pw_req_digit">Mengandung angka</string>
<string name="pw_req_symbol">Mengandung simbol</string>
<string name="pw_strength_weak">Lemah</string>
<string name="pw_strength_fair">Cukup</string>
<string name="pw_strength_good">Baik</string>
<string name="pw_strength_strong">Kuat</string>

<!-- Success -->
<string name="auth_success_title">Registrasi Berhasil!</string>
<string name="auth_success_body">Akunmu sudah siap. Sekarang kamu bisa mulai mencatat barang dan memantau tanggal kedaluwarsa agar tidak ada lagi yang mubazir.</string>

<!-- contentDescription -->
<string name="cd_logo_tickr">Logo Tickr</string>
<string name="cd_google_icon">Ikon Google</string>
<string name="cd_toggle_password">Tampilkan atau sembunyikan kata sandi</string>
<string name="cd_success_check">Ikon registrasi berhasil</string>
```

```
DRAWABLE (placeholder — TODO(user): ganti aset final):
- ic_logo_tickr     (logo daun + wordmark Tickr)
- ic_google         (logo Google warna)
- ic_user           (ikon profil, leading Nama Lengkap)
- ic_mail           (ikon amplop, leading Email)
- ic_lock           (ikon gembok, leading Kata Sandi)
- ic_eye / ic_eye_off (toggle visibilitas password)
- ic_success_check  (ceklis dalam lingkaran — atau pakai Canvas/animasi vector)
```

## ✅ Acceptance Criteria Spesifik Layar Ini  🟨

```
[ ] LoginScreen: field identifier + password + toggle eye, tombol Masuk & Daftar,
    Google outlined (// TODO OAuth), footer S&K. @Preview light & dark.
[ ] RegisterScreen: 3 field berlabel + ikon leading, strength meter + 4 requirement
    beranimasi real-time, tombol Daftar Sekarang, link "Masuk di sini", Google outlined.
[ ] RegisterSuccessScreen transient: animasi ceklis, auto-navigate ke Home (clear backstack).
[ ] Onboarding "Lanjutkan" → Login (popUpTo onboarding inclusive).
[ ] FLOW: Register sukses → sesi aktif (Fake) → Success → Home tanpa DB.
[ ] Login demo (demo@tickr.app) sukses → Home; kredensial lain → error inline/snackbar.
[ ] Google/OAuth = tombol tampil + // TODO(user), tidak memanggil apa pun.
[ ] Transisi navigasi (slide Login↔Register, scale/fade ke Success) berjalan halus.
[ ] Submit disabled saat field invalid / saat loading; loading tampil spinner.
[ ] Gap data/domain/presentation Auth ditambahkan; SupabaseAuthRepository = skeleton TODO.
[ ] Zero hardcode warna/string; token & resource dipakai; ≥44dp; WCAG AA light & dark.
```

---

## 🧩 Catatan Swap ke Supabase (nanti, TETAP sebagai TODO)

```
- Ganti FakeAuthRepository → SupabaseAuthRepository di authModule (DI) saja;
  domain & presentation TIDAK berubah (itu sebabnya kontrak Result<UserSession> dipakai).
- register()/login() memanggil gotrue (supabase-kt). observeSession() map dari
  sessionStatus Flow Supabase → UserSession?. loginWithGoogle() pakai OAuth provider.
- Semua // TODO(user) Google/OAuth diaktifkan di tahap ini.
```
