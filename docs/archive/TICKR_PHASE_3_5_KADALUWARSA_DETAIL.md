# 📋 Template Prompt — Tickr Phase 3.5 · Screen **Kadaluwarsa (List)** & **Detail Barang**

> **Turunan dari** `TICKR_PHASE_3_5_PROMPT_TEMPLATE.md`. Semua **constraint global,
> design token, struktur theme, dan strategi anti-token-leak TETAP** (tidak diubah).
> Dokumen ini hanya mengisi bagian 🟨 untuk dua layar: **Kadaluwarsa** dan **Detail Barang**,
> plus tambahan eksplisit untuk **transisi navigasi**, **empty state**, dan **skeleton loading**.

---

## 0. Ringkasan Analisis (BACA DULU — konteks untuk agent)

### 0.1 Analisis Template Phase 3.5 (markdown referensi)
Template existing sudah matang: pola dua lapis `Route` (stateful) + `Screen` (stateless),
design tokens penuh (`TickrTheme.colors/.spacing/.typography/.shapes`), zero-hardcode,
MVI/UDF, anti-token-leak saat baca gambar, dan acceptance criteria global. **Dokumen ini
mengikuti semua pola tersebut tanpa kecuali.** Yang ditambahkan hanya hal yang belum
ada di template asli: spec list+detail, transisi navigasi, dan tiga kondisi UI list
(terisi / kosong / skeleton).

### 0.2 Design Token Brief — Layar Kadaluwarsa & Detail (ringkas, tekstual, SEKALI baca)
```
KADALUWARSA (List):
- TopBar custom: kiri "Tickr" (wordmark, primaryBrand, Bold) + tanggal & jam LIVE
  (jam HH:mm:ss berdetak; format "Selasa, 9 Juni 2026" / "18:27:08").
- Search field rounded (shape pill/16dp, surface, ikon search kiri) +
  tombol filter (ikon sliders) → buka BottomSheet Filter.
- Filter chips kategori horizontal-scroll: aktif = primaryBrand solid teks putih,
  inaktif = surface + border tipis textSecondary. ("Semua", "Makanan & Minuman", "Kecan…").
- 2 stat card berdampingan: kiri = safe bg (barang berhasil dikonsumsi, angka besar,
  ikon check), kanan = critical bg (barang mubazir, ikon trash).
- Legend dot: Kritis(critical) · Waspada(warning) · Aman(safe).
- Section "Status Kedaluwarsa" + subtitle textSecondary.
- List card item: thumbnail 14dp kiri, nama(productName) + kategori(body, textSecondary)
  + "Kedaluwarsa: <tgl>", badge countdown kanan (pill, warna ikut urgensi:
  "0 hari"=critical, "3 hari"=warning, "1 bulan 5 hari"=safe).
- FAB bundar primaryBrand (ikon +) mengambang di atas BottomNav.
- BottomNav 4 item: Beranda · Kadaluwarsa(aktif) · Wawasan · Profil (ikon outline 2px).

DETAIL BARANG:
- Hero image full-width (≈45% tinggi) + back button bulat (kiri-atas, surface alpha) +
  delete button bulat (kanan-atas, critical tint).
- Sheet konten overlap hero (rounded-top 20dp, surface).
- Status banner penuh-lebar (radius 16dp) BERUBAH per urgensi:
  critical bg/text + ikon alert-circle · warning bg/text + ikon alert-triangle ·
  safe bg/text + ikon check-circle. Teks: "<Status> – Kedaluwarsa <…> (<tgl>)".
- Judul produk (displayDate/onboardingTitle weight, besar).
- Grid 2 kolom info box: KATEGORI (ikon + nilai) | JUMLAH (ikon + nilai).
- Box "Catatan Penyimpanan" (warning soft bg, ikon pin, body text).
- CTA bawah full-width "Tandai Sudah Habis Dikonsumsi" (primaryBrand, 56dp, soft shadow).
```

