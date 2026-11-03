# 📋 Prompt — Tickr Phase 3.5 · Beranda (Home) + Tambah Barang (Add Item Bottom Sheet)

> **Cara pakai:** Kirim seluruh dokumen ini ke agent. Dokumen sudah final-isi
> (bukan template kosong) untuk **dua layar terkait**: `Home` (deliverable utama)
> dan `AddItem` (bottom sheet modal yang dipicu FAB tengah bottom nav).
> Constraint global, design token, theme, dan strategi anti-token-leak bersifat
> **TETAP** dan tidak boleh diubah.

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
Jika ada GAP kontrak (State/Action/Event/UseCase/Repo) untuk layar ini, TAMBAHKAN
mengikuti pola Phase 1/2/3 — JANGAN merombak yang sudah ada.
```

---

## ⚠️ Aturan Wajib / Constraints (TETAP)

```
1. SEMUA UI di commonMain (Compose Multiplatform). Tidak ada kode UI di
   androidMain/iosMain kecuali expect/actual untuk platform-specific
   (safe-area/insets, font loader, image/camera picker, date picker native,
   share, dsb).
2. Pola tiap layar WAJIB dua lapis:
   - <Feature>Route   → stateful: koinViewModel + collectAsState + LaunchedEffect(events)
   - <Feature>Screen  → stateless: hanya (state, (Action) -> Unit), wajib @Preview-able
3. Zero hardcode visual: SEMUA warna/spacing/ukuran/bentuk lewat design tokens
   (TickrTheme.colors / .spacing / .typography / .shapes). Tidak ada Color(0x..),
   .dp magic number, atau MaterialTheme default yang menimpa token.
4. Zero hardcode string: semua teks via resource (lihat "Strategi String & Drawable").
5. Patuhi kontrak existing: Destination, Navigator, UiText.asString(), UiEvent.
6. Target ketuk minimal 44x44dp. contentDescription wajib utk ikon/gambar bermakna.
7. Kerjakan PER SCREEN sesuai blok "Scope Layar Saat Ini". Boleh menggarap Home
   dan AddItem dalam satu sesi karena AddItem dipicu dari Home, TAPI tetap
   pisahkan file Route/Screen/komponen masing-masing. JANGAN buat layar lain
   (Auth/Kadaluwarsa/Wawasan/Profil) selain shell bottom nav-nya.
8. Setiap nilai string & nama drawable diberi komentar // TODO(user): sesuaikan.
9. Hitungan turunan (countdown sisa waktu, urgensi traffic-light) dihitung di
   DOMAIN/PRESENTATION dari expiry_date — JANGAN disimpan di DB, JANGAN hardcode di UI.
```

---

## 🔒 Strategi Anti-Kebocoran Token Saat Membaca Gambar (TETAP)

```
ATURAN PEMBACAAN GAMBAR:
- JANGAN load/parse file gambar mentah ke konteks untuk men-generate kode.
- Gambar HANYA dideskripsikan SEKALI sebagai "Design Token Brief" tekstual ringkas.
  Setelah itu, kode dibuat HANYA dari brief tekstual + token.
- Jika butuh detail visual baru, MINTA user menyebutkan elemen spesifik secara teks
  (mis. "warna badge X", "posisi notch FAB"), JANGAN scan ulang seluruh gambar.
- Untuk layar berikutnya, rujuk brief dengan LABEL pendek (mis. "Home-card-donut"),
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

// Warna kategori donut (selaraskan dgn categories.color_hex jika ada):
//   Makanan & Minuman = primaryBrand · Kecantikan = secondaryAccent
//   Obat & Vitamin = #5EC9B7 (teal muda) · Lainnya = textSecondary alpha .35

// === TYPOGRAPHY === Plus Jakarta Sans (fallback Inter). Bobot: Medium/SemiBold/Bold
displayClock    = Bold     17sp   (jam realtime di TopBar, tabular figures)
greetingTitle   = Bold     26sp   ("Selamat Datang, Sarah")
sectionTitle    = Bold     18sp   ("Kategori Konsumsi", "Produk Kadaluwarsa")
donutTotalNum   = Bold     24sp   (angka TOTAL di tengah donut)
productName     = SemiBold 17sp
body            = Medium   14sp
countdown       = SemiBold 12sp   (warna ikut urgensi)
caption         = Medium   12sp   (footer, tanggal, label legend)

