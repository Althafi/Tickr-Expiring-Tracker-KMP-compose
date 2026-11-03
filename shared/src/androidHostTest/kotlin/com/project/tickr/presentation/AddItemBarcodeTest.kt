package com.project.tickr.presentation

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.core.result.getOrNull
import com.project.tickr.core.util.DateTimeUtilImpl
import com.project.tickr.data.local.FakeImageStorage
import com.project.tickr.data.local.datasource.CategoryLocalDataSource
import com.project.tickr.data.local.datasource.ItemLocalDataSource
import com.project.tickr.data.local.db.DatabaseSeeder
import com.project.tickr.data.repository.LocalCategoryRepository
import com.project.tickr.data.repository.LocalItemRepository
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.model.CategoryKeys
import com.project.tickr.domain.model.DueReminder
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.model.ItemUnits
import com.project.tickr.domain.model.ProductInfo
import com.project.tickr.domain.provider.ConnectivityProvider
import com.project.tickr.domain.repository.NotificationRepository
import com.project.tickr.domain.repository.ProductLookupRepository
import com.project.tickr.domain.scheduler.ReminderScheduler
import com.project.tickr.domain.usecase.category.GetCategoriesUseCase
import com.project.tickr.domain.usecase.home.AddItemUseCase
import com.project.tickr.domain.usecase.item.DiscardProductImageUseCase
import com.project.tickr.domain.usecase.item.SaveProductImageUseCase
import com.project.tickr.domain.usecase.product.DownloadProductImageUseCase
import com.project.tickr.domain.usecase.product.LookupBarcodeUseCase
import com.project.tickr.domain.usecase.reminder.ProcessDueRemindersUseCase
import com.project.tickr.domain.usecase.reminder.SyncRemindersUseCase
import com.project.tickr.presentation.additem.AddItemAction
import com.project.tickr.presentation.additem.AddItemViewModel
import com.project.tickr.presentation.additem.NoticeKind
import com.project.tickr.presentation.common.AppMessenger
import com.project.tickr.presentation.common.ErrorMessageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Perilaku scan barcode di form Tambah Barang: wajib online, ada alert saat offline, autofill, barcode ikut tersimpan. */
@OptIn(ExperimentalCoroutinesApi::class)
class AddItemBarcodeTest {

    private class Connectivity(var online: Boolean) : ConnectivityProvider {
        override fun isOnline() = online
    }

    private class Lookup(var result: DataResult<ProductInfo?>) : ProductLookupRepository {
        val requested = mutableListOf<String>()
        override suspend fun lookup(barcode: String): DataResult<ProductInfo?> { requested += barcode; return result }
        override suspend fun downloadImage(url: String): DataResult<ByteArray> = DataResult.Success(byteArrayOf(1, 2, 3))
    }

    private val noScheduler = object : ReminderScheduler {
        override suspend fun hasPermission() = true
        override suspend fun syncAll(items: List<Item>) = Unit
        override suspend fun deliver(due: List<DueReminder>) = Unit
        override suspend fun cancel(itemId: Long) = Unit
        override suspend fun cancelAll() = Unit
    }

    private lateinit var connectivity: Connectivity
    private lateinit var lookup: Lookup
    private lateinit var itemRepo: LocalItemRepository
    private lateinit var vm: AddItemViewModel