### 0.3 Analisis Skema Data (PostgreSQL) — apakah tabel sekarang cukup?
> ⚠️ Konfirmasi: tabel Anda **saat ini kosong dan belum lengkap** untuk memenuhi
> semua data di screen Beranda/Kadaluwarsa/Detail. Berikut skema minimum yang
> direkomendasikan + computed field yang TIDAK boleh disimpan mentah (dihitung di query/domain).

```sql
-- categories: untuk filter chips & label kategori
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        TEXT NOT NULL,              -- "Makanan", "Minuman", "Obat", "Kosmetik", "Lainnya"
    icon_key    TEXT,                       -- key ikon (mis. "ic_food") — bukan path mentah
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- items: entitas utama barang
CREATE TABLE items (
    id            BIGSERIAL PRIMARY KEY,
    name          TEXT NOT NULL,                       -- "Susu UHT"
    category_id   BIGINT NOT NULL REFERENCES categories(id),
    quantity      NUMERIC(10,2) NOT NULL DEFAULT 1,    -- jumlah
    unit          TEXT,                                -- "Karton", "Botol", dll
    expiry_date   DATE NOT NULL,                       -- "2026-06-10"
    storage_note  TEXT,                                -- "Simpan di kulkas bagian pintu."
    image_url     TEXT,                                -- thumbnail / hero
    status        TEXT NOT NULL DEFAULT 'active'       -- active | consumed | wasted
                  CHECK (status IN ('active','consumed','wasted')),
    consumed_at   TIMESTAMPTZ,                         -- diisi saat "Tandai Habis Dikonsumsi"
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_items_expiry  ON items (expiry_date) WHERE status = 'active';
CREATE INDEX idx_items_status  ON items (status);
CREATE INDEX idx_items_category ON items (category_id);
```

**Computed di query / domain (JANGAN disimpan kolom mentah — agar tidak basi):**
- `days_left = expiry_date - CURRENT_DATE` (integer hari).
- `urgency` (enum domain): `CRITICAL` jika `days_left <= 0`/`< 3`; `WARNING` jika `4..7`;
  `SAFE` jika `> 7`. (Sesuaikan ambang dengan design: "0 hari"=critical, "3 hari"=warning,
  "1 bulan 5 hari"=safe — finalkan threshold di UseCase, satu sumber kebenaran.)
- `countdown_label`: format manusiawi ("0 hari", "3 hari", "1 bulan 5 hari", "3 bulan 10 hari").
  Format di layer presentation (`UiText`), bukan disimpan DB.

**Statistik stat-card Beranda** (agregat, bukan kolom):
```sql
SELECT
  COUNT(*) FILTER (WHERE status = 'consumed') AS berhasil_dikonsumsi,
  COUNT(*) FILTER (WHERE status = 'wasted')   AS barang_mubazir
FROM items;
```

**Kesimpulan:** Tabel lama belum cukup. Dengan skema di atas (categories + items +
status lifecycle + index), seluruh data di screen Beranda/Kadaluwarsa/Detail dapat
ditampilkan. `days_left`, `urgency`, dan `countdown_label` **dihitung**, bukan disimpan.
> 🟨 TODO(user): konfirmasi ambang `urgency` final & daftar unit/kategori sebelum migrasi.

---
---

# 🟨 SCREEN A — KADALUWARSA (List)

## 🧭 Scope Layar Saat Ini

```
Nama layar       : Kadaluwarsa (daftar barang + status urgensi)
Label brief      : Kadaluwarsa-List
Jumlah composable: 1 Route + 1 Screen
                   + komponen: TickrTopBar, ExpirySearchBar, CategoryFilterRow,
                     StatCardPair, UrgencyLegend, ExpiryItemCard, ExpiryFilterSheet,
                     TickrBottomNav, TickrFab
                   + 3 STATE konten: Content / Empty / Skeleton(loading-isi)
Catatan          : HANYA layar ini + BottomSheet filter-nya. Jangan sentuh Detail/Beranda
                   selain navigasi keluar (lihat bagian Transisi Navigasi).
```

## 🔍 Analisis Gap Lintas Layer