// === SHAPE === card/bottomSheet=20dp · button=16dp · chip=Pill · thumbnail=14dp
// === SPACING === base 8pt · screenPadding=20dp · cardInner=16dp · itemGap=12dp
// === ELEVATION === soft: 0 4px 16px rgba(13,103,89,0.08) — tombol utama & FAB.
//                   Card pakai border tipis / flat elevation.
// === ICON === Lucide / Phosphor, outline, stroke 2px, rounded line caps
//   home, history(rotate-ccw), trending-up(wawasan), user(profil), plus(FAB),
//   bell, chevron-right, camera, trending-down(insight), x(close sheet)
```

---

## 🗂️ Struktur File Theme & Navigasi (TETAP — buat sekali, reuse semua screen)

```
commonMain/ui/theme/
├── TickrColors.kt        // data class TickrColors + TickrLightColors / TickrDarkColors
├── TickrTypography.kt    // Typography Material3 + custom (displayClock, donutTotalNum, dst)
├── TickrShapes.kt        // 20/16/14dp, Pill
├── TickrSpacing.kt       // data class TickrSpacing (token spacing)
├── SemanticColors.kt     // mapping critical/warning/safe + warna kategori donut
└── TickrTheme.kt         // CompositionLocal + TickrTheme {} wrapper, isSystemInDarkTheme()

commonMain/ui/navigation/
├── ComposeNavigator.kt   // implementasi interface Navigator (Phase 3)
├── TickrNavGraph.kt      // NavHost root
└── MainShell.kt          // Scaffold bottom nav + FAB notch (host tab destinations)
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

object TickrTheme {
    val colors: TickrColors      @Composable get() = LocalTickrColors.current
    val spacing: TickrSpacing    @Composable get() = LocalTickrSpacing.current
    val typography: TickrTypography @Composable get() = LocalTickrTypography.current
}
```

---

## 🔤 Strategi String & Drawable (TETAP)

```
STRING (Compose Multiplatform Resources / strings.commonMain.xml):
- Buat dengan nilai EXAMPLE; tandai setiap entry // TODO(user): sesuaikan.
- Pola key: <screen>_<peran>  (mis. home_greeting, additem_cta_save).
- contentDescription pakai prefix cd_  (mis. cd_home_notification).
- String dinamis (jumlah hari, %): gunakan plural/format param, JANGAN gabung string.

DRAWABLE (placeholder):
- Nama deskriptif: ill_<konteks>, ic_<nama>  (mis. ic_home_tab, ill_empty_items).
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
[ ] Tidak ada perubahan pada ViewModel/State/Action/Event Phase 3 yg sudah ada
[ ] Gap data/domain/presentation (bila ada) ditambahkan mengikuti pola Phase 1/2/3
[ ] Navigasi memakai Destination + Navigator (tanpa magic string)
```

---

## 📤 Format Output yang Diminta (TETAP)

```
1. Mulai dengan ringkasan 3–5 baris rencana implementasi.
2. Urutan file: theme (jika belum) → migrasi/gap DB → gap domain/presentation
   → MainShell (bottom nav+FAB) → Home Route/Screen + komponen → AddItem sheet.
3. Sertakan @Preview (light + dark) untuk setiap stateless Screen/komponen utama,
   termasuk preview state KOSONG (empty) untuk Home.
4. Komentar // TODO(user) di setiap string & drawable example.
5. JANGAN implementasi layar lain di luar Scope Layar Saat Ini.
```

---
---

# 🟨 BAGIAN SPESIFIK LAYAR (SUDAH DIISI)

## 🧭 Scope Layar Saat Ini

```
Nama layar       : Home (Beranda)  +  AddItem (Tambah Barang — modal bottom sheet)
Label brief      : Home-topbar, Home-insight, Home-donut, Home-expiring,
                   Home-bottomnav, AddItem-sheet