    private val beautyProduct = ProductInfo(
        barcode = "8999999999990", name = "Serum Wajah", brand = "Glow", imageUrl = "https://img.example/serum.jpg",
        quantity = 30, unit = ItemUnits.MILLILITER, categoryKey = CategoryKeys.BEAUTY,
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, Properties().apply { put("foreign_keys", "true") })
        TickrDatabase.Schema.create(driver)
        val db = TickrDatabase(driver).also { DatabaseSeeder.seedIfEmpty(it, "seed") }
        val images = FakeImageStorage()
        itemRepo = LocalItemRepository(ItemLocalDataSource(db), images)
        val categoryRepo = LocalCategoryRepository(CategoryLocalDataSource(db))
        val dateTime = DateTimeUtilImpl()
        connectivity = Connectivity(online = true)
        lookup = Lookup(DataResult.Success(beautyProduct))
        val notifications = object : NotificationRepository by notImplemented() {}
        vm = AddItemViewModel(
            addItem = AddItemUseCase(itemRepo, dateTime),
            getCategories = GetCategoriesUseCase(categoryRepo),
            dateTime = dateTime,
            saveProductImage = SaveProductImageUseCase(images),
            discardProductImage = DiscardProductImageUseCase(images),
            errorMessages = ErrorMessageProvider(),
            syncReminders = SyncRemindersUseCase(itemRepo, noScheduler, ProcessDueRemindersUseCase(itemRepo, notifications, dateTime)),
            messenger = AppMessenger(),
            lookupBarcode = LookupBarcodeUseCase(connectivity, lookup),
            downloadProductImage = DownloadProductImageUseCase(lookup, images),
            connectivity = connectivity,
        )
        // Kategori dimuat di thread lain; tunggu dulu supaya tes tidak bergantung pada kebetulan waktu.
        awaitUntil { !vm.state.value.isLoadingCategories }
    }

    /** Menunggu pekerjaan latar (database/unduhan jalan di Dispatchers.Default) sampai [condition] benar. */
    private fun awaitUntil(timeoutMillis: Long = 5_000, condition: () -> Boolean) = kotlinx.coroutines.runBlocking {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "Kondisi tidak tercapai dalam $timeoutMillis ms" }
            kotlinx.coroutines.delay(10)
        }
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun offline_scan_shows_an_alert_and_does_not_open_the_scanner() {
        connectivity.online = false
        vm.onAction(AddItemAction.ScanClicked)

        val state = vm.state.value
        assertFalse(state.requestScanner)
        assertEquals(NoticeKind.OFFLINE, state.notice?.kind)
        assertTrue(lookup.requested.isEmpty())
    }

    @Test
    fun offline_user_can_still_fill_and_the_form_stays_usable() {
        connectivity.online = false
        vm.onAction(AddItemAction.ScanClicked)
        vm.onAction(AddItemAction.NameChanged("Roti tawar"))
        vm.onAction(AddItemAction.QtyChanged(2))
        vm.onAction(AddItemAction.RawDateChanged("01122030"))

        assertTrue(vm.state.value.canSave)
    }

    @Test
    fun online_scan_requests_the_scanner_once() {
        vm.onAction(AddItemAction.ScanClicked)
        assertTrue(vm.state.value.requestScanner)
        assertNull(vm.state.value.notice)

        vm.onAction(AddItemAction.ScannerLaunched)
        assertFalse(vm.state.value.requestScanner)
    }

    @Test
    fun found_product_fills_the_form() {
        vm.onAction(AddItemAction.BarcodeScanned(" 8999999999990 "))

        val s = vm.state.value
        assertEquals(listOf("8999999999990"), lookup.requested)
        assertEquals("8999999999990", s.barcode)
        assertEquals("Glow Serum Wajah", s.name)
        assertEquals("https://img.example/serum.jpg", s.photoPath)
        assertEquals(30, s.quantity)
        assertEquals(ItemUnits.MILLILITER, s.unit)
        assertEquals(CategoryKeys.BEAUTY, s.dbCategories.first { it.id == s.selectedCategoryId }.key)
        assertEquals(NoticeKind.SUCCESS, s.notice?.kind)
        assertFalse(s.isLookingUp)
    }

    @Test
    fun scanning_does_not_overwrite_what_the_user_already_typed() {
        vm.onAction(AddItemAction.NameChanged("Serum buatan sendiri"))
        vm.onAction(AddItemAction.QtyChanged(4))
        vm.onAction(AddItemAction.UnitChanged(ItemUnits.PACK))
        vm.onAction(AddItemAction.BarcodeScanned("8999999999990"))

        val s = vm.state.value
        assertEquals("Serum buatan sendiri", s.name)
        assertEquals(4, s.quantity)
        assertEquals(ItemUnits.PACK, s.unit)
    }

    @Test
    fun unknown_product_keeps_the_barcode_and_tells_the_user() {
        lookup.result = DataResult.Success(null)
        vm.onAction(AddItemAction.BarcodeScanned("8999999999990"))

        val s = vm.state.value
        assertEquals("8999999999990", s.barcode)
        assertEquals("", s.name)
        assertEquals(NoticeKind.INFO, s.notice?.kind)
        assertFalse(s.isLookingUp)
    }

    @Test
    fun network_failure_shows_the_offline_alert_but_keeps_the_barcode() {
        lookup.result = DataResult.Error(AppError.Network("timeout"))
        vm.onAction(AddItemAction.BarcodeScanned("8999999999990"))

        val s = vm.state.value
        assertEquals("8999999999990", s.barcode)
        assertEquals(NoticeKind.OFFLINE, s.notice?.kind)
        assertFalse(s.isLookingUp)
    }

    @Test
    fun connection_lost_between_scan_and_lookup_is_reported_without_a_request() {
        connectivity.online = false
        vm.onAction(AddItemAction.BarcodeScanned("8999999999990"))

        assertEquals(NoticeKind.OFFLINE, vm.state.value.notice?.kind)
        assertTrue(lookup.requested.isEmpty())
    }

    @Test
    fun clear_barcode_and_dismiss_notice() {
        vm.onAction(AddItemAction.BarcodeScanned("8999999999990"))
        vm.onAction(AddItemAction.ClearBarcode)
        vm.onAction(AddItemAction.DismissNotice)

        assertNull(vm.state.value.barcode)
        assertNull(vm.state.value.notice)
    }

    @Test
    fun saving_stores_the_barcode_and_downloads_the_photo() = kotlinx.coroutines.runBlocking {
        vm.onAction(AddItemAction.BarcodeScanned("8999999999990"))
        vm.onAction(AddItemAction.RawDateChanged("01122030"))
        assertTrue(vm.state.value.canSave)

        vm.onAction(AddItemAction.Save)
        awaitUntil { vm.state.value.barcode == null && !vm.state.value.isSaving }

        val saved = itemRepo.getItems().getOrNull().orEmpty().single()
        assertEquals("8999999999990", saved.barcode)
        assertEquals("Glow Serum Wajah", saved.name)
        assertNotNull(saved.imageUrl) // foto diunduh dan disimpan lokal, bukan URL internet
        assertFalse(saved.imageUrl!!.startsWith("http"))
        assertNull(vm.state.value.barcode) // form direset
        Unit
    }

    private inline fun <reified T : Any> notImplemented(): T =
        java.lang.reflect.Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, m, _ ->
            error("${m.name} tidak diharapkan dipanggil")
        } as T
}