```
DATA LAYER:
- ItemRepository {
      fun observeItems(filter: ExpiryFilter): Flow<List<Item>>
      suspend fun markConsumed(id: Long)
      suspend fun delete(id: Long)
  }
  (PostgreSQL via backend API ATAU SQLDelight lokal — ikuti pola Phase 1/2.)

DOMAIN LAYER (UseCase tipis):
- ObserveExpiryItemsUseCase(filter) → Flow<List<ExpiryItem>>
    (memetakan Item → ExpiryItem termasuk days_left, urgency, countdown_label).
- GetConsumptionStatsUseCase() → Flow<ConsumptionStats(consumed, wasted)>
- model: ExpiryItem(id, name, categoryName, expiryDate, daysLeft, urgency, countdownLabel, thumbnailKey)
- enum Urgency { CRITICAL, WARNING, SAFE }
- ExpiryFilter(urgency: Set<Urgency> = emptySet(), timeRange: TimeRange? = null,
               category: Long? = null, query: String = "")
- enum TimeRange { GT_6M, GT_3M, GT_30D, GT_7D, D0 }

PRESENTATION LAYER (MVI):
- ExpiryUiState(
      loadState: LoadState,           // Loading | Content | Empty | Error
      items: List<ExpiryItem> = emptyList(),
      stats: ConsumptionStats? = null,
      categories: List<CategoryChip> = emptyList(),
      activeFilter: ExpiryFilter = ExpiryFilter(),
      isRefreshing: Boolean = false,
      isFilterSheetVisible: Boolean = false,
      nowEpochSeconds: Long             // untuk jam live di TopBar
  )
  // LoadState dipisah agar Content/Empty/Skeleton eksplisit (lihat AC).
- ExpiryAction:
      QueryChanged(String), CategorySelected(Long?), OpenFilterSheet, DismissFilterSheet,
      ToggleUrgency(Urgency), SelectTimeRange(TimeRange?), ApplyFilter, ResetFilter,
      Refresh, ItemClicked(Long), MarkConsumed(Long), DeleteItem(Long), AddClicked, TabSelected(Tab)
- ExpiryEvent: NavigateToDetail(Long), NavigateToAddItem, NavigateToTab(Tab), ShowError(UiText)
- ExpiryViewModel: collect UseCase; Refresh → set isRefreshing → reload → unset.
- Destination.ExpiryList : Destination { override val route = "expiry" }
- DI: factory { ExpiryViewModel(get(), get()) }

⚠️ Konfirmasi user: jam live di TopBar = tick per detik (clock state di VM via flow ticker).
```

## 📱 Spesifikasi Detail Layar