Jumlah composable:
  - MainShell                 (Scaffold + BottomBar 4 item + FAB tengah/notch)
  - HomeRoute + HomeScreen
    · TickrTopBar             (logo + tanggal/jam realtime + bell)
    · GreetingHeader
    · WasteInsightCard        (kartu "tingkat sampah turun X%")
    · ConsumptionDonutCard    (donut chart + legend) ← WAJIB sama dgn desain
    · ExpiringSection         (header + "PERLU TINDAKAN" + ringkasan kritis)
    · CategoryGroupHeader     (titik warna + nama kategori uppercase)
    · ExpiringItemRow         (thumbnail + nama + tanggal + badge countdown + chevron)
    · HomeEmptyState          (state ketika belum ada item sama sekali)
  - AddItemRoute + AddItemSheet (ModalBottomSheet)
    · PhotoPickerBox · CategoryChipGroup · QuantityUnitRow · ExpiryDateField
Catatan          : HANYA kerjakan layar ini + shell bottom nav. Tab lain cukup
                   placeholder Box "Coming soon" agar NavHost kompilasi.
```

---

## 🔍 Analisis Gap Lintas Layer  (PENTING — baca teliti)

> Hasil cross-check skema DB (gambar 2) terhadap kebutuhan UI Beranda + Tambah Barang.
> Skema saat ini: `items`, `categories`, `profiles`, `notifications_log`.

### 🟥 DATA LAYER — GAP WAJIB DITAMBAH

```
1) MIGRASI TABEL `items` — kolom HILANG untuk form Tambah Barang:
   ALTER TABLE items ADD COLUMN quantity  integer NOT NULL DEFAULT 1;
   ALTER TABLE items ADD COLUMN unit      text    NOT NULL DEFAULT 'Pcs';
   (Form punya field "Jumlah & Satuan" → tidak ada padanan kolom saat ini.)

2) AGREGASI DONUT "Kategori Konsumsi" → JANGAN query GROUP BY berulang dari client.
   Buat Postgres VIEW (read-optimized, hormati RLS via auth.uid()):

   CREATE VIEW v_category_consumption AS
   SELECT
     i.user_id,
     c.id            AS category_id,
     c.name          AS category_name,
     c.color_hex,
     COUNT(*)        AS total_items,
     ROUND(100.0 * COUNT(*) / NULLIF(SUM(COUNT(*)) OVER (PARTITION BY i.user_id), 0), 0) AS percentage
   FROM items i
   JOIN categories c ON c.id = i.category_id
   WHERE i.is_consumed = false           -- "stok aktif" untuk donut
   GROUP BY i.user_id, c.id, c.name, c.color_hex;

3) INSIGHT "tingkat sampah turun X%" → butuh perbandingan antar bulan.
   Tidak ada kolom status "terbuang vs terpakai" eksplisit. REKOMENDASI:
   - Tambah enum/flag pembeda: items.disposal_status text
     CHECK (disposal_status IN ('active','consumed','wasted')) DEFAULT 'active';
     (lebih kaya dari boolean is_consumed; is_consumed bisa tetap utk back-compat).
   - Buat RPC agregasi bulanan:
     CREATE FUNCTION get_waste_trend(p_user uuid)
     RETURNS TABLE(this_month int, last_month int, delta_pct numeric) ...
   - Jika belum mau ubah skema, UI tetap dibuat tetapi WasteInsightCard
     menampilkan state "belum cukup data" (lihat Spesifikasi).

CATATAN COUNTDOWN: TIDAK perlu kolom baru. "0 hari 4 jam" dihitung dari
expiry_date di domain. Untuk presisi jam, pertimbangkan ubah items.expiry_date
dari `date` → `timestamptz` (opsional; jika tetap date, jam ditampilkan 00:00).
⚠️ Konfirmasi ke user: badge di desain menampilkan JAM ("0 hari 4 jam"),
   sedangkan kolom saat ini bertipe `date`. Tanyakan apakah perlu timestamptz.