```
TOP BAR (TickrTopBar — sticky):
- Kiri: wordmark "Tickr" (primaryBrand, Bold). Di sebelahnya: tanggal lengkap (body,
  textSecondary) baris atas + jam "HH:mm:ss" (displayDate kecil/SemiBold) baris bawah.
- Jam berdetak tiap detik (LaunchedEffect ticker 1s di Route → kirim ke state, ATAU
  rememberCoroutineScope; JANGAN bikin VM logic baru bila Phase 3 sudah ada — kalau belum,
  tambahkan field nowEpochSeconds).

SEARCH + FILTER ROW:
- ExpirySearchBar: TextField rounded (shape 16dp/pill, surface, border tipis),
  placeholder "Cari barang…", leading icon search. onValueChange → Action.QueryChanged.
- Tombol filter (44x44dp, surface, border) ikon sliders → Action.OpenFilterSheet.

CATEGORY FILTER ROW (horizontal scroll, itemGap=12dp):
- Chip aktif: primaryBrand solid + teks putih. Inaktif: surface + border textSecondary.
- onClick → Action.CategorySelected(id|null untuk "Semua").

STAT CARD PAIR (Row, 2 kartu, gap 12dp):
- Kartu kiri (safe soft bg): ikon check bulat hijau, angka besar (displayDate),
  label "Barang Berhasil Dikonsumsi" (body, textSecondary).
- Kartu kanan (critical soft bg): ikon trash bulat merah, angka, "Barang Mubazir".
- Sumber: stats. Saat Loading → tampilkan shimmer di area angka.

LEGEND: Row 3 dot+label — Kritis(critical) · Waspada(warning) · Aman(safe).

SECTION HEADER: "Status Kedaluwarsa" (onboardingTitle kiri) + subtitle
  "Pantau kesegaran stok rumah Anda." (body, textSecondary).

LIST (LazyColumn, itemGap=12dp, contentPadding screenPadding=20dp, bottom padding utk FAB+nav):
- ExpiryItemCard:
    [thumbnail 14dp] [kolom: name(productName) · categoryName(body,textSecondary) ·
     "Kedaluwarsa: <tgl>"(body,textSecondary)] [spacer] [badge countdownLabel]
  - Badge pill warna ikut urgency: critical/warning/safe (bg+text dari SemanticColors).
  - Card: surface, border tipis/flat elevation, radius 20dp, padding cardInner=16dp.
  - onClick → Action.ItemClicked(id) → Event.NavigateToDetail.
  - (opsional) swipe-to-delete → Action.DeleteItem; long-press tidak wajib.

FAB: bundar 56dp primaryBrand ikon "+", soft shadow, mengambang kanan-bawah di atas
  BottomNav → Action.AddClicked → Event.NavigateToAddItem.

BOTTOM NAV (TickrBottomNav, 4 item, sticky):
- Beranda · Kadaluwarsa(aktif=primaryBrand) · Wawasan · Profil.
- Ikon outline 2px; label kecil. onClick → Action.TabSelected(Tab).

═══════════════════════════════════════════════════════════════
TIGA KONDISI KONTEN (WAJIB, sesuai loadState) — render di area LIST:
═══════════════════════════════════════════════════════════════
1) CONTENT (ada data):
   - Tampilkan StatCardPair + Legend + List item seperti di atas.

2) EMPTY (loadState = Empty / items kosong & bukan loading):
   - Komponen ExpiryEmptyState: ilustrasi placeholder (ill_empty_expiry, primaryBrand
     alpha 0.1 box + label), judul "Belum ada barang" (onboardingTitle),
     subtitle "Tambahkan barang pertamamu untuk mulai memantau tanggal kedaluwarsa."
     (body, textSecondary), tombol "Tambah Barang" (primaryBrand, 56dp) → Action.AddClicked.
   - StatCardPair tetap tampil bila stats != null (boleh 0/0); legend disembunyikan
     bila tak relevan. // TODO(user): putuskan tampil/sembunyi stat saat empty.

3) SKELETON LOADING — versi TERISI DATA (loadState = Loading, pertama kali / saat refresh):
   - JANGAN tampilkan empty saat loading. Tampilkan shimmer mirip layout CONTENT:
     * StatCardPair → 2 kartu shimmer (angka & label bar abu).
     * Legend → bisa langsung tampil (statis) atau shimmer tipis.
     * List → 5–6 ExpiryItemCardSkeleton: kotak thumbnail shimmer + 3 bar teks shimmer
       (lebar berbeda) + bar badge shimmer. Tinggi sama dgn card asli (no layout shift).
   - Komponen: ShimmerBox(modifier) reusable (brush animasi gradient,
     warna surface→background→surface). itemGap & padding identik dgn Content.

REFRESH (pull-to-refresh):
- PullToRefresh (Material3 / expect-actual bila perlu) → Action.Refresh.
- Saat refresh: isRefreshing=true. Pilihan UX (pilih salah satu, // TODO(user)):
  (a) spinner pull-to-refresh di atas list yang lama tetap terlihat, ATAU
  (b) ganti list dgn SKELETON terisi-data (poin 3) bila ingin full reload feel.
  Default rekomendasi: (a) untuk refresh manual, (b) untuk initial load.

AKSESIBILITAS: target ≥44dp (chip, FAB, nav, filter btn); contentDescription pada
  ikon search/filter/FAB/nav/thumbnail; kontras WCAG AA light & dark.
ANIMASI: chip select → animateColor; FAB pressed scale 0.95x; shimmer translate loop;
  list item enter → fade/slide-in tipis (animateItemPlacement).
```

## 🔤 String & Drawable Example untuk Layar Ini

```xml
<!-- TODO(user): sesuaikan semua nilai -->
<string name="expiry_search_hint">Cari barang…</string>
<string name="expiry_filter_category_all">Semua</string>
<string name="expiry_stat_consumed_label">Barang Berhasil Dikonsumsi</string>
<string name="expiry_stat_wasted_label">Barang Mubazir</string>
<string name="expiry_legend_critical">Kritis</string>
<string name="expiry_legend_warning">Waspada</string>
<string name="expiry_legend_safe">Aman</string>
<string name="expiry_section_title">Status Kedaluwarsa</string>
<string name="expiry_section_subtitle">Pantau kesegaran stok rumah Anda.</string>
<string name="expiry_item_expiry_prefix">Kedaluwarsa: %1$s</string>
<string name="expiry_empty_title">Belum ada barang</string>
<string name="expiry_empty_subtitle">Tambahkan barang pertamamu untuk mulai memantau tanggal kedaluwarsa.</string>
<string name="expiry_empty_cta">Tambah Barang</string>
<string name="nav_home">Beranda</string>
<string name="nav_expiry">Kadaluwarsa</string>
<string name="nav_insight">Wawasan</string>
<string name="nav_profile">Profil</string>
<string name="cd_expiry_filter">Buka filter status</string>
<string name="cd_expiry_add">Tambah barang baru</string>
<string name="cd_expiry_search">Cari barang</string>
```

```
DRAWABLE (placeholder — TODO(user): ganti aset final):
- ill_empty_expiry        (ilustrasi keranjang kosong)
- ic_search, ic_sliders, ic_plus, ic_check_circle, ic_trash
- ic_nav_home, ic_nav_expiry, ic_nav_insight, ic_nav_profile
```

## ✅ Acceptance Criteria Spesifik — Kadaluwarsa

```
[ ] Route/Screen split benar; Screen @Preview light & dark
[ ] @Preview terpisah untuk KETIGA kondisi: Content, Empty, Skeleton(loading-isi)
[ ] LoadState mengontrol UI: Loading→skeleton, kosong→empty, ada→content (tidak tertukar)
[ ] Skeleton meniru layout Content (no layout shift), shimmer beranimasi
[ ] Pull-to-refresh memicu Action.Refresh; perilaku refresh sesuai pilihan UX
[ ] TopBar menampilkan tanggal + jam live berdetak per detik
[ ] Filter chips kategori sinkron dgn activeFilter; "Semua" reset kategori
[ ] Badge countdown warna ikut urgency via SemanticColors (zero hardcode)
[ ] StatCardPair menampilkan consumed/wasted dari GetConsumptionStatsUseCase
[ ] FAB & BottomNav memicu event navigasi yang benar
[ ] Gap data/domain/presentation ditambahkan mengikuti pola Phase 1/2/3
```

---
---

# 🟨 SCREEN B — DETAIL BARANG

## 🧭 Scope Layar Saat Ini

```
Nama layar       : Detail Barang (status urgensi dinamis)
Label brief      : Detail-Item
Jumlah composable: 1 Route + 1 Screen
                   + komponen: DetailHero (image+back+delete), UrgencyBanner,
                     InfoBoxPair (Kategori/Jumlah), StorageNoteBox, ConsumeCta
                   + state: Loading(skeleton) / Content / Error
Catatan          : HANYA layar ini. Banner BERUBAH per urgency (3 varian di gambar
                   adalah SATU komponen yang sama dgn state berbeda, bukan 3 layar).
```

## 🔍 Analisis Gap Lintas Layer