```

### 🟦 DOMAIN LAYER (UseCase tipis, ikuti pola Phase 2)

```
- GetHomeDashboardUseCase()      → Flow<HomeDashboard> (gabungan stok+donut+insight)
- ObserveExpiringItemsUseCase()  → Flow<List<ExpiringItem>> (urut by sisa waktu asc)
- GetCategoryConsumptionUseCase()→ Flow<List<CategorySlice>> (dari v_category_consumption)
- GetWasteTrendUseCase()         → Flow<WasteTrend?>  (null = data belum cukup)
- AddItemUseCase(params)         → suspend Result<Unit>
- GetCategoriesUseCase()         → Flow<List<Category>> (isi chip kategori di sheet)

Model domain baru:
- ExpiringItem(id, name, categoryName, expiryDate, imageUrl?, quantity, unit)
- Urgency { CRITICAL, WARNING, SAFE }  // fungsi murni dari sisa hari
- CategorySlice(categoryId, name, colorHex, percentage, count)
- WasteTrend(deltaPercent: Int, isImproving: Boolean)
```

### 🟩 PRESENTATION LAYER (pola MVI Phase 3)

```
HOME:
- HomeUiState(
    isLoading, userName, nowEpochMillis (tick tiap 1s utk jam),
    wasteTrend: WasteTrend?,
    slices: List<CategorySlice>, totalItems: Int,
    expiringGroups: List<CategoryGroup>, criticalCount: Int,
    isEmpty: Boolean   // true bila tdk ada item sama sekali
  )
- HomeAction: Refresh, OpenNotifications, SeeAllCategories, OpenItem(id),
              OpenAddItem, TabSelected(Destination)
- HomeEvent : NavigateToItemDetail(id), NavigateToNotifications, ShowAddItemSheet
- HomeViewModel: kombinasikan use case di atas; jam realtime pakai flow ticker.
- Destination.Home : Destination { override val route = "home" }

ADD ITEM (bottom sheet):
- AddItemUiState(name, categories, selectedCategoryId, quantity, unit,
                 expiryDateMillis?, photoPath?, isSaving, canSave)
- AddItemAction: NameChanged, CategorySelected(id), QtyChanged, UnitChanged,
                 ExpiryPicked(millis), PhotoPicked(path?), AddCustomCategory, Save, Dismiss
- AddItemEvent : Saved, Dismissed, ShowError(UiText)
- AddItemViewModel(addItem, getCategories): Save → AddItemUseCase → emit Saved
- Destination.AddItem : modal (bukan tab); ditampilkan sbg ModalBottomSheet di MainShell.

DI: factory { HomeViewModel(...) } & factory { AddItemViewModel(...) } di presentationModule.
```

---

## 📱 Spesifikasi Detail Layar

### A. MainShell — Bottom Nav + FAB (Home-bottomnav)

```
- Scaffold dgn bottomBar = TickrBottomBar; FAB tengah MENONJOL (notch).
- 4 tab: BERANDA(home), KADALUWARSA(history/rotate-ccw), [GAP FAB], WAWASAN(trending-up), PROFIL(user).
  Urutan visual: Beranda · Kadaluwarsa · (FAB) · Wawasan · Profil.
- FAB: lingkaran primaryBrand Ø56dp, ikon plus putih, soft shadow, naik ~28dp di atas bar.
  Klik FAB = HomeAction.OpenAddItem → tampilkan AddItemSheet (BUKAN push halaman).
- Tab aktif: ikon+label primaryBrand; inaktif: textSecondary. Label caption 11–12sp.
- Pakai NavigationBar Material3 dgn slot tengah kosong utk notch, atau custom Row.
- Hormati safe-area/insets bawah (expect/actual jika perlu).
```

### B. AddItem — Transisi & Best-Practice Navigasi (AddItem-sheet)

```
- Gunakan ModalBottomSheet (Material3) dgn:
  · sheetState rememberModalBottomSheetState(skipPartiallyExpanded = true)
  · drag handle bawaan (BottomSheetDefaults.DragHandle) — sesuai garis abu di desain
  · shape = TickrShapes.bottomSheet (sudut atas 20dp), containerColor = surface
  · scrim gelap di belakang; klik scrim / drag-down / tombol X = AddItemAction.Dismiss