```
DATA LAYER: reuse ItemRepository (getById, markConsumed, delete) dari Screen A.

DOMAIN LAYER:
- GetItemDetailUseCase(id) → Flow<ExpiryItem?> (termasuk urgency + countdown_label + tgl format)
- MarkItemConsumedUseCase(id) → suspend
- DeleteItemUseCase(id) → suspend

PRESENTATION LAYER (MVI):
- DetailUiState(loadState: LoadState, item: ExpiryItem? = null, isProcessing: Boolean = false)
- DetailAction: Back, Delete, ConfirmDelete, DismissDeleteDialog, MarkConsumed
- DetailEvent: NavigateBack, ShowDeleteConfirm, ItemRemoved(→back), ShowError(UiText)
- DetailViewModel(getDetail, markConsumed, delete): observe by id.
- Destination.ItemDetail(id) : Destination { override val route = "item/{id}" }
- DI: factory { params -> DetailViewModel(params.get(), get(), get(), get()) }
```

## 📱 Spesifikasi Detail Layar

```
HERO (DetailHero, ≈45% tinggi):
- Image full-width (image_url; placeholder shimmer saat load).
- Back button bulat 44dp kiri-atas (surface alpha, ikon chevron-left) → Action.Back.
- Delete button bulat 44dp kanan-atas (critical tint bg, ikon trash) → Action.Delete
  → Event.ShowDeleteConfirm → dialog konfirmasi → Action.ConfirmDelete.
- Safe-area top (expect/actual) agar tombol tidak ketutup notch.

SHEET KONTEN (overlap hero, rounded-top 20dp surface, screenPadding=20dp):
- UrgencyBanner (full-width, radius 16dp, padding 12–16dp) — VARIAN per urgency:
    CRITICAL: bg critical-light, text critical, ikon alert-circle.
              "Kritis – Kedaluwarsa Hari Ini (<tgl>)"
    WARNING : bg warning-light, text warning, ikon alert-triangle.
              "Waspada – Kedaluwarsa <n> Hari Lagi (<tgl>)"
    SAFE    : bg safe-light, text safe, ikon check-circle.
              "Aman – Kedaluwarsa <n> Hari Lagi (<tgl>)"
  → satu @Composable, warna & ikon & teks dipilih dari Urgency (when), via SemanticColors.

- Judul produk: name (besar, Bold ~26–30sp / onboardingTitle).

- InfoBoxPair (Row 2 box, gap 12dp, masing-masing surface+border, radius 14dp, padding 16dp):
    Box KATEGORI: label "KATEGORI" (caption uppercase, textSecondary) + ikon + categoryName.
    Box JUMLAH  : label "JUMLAH" + ikon + "<quantity> <unit>" (mis. "1 Karton").

- StorageNoteBox (full-width, warning soft bg, radius 14dp, padding 16dp):
    header ikon pin + "Catatan Penyimpanan" (warning text, SemiBold) + body storage_note.
    Sembunyikan box bila storage_note kosong.

CTA BAWAH (ConsumeCta, sticky bottom, full-width):
- Tombol "Tandai Sudah Habis Dikonsumsi" (primaryBrand, 56dp, radius 16dp, ikon check,
  soft shadow) → Action.MarkConsumed → MarkItemConsumedUseCase → Event.ItemRemoved → back.
- Saat isProcessing → tombol disabled + loading indicator.

═══════════════════════════════════════════════════════════════
KONDISI KONTEN:
- LOADING → DetailSkeleton: hero shimmer + banner-bar shimmer + judul bar shimmer +
  2 info box shimmer + note box shimmer + CTA shimmer (mirip layout content, no shift).
- CONTENT → seperti di atas.
- ERROR (item null/terhapus) → state pesan + tombol kembali.
═══════════════════════════════════════════════════════════════

AKSESIBILITAS: back/delete/CTA ≥44dp; contentDescription pada hero image, back, delete,
  banner (umumkan status), CTA. Kontras banner WCAG AA di light & dark (token sudah disiapkan).
ANIMASI: shared-element/scale hero saat masuk (opsional); CTA pressed scale 0.95x;
  banner fade-in; delete → dialog.
```

## 🔤 String & Drawable Example untuk Layar Ini