- ANIMASI: ModalBottomSheet sudah slide-up + fade scrim secara default (best practice
  Material3). FAB→sheet TIDAK perlu shared-element; cukup show/hide sheet via state.
  Saat dismiss, sheetState.hide() lalu set show=false (animasi turun mulus).
- Hindari Dialog full-screen utk form ini; bottom sheet = pola standar mobile utk
  quick-add yg tetap memberi konteks layar Home di belakang.
- Konten sheet (scrollable, urut atas→bawah):
  1. Header: judul "Tambah Barang" (sectionTitle) + tombol X bulat kanan-atas (≥44dp).
  2. PhotoPickerBox: kotak dashed radius 20dp, ikon kamera + "Ketuk untuk unggah foto".
     Kosong = placeholder; terisi = preview. expect/actual image picker.
  3. TextField "Nama Produk" (outline tipis, radius 16dp, placeholder textSecondary).
  4. Label "Kategori" + CategoryChipGroup (wrap): chip terpilih = primaryBrand solid
     teks putih; lainnya = surface + border tipis. Chip terakhir "+ Lainnya" memicu
     AddCustomCategory. Chip diisi dari GetCategoriesUseCase (BUKAN hardcode).
  5. Panel "Jumlah & Satuan" (background abu lembut radius 16dp): dua field
     berdampingan → kiri angka (quantity, default 1), kanan satuan (unit, default "Pcs").
  6. Label "Waktu Kedaluwarsa" + ExpiryDateField placeholder "DD / MM / YYYY";
     ketuk = buka date picker native (expect/actual). Simpan sbg millis.
  7. Tombol "Catat Barang" + ikon check: full-width-ish, h=56dp, radius 16dp,
     primaryBrand, soft shadow. Disable saat !canSave / isSaving (tampilkan loader).
- Sukses: AddItemEvent.Saved → tutup sheet + refresh Home (list & donut ter-update).
```

### C. Home — Layout (atas → bawah)

```
TOPBAR (Home-topbar):
- Kiri: wordmark "Tickr" (primaryBrand, greeting-ish bold) + di kanannya tanggal
  ("Selasa, 9 Juni 2026", caption textSecondary) & jam realtime ("18:27:08",
  displayClock, primaryBrand, tabular). Jam update tiap detik dari state ticker.
- Kanan: ikon bell (≥44dp) → HomeAction.OpenNotifications. (Badge unread opsional.)

GREETING (Home greeting):
- "Selamat Datang, {nama}" (greetingTitle) + subjudul "Cek status barangmu hari ini."
  (body, textSecondary). Nama dari profiles.full_name.

INSIGHT CARD (Home-insight):
- Card surface radius 20dp, border tipis. Kiri: lingkaran soft hijau (safe.bg) berisi
  ikon trending-down hijau. Kanan: teks "Bagus sekali! Bulan ini tingkat makanan
  terbuangmu turun {X}%." dgn angka % di-highlight hijau (safe.text) bold, lalu
  baris kecil "Terus pertahankan kebiasaan baik ini." (textSecondary).
- STATE DATA BELUM CUKUP (wasteTrend == null): tampilkan varian netral —
  ikon info, teks "Catat & habiskan barang untuk melihat tren penghematanmu."
  (jangan tampilkan angka palsu).

DONUT CARD (Home-donut) ⭐ WAJIB SAMA DENGAN DESAIN:
- Header baris: "Kategori Konsumsi" (sectionTitle) kiri + "Lihat Semua"
  (primaryBrand, body, klik=SeeAllCategories) kanan.