```xml
<!-- TODO(user): sesuaikan -->
<string name="detail_label_category">KATEGORI</string>
<string name="detail_label_quantity">JUMLAH</string>
<string name="detail_storage_note_title">Catatan Penyimpanan</string>
<string name="detail_cta_consume">Tandai Sudah Habis Dikonsumsi</string>
<string name="detail_status_critical_today">Kritis – Kedaluwarsa Hari Ini (%1$s)</string>
<string name="detail_status_warning">Waspada – Kedaluwarsa %1$d Hari Lagi (%2$s)</string>
<string name="detail_status_safe">Aman – Kedaluwarsa %1$d Hari Lagi (%2$s)</string>
<string name="detail_delete_confirm_title">Hapus barang ini?</string>
<string name="detail_delete_confirm_message">Tindakan ini tidak dapat dibatalkan.</string>
<string name="detail_quantity_format">%1$s %2$s</string> <!-- "1 Karton" -->
<string name="cd_detail_back">Kembali</string>
<string name="cd_detail_delete">Hapus barang</string>
<string name="cd_detail_hero">Foto produk</string>
```

```
DRAWABLE (placeholder — TODO(user): ganti aset final):
- ic_chevron_left, ic_trash, ic_pin, ic_check
- ic_alert_circle, ic_alert_triangle, ic_check_circle (banner per urgency)
- ic_category, ic_quantity
- placeholder_product_image (shimmer/box saat hero load)
```

## ✅ Acceptance Criteria Spesifik — Detail Barang

```
[ ] Route/Screen split benar; @Preview untuk 3 urgency (critical/warning/safe) light & dark
[ ] @Preview untuk DetailSkeleton (loading)
[ ] UrgencyBanner satu komponen, varian dipilih via Urgency (when) + SemanticColors
[ ] InfoBoxPair menampilkan kategori & "jumlah + unit" benar
[ ] StorageNoteBox tersembunyi bila note kosong
[ ] MarkConsumed → UseCase → Event.ItemRemoved → kembali ke list
[ ] Delete → konfirmasi → DeleteItemUseCase → kembali ke list
[ ] Back & delete & CTA ≥44dp; contentDescription lengkap; safe-area dihormati
[ ] Gap domain/presentation ditambahkan; reuse ItemRepository
```

---
---

# 🟨 BOTTOM SHEET — FILTER STATUS (komponen Screen A)

## 📱 Spesifikasi

```
ExpiryFilterSheet (ModalBottomSheet, surface, rounded-top 20dp, drag handle):
- Judul "Filter Status" (onboardingTitle/Bold).
- Grup "Status Urgensi" (label SemiBold): chip multi-select Aman/Waspada/Kritis.
    aktif = primaryBrand solid teks putih; inaktif = surface + border.
    onClick → Action.ToggleUrgency(Urgency).
- Grup "Rentang Waktu" (label SemiBold): chip single-select wrap →
    ">6 Bulan", ">3 Bulan", ">30 Hari", ">7 Hari", "0 Hari".
    onClick → Action.SelectTimeRange(TimeRange?).
- Tombol "Terapkan Filter" full-width (primaryBrand, 56dp, radius 16dp) → Action.ApplyFilter
    → tutup sheet + reload list dgn activeFilter. (opsional "Reset" → Action.ResetFilter.)
- Chip wrap pakai FlowRow; gap 12dp; chip shape Pill; target ≥44dp.
- Dismiss (drag down / scrim) → Action.DismissFilterSheet.
```

```xml
<!-- TODO(user): sesuaikan -->
<string name="filter_title">Filter Status</string>
<string name="filter_group_urgency">Status Urgensi</string>
<string name="filter_group_timerange">Rentang Waktu</string>
<string name="filter_urgency_safe">Aman</string>
<string name="filter_urgency_warning">Waspada</string>
<string name="filter_urgency_critical">Kritis</string>
<string name="filter_range_6m">&gt;6 Bulan</string>
<string name="filter_range_3m">&gt;3 Bulan</string>
<string name="filter_range_30d">&gt;30 Hari</string>
<string name="filter_range_7d">&gt;7 Hari</string>
<string name="filter_range_0d">0 Hari</string>
<string name="filter_apply">Terapkan Filter</string>
```

---
---

# 🟨 TRANSISI NAVIGASI (best practice — TETAP untuk seluruh app)

> Compose Multiplatform: gunakan `androidx.navigation` (Navigation Compose KMP) atau
> library yang sudah dipakai Phase 3. Definisikan transisi **sekali** di `TickrNavGraph`.

```
PRINSIP:
- List → Detail  : SLIDE horizontal (push). Masuk: slideInHorizontally(initial = +fullWidth)
  + fadeIn; keluar (list): slideOutHorizontally(target = -fullWidth/3) + fadeOut (paralaks).
- Detail → List  : reverse (pop) — slideOut ke kanan + fadeOut; list slideIn dari kiri.
- Antar Tab BottomNav: CROSSFADE (fadeIn/fadeOut, tanpa slide arah) — tab itu sejajar,
  bukan hierarki. Atau slide-up tipis. Hindari slide kiri-kanan antar tab (membingungkan).
- BottomSheet Filter: ModalBottomSheet (slide-up bawaan) + scrim fade. Bukan layar baru.
- FAB → Add Item : slide-up (vertikal) atau fade+scale dari FAB (container transform feel).
- Durasi: 250–300ms; easing: FastOutSlowIn (standar Material). Jangan > 400ms.

IMPLEMENTASI (pola, taruh di TickrNavGraph):
- Pakai composable(route, enterTransition = { slideInHorizontally(animationSpec=tween(280,
  easing=FastOutSlowInEasing)){ it } + fadeIn() }, exitTransition = { slideOutHorizontally{ -it/3 }
  + fadeOut() }, popEnterTransition = {...}, popExitTransition = {...}).
- Untuk tab: NavHost terpisah per-tab atau enter/exit = fadeIn()/fadeOut() saja.
- Spesifik platform (gesture back iOS swipe-from-edge) → expect/actual bila perlu;
  default Material predictive-back di Android.
- JANGAN hardcode angka durasi tersebar — taruh di TickrMotion (objek token motion):
    object TickrMotion { val durationMedium = 280; val easingStandard = FastOutSlowInEasing }
  // sejalan prinsip zero-magic-number → tambahkan ke design tokens.
```

```
// TODO(user): konfirmasi library navigasi Phase 3 (Voyager / Decompose / Nav-Compose KMP)
//             agar API transisi disesuaikan.
```

---

## ✅ Acceptance Criteria Global Tambahan (untuk paket ini)

```
[ ] Transisi List↔Detail slide horizontal; antar-tab crossfade; sheet slide-up — konsisten
[ ] Durasi/easing motion via TickrMotion (bukan magic number tersebar)
[ ] Tiga kondisi list (Content/Empty/Skeleton) punya @Preview & tidak tertukar oleh loadState
[ ] Skeleton meniru layout berisi data (no layout shift), shimmer beranimasi
[ ] Skema PostgreSQL (categories+items+status) dikonfirmasi; days_left/urgency/countdown dihitung
[ ] Semua warna/spacing/teks via token & resource (zero hardcode) — termasuk komponen baru
[ ] Tidak ada perubahan ViewModel/State/Action/Event Phase 3 yang sudah final
```

---

## 📤 Format Output yang Diminta (TETAP)

```
1. Ringkasan 3–5 baris rencana implementasi.
2. Urutan file: (gap data/domain/presentation bila ada) → komponen reusable
   (ShimmerBox, TickrTopBar, TickrBottomNav, ExpiryItemCard, UrgencyBanner) →
   Screen A (Kadaluwarsa + 3 state) → BottomSheet Filter → Screen B (Detail) →
   TickrNavGraph (transisi).
3. @Preview wajib: Kadaluwarsa{Content, Empty, Skeleton} + Detail{Critical, Warning, Safe, Skeleton},
   masing-masing light & dark.
4. Komentar // TODO(user) di setiap string, drawable, ambang urgency, & pilihan UX.
5. JANGAN implementasi layar lain di luar scope (Beranda/Auth/Add/Wawasan/Profil).
```