- Body baris: KIRI = donut chart, KANAN = legend.
- DONUT SPEC (gambar Canvas, BUKAN library berat):
  · Ring donut (bukan pie penuh): stroke tebal ± 22dp, innerRadius besar (hole lebar).
  · Cap antar-segmen BUTT (rapat) ATAU beri gap kecil ~2–3dp antar segmen.
  · Mulai dari sudut atas (-90°), searah jarum jam.
  · Urutan & warna segmen ikut data slices (Makanan=primaryBrand, Kecantikan=
    secondaryAccent, Obat=teal #5EC9B7, Lainnya=abu). Sudut tiap segmen = pct*3.6°.
  · Pusat donut: label "TOTAL" (caption, textSecondary) di atas angka total
    (donutTotalNum, textPrimary) — center vertikal.
  · Animasi sweep saat pertama tampil (animateFloatAsState 0→target) opsional.
- LEGEND (kanan): tiap baris = dot warna kategori + nama kategori (body) +
  persentase rata-kanan (body SemiBold). 4 baris sesuai slices.
- Hitung sudut & persen MURNI dari data; jangan hardcode 50/30/20/10.

EXPIRING SECTION (Home-expiring):
- Header: "Produk Kadaluwarsa" (sectionTitle) kiri + badge pill "PERLU TINDAKAN"
  (critical.bg / critical.text, caption, uppercase) kanan.
- Sub-ringkasan: "Kritis: {n} ({Makanan & Minuman a, Kecantikan b, Obat & Vitamin c})"
  (caption, textSecondary). Angka dari criticalCount + breakdown.
- Dikelompokkan per kategori. CategoryGroupHeader: dot kecil warna kategori +
  NAMA KATEGORI uppercase (caption SemiBold, letter-spacing).
- ExpiringItemRow (card surface radius 14–20dp, klik=OpenItem(id)):
  · Kiri: thumbnail 14dp (image_url; jika null → placeholder abu).
  · Tengah: productName (SemiBold) + baris kecil "{kategori} • {tgl expiry}" (caption).
  · Kanan-atas: BADGE COUNTDOWN pill — teks "{X} hari {Y} jam" / "{bulan} bulan ..."
    warna ikut Urgency (critical/warning/safe bg+text). Badge bisa wrap 2 baris.
  · Paling kanan: chevron-right (textSecondary).
- Urut item by sisa waktu menaik (paling kritis di atas) dalam tiap grup.

EMPTY STATE (HomeEmptyState) — saat isEmpty == true (DB kosong / user baru):
- Sembunyikan donut & expiring section. Tampilkan ilustrasi ill_empty_items
  (placeholder), judul "Belum ada barang" (sectionTitle), body
  "Mulai catat barangmu agar tak ada yang kedaluwarsa terlewat."
  (textSecondary, rata tengah), lalu tombol "Tambah Barang Pertama"
  (primaryBrand, radius 16dp) → HomeAction.OpenAddItem.
- TopBar, greeting, dan FAB tetap tampil. Insight card → varian "belum cukup data".
- Sediakan @Preview khusus state ini.

AKSESIBILITAS: contentDescription pada bell, FAB, thumbnail, ikon insight, chevron.
  Donut diberi contentDescription ringkas ("Komposisi kategori: Makanan 50% ...").
ANIMASI: tab switch via NavHost default; FAB pressed scale 0.95x; sheet slide-up
  default Material3; donut sweep animasi masuk.
```

---

## 🔤 String & Drawable Example untuk Layar Ini

```xml
<!-- TODO(user): sesuaikan semua nilai di bawah -->
<!-- HOME -->
<string name="home_greeting">Selamat Datang, %1$s</string>
<string name="home_subtitle">Cek status barangmu hari ini.</string>
<string name="home_insight_text">Bagus sekali! Bulan ini tingkat makanan terbuangmu turun %1$d%%.</string>
<string name="home_insight_sub">Terus pertahankan kebiasaan baik ini.</string>
<string name="home_insight_empty">Catat &amp; habiskan barang untuk melihat tren penghematanmu.</string>
<string name="home_consumption_title">Kategori Konsumsi</string>
<string name="home_see_all">Lihat Semua</string>
<string name="home_donut_total_label">TOTAL</string>
<string name="home_expiring_title">Produk Kadaluwarsa</string>
<string name="home_need_action">PERLU TINDAKAN</string>
<string name="home_critical_summary">Kritis: %1$d (%2$s)</string>
<string name="home_empty_title">Belum ada barang</string>
<string name="home_empty_body">Mulai catat barangmu agar tak ada yang kedaluwarsa terlewat.</string>
<string name="home_empty_cta">Tambah Barang Pertama</string>
<!-- countdown (gunakan plural resource utk hari/jam/bulan) -->
<string name="countdown_day_hour">%1$d hari %2$d jam</string>
<string name="countdown_month_day_hour">%1$d bulan %2$d hari %3$d jam</string>
<!-- bottom nav -->
<string name="nav_home">Beranda</string>
<string name="nav_history">Kadaluwarsa</string>
<string name="nav_insight">Wawasan</string>
<string name="nav_profile">Profil</string>

<!-- ADD ITEM -->
<string name="additem_title">Tambah Barang</string>
<string name="additem_photo_hint">Ketuk untuk unggah foto</string>
<string name="additem_name_hint">Nama Produk</string>
<string name="additem_category_label">Kategori</string>
<string name="additem_category_more">Lainnya</string>
<string name="additem_qty_unit_label">Jumlah &amp; Satuan</string>
<string name="additem_unit_default">Pcs</string>
<string name="additem_expiry_label">Waktu Kedaluwarsa</string>
<string name="additem_expiry_hint">DD / MM / YYYY</string>
<string name="additem_cta_save">Catat Barang</string>

<!-- contentDescription -->
<string name="cd_home_notification">Buka notifikasi</string>
<string name="cd_home_add">Tambah barang baru</string>
<string name="cd_additem_close">Tutup</string>
<string name="cd_additem_photo">Unggah foto produk</string>
<string name="cd_item_chevron">Lihat detail barang</string>
```

```
DRAWABLE (placeholder — TODO(user): ganti aset final):
- ill_empty_items     (ilustrasi state kosong beranda)
- ic_home, ic_history, ic_insight, ic_profile, ic_plus  (bottom nav + FAB)
- ic_bell, ic_chevron_right, ic_camera, ic_trending_down, ic_check, ic_close
- placeholder thumbnail item (Box abu radius 14dp) bila image_url null
```

---

## ✅ Acceptance Criteria Spesifik Layar Ini

```
[ ] MainShell: bottom nav 4 tab + FAB tengah notch; FAB membuka AddItemSheet
[ ] AddItem tampil sbg ModalBottomSheet (slide-up + scrim), dismiss via X/drag/scrim
[ ] Donut chart Home SAMA dgn desain: ring berlubang, TOTAL di tengah, legend %,
    sudut & warna dihitung dari data (bukan hardcode 50/30/20/10)
[ ] Expiring list dikelompokkan per kategori; badge countdown berwarna sesuai Urgency
[ ] Jam di TopBar update realtime tiap detik dari state
[ ] HomeEmptyState muncul saat tidak ada item (DB kosong) + tombol tambah pertama
[ ] WasteInsightCard punya varian "data belum cukup" (tanpa angka palsu)
[ ] Migrasi DB items.quantity + items.unit ditambahkan
[ ] VIEW v_category_consumption (atau RPC setara) disediakan utk donut
[ ] Form AddItem menyimpan via AddItemUseCase → Saved → Home ter-refresh
[ ] Gap data/domain/presentation Home & AddItem ditambahkan mengikuti Phase 1/2/3
[ ] @Preview light & dark untuk HomeScreen (state isi + state kosong) & AddItemSheet
```

---

## ❓ Konfirmasi yang Diminta ke User SEBELUM Coding

```
1. expiry_date bertipe `date`, tapi badge desain menampilkan JAM ("0 hari 4 jam").
   → Ubah ke timestamptz, atau cukup tampilkan hari saja?
2. Insight "sampah turun %": setuju tambah kolom disposal_status
   ('active'/'consumed'/'wasted') + RPC tren bulanan? Atau tampilkan varian
   "belum cukup data" dulu tanpa ubah skema?
3. Satuan (unit) di form: free-text atau dropdown daftar tetap (Pcs/Gram/Liter/...)?
4. Tab "Kadaluwarsa" & "Wawasan": cukup placeholder "Coming soon" untuk sesi ini? (ya/tidak)
```
