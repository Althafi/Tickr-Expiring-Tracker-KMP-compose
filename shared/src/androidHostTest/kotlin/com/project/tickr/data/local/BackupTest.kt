package com.project.tickr.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.core.util.DateTimeUtilImpl
import com.project.tickr.data.local.datasource.CategoryLocalDataSource
import com.project.tickr.data.local.datasource.ItemLocalDataSource
import com.project.tickr.data.local.db.DatabaseSeeder
import com.project.tickr.data.repository.LocalBackupRepository
import com.project.tickr.data.repository.LocalCategoryRepository
import com.project.tickr.data.repository.LocalItemRepository
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.model.BackupExport
import com.project.tickr.domain.model.Category
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.storage.ImageStorage
import kotlinx.coroutines.runBlocking
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Satu "perangkat": database + penyimpanan foto sendiri. */
private class Device {
    val db: TickrDatabase = JdbcSqliteDriver(
        JdbcSqliteDriver.IN_MEMORY,
        Properties().apply { put("foreign_keys", "true") },
    ).let { driver ->
        TickrDatabase.Schema.create(driver)
        TickrDatabase(driver).also { DatabaseSeeder.seedIfEmpty(it, "seed") }
    }
    val images = FakeImageStorage()
    val items = LocalItemRepository(ItemLocalDataSource(db), images)
    val categories = LocalCategoryRepository(CategoryLocalDataSource(db))
    val backup = LocalBackupRepository(db, images, DateTimeUtilImpl())

    fun item(
        name: String,
        expiry: String = "2026-12-01",
        categoryId: Long? = null,
        image: String? = null,
        consumed: Boolean = false,
        notes: String? = null,
    ) = Item(
        id = 0, categoryId = categoryId, name = name, barcode = "123", expiryDate = expiry,
        imageUrl = image, notes = notes, isConsumed = consumed, quantity = 3, unit = "PCS",
        createdAt = "", updatedAt = "",
    )

    suspend fun add(item: Item): Item = (items.createItem(item) as DataResult.Success).data
    suspend fun allItems(): List<Item> = (items.getItems() as DataResult.Success).data
    suspend fun export(): BackupExport = (backup.export() as DataResult.Success).data
}

class BackupTest {

    private fun photo(seed: Int) = ByteArray(64) { (it * seed).toByte() }

    private suspend fun Device.addPhoto(name: String, bytes: ByteArray, categoryId: Long? = null): Item {
        val path = (images.saveBytes(bytes) as DataResult.Success).data
        return add(item(name, image = path, categoryId = categoryId))
    }

    private fun validationField(r: DataResult<*>): String =
        ((r as DataResult.Error).error as AppError.Validation).field

    @Test
    fun export_then_import_on_another_device_restores_items_and_photos() = runBlocking<Unit> {
        val a = Device()
        val food = (a.categories.getCategories() as DataResult.Success).data.first { it.key == "food" }
        val photoBytes = photo(7)
        a.addPhoto("Susu", photoBytes, food.id)
        a.add(a.item("Salep", expiry = "2027-01-15", consumed = true, notes = "oles tipis"))

        val export = a.export()
        assertEquals(2, export.itemCount)
        assertTrue(Regex("""tickr-backup-\d{4}-\d{2}-\d{2}\.json""").matches(export.fileName), export.fileName)

        val b = Device()
        val result = b.backup.import(export.content)
        assertEquals(2, (result as DataResult.Success).data.itemCount)

        val restored = b.allItems().associateBy { it.name }
        val susu = restored.getValue("Susu")
        assertEquals("2026-12-01", susu.expiryDate)
        assertEquals(3, susu.quantity)
        assertEquals("123", susu.barcode)
        val localFood = (b.categories.getCategories() as DataResult.Success).data.first { it.key == "food" }
        assertEquals(localFood.id, susu.categoryId)
        assertContentEquals(photoBytes, b.images.files.getValue(susu.imageUrl!!))

        val salep = restored.getValue("Salep")
        assertTrue(salep.isConsumed)
        assertEquals("oles tipis", salep.notes)
        assertNull(salep.imageUrl)
    }

    @Test
    fun import_replaces_everything_and_removes_old_photos_and_notifications() = runBlocking<Unit> {
        val source = Device().apply { add(item("Baru")) }
        val content = source.export().content

        val target = Device()
        val old = target.addPhoto("Lama", photo(3))
        target.db.notificationLogQueries.insert(old.id, "2026-12-01", 1, "t")
        assertEquals(1, target.db.notificationLogQueries.selectAll().executeAsList().size)

        assertIs<DataResult.Success<*>>(target.backup.import(content))

        assertEquals(listOf("Baru"), target.allItems().map { it.name })
        assertTrue(target.images.deleted.contains(old.imageUrl), "foto barang lama harus dihapus")
        assertTrue(target.db.notificationLogQueries.selectAll().executeAsList().isEmpty())
    }

    @Test
    fun custom_category_is_recreated_and_ids_are_remapped() = runBlocking<Unit> {
        val a = Device()
        val pet = (a.categories.createCategory(Category(0, "Hewan", "ic_pet", "#112233", "")) as DataResult.Success).data
        a.add(a.item("Pakan", categoryId = pet.id))
        val content = a.export().content

        val b = Device()
        // Buat id berbeda di perangkat tujuan supaya pemetaan ulang benar-benar teruji.
        b.categories.createCategory(Category(0, "Lain", "ic_x", "#000000", ""))
        b.backup.import(content)

        val cats = (b.categories.getCategories() as DataResult.Success).data
        val restoredPet = cats.single { it.name == "Hewan" }
        assertNull(restoredPet.key)
        assertEquals(restoredPet.id, b.allItems().single().categoryId)
        assertNotEquals(pet.id, restoredPet.id)

        // Impor kedua kali tidak menggandakan kategori.
        b.backup.import(content)
        assertEquals(1, (b.categories.getCategories() as DataResult.Success).data.count { it.name == "Hewan" })
    }

    @Test
    fun invalid_files_are_rejected_and_leave_data_untouched() = runBlocking<Unit> {
        val d = Device()
        val existing = d.addPhoto("Aman", photo(5))
        val bad = listOf(
            "",
            "bukan json",
            "[]",
            """{"app":"lain","version":1,"exportedAt":"x","categories":[],"items":[]}""",
            """{"app":"tickr","exportedAt":"x","categories":[],"items":[]}""",
            """{"app":"tickr","version":1,"exportedAt":"x","categories":[],"items":[{"name":"X"}]}""",
            // tanggal rusak
            """{"app":"tickr","version":1,"exportedAt":"x","categories":[],"items":[{"name":"X","expiryDate":"besok","unit":"PCS","createdAt":"a","updatedAt":"a"}]}""",
            // jumlah 0
            """{"app":"tickr","version":1,"exportedAt":"x","categories":[],"items":[{"name":"X","expiryDate":"2026-12-01","quantity":0,"unit":"PCS","createdAt":"a","updatedAt":"a"}]}""",
            // nama kosong
            """{"app":"tickr","version":1,"exportedAt":"x","categories":[],"items":[{"name":" ","expiryDate":"2026-12-01","unit":"PCS","createdAt":"a","updatedAt":"a"}]}""",
            // base64 rusak
            """{"app":"tickr","version":1,"exportedAt":"x","categories":[],"items":[{"name":"X","expiryDate":"2026-12-01","unit":"PCS","createdAt":"a","updatedAt":"a","imageBase64":"@@@bukan base64@@@"}]}""",
        )
        bad.forEach { content ->
            val r = d.backup.import(content)
            assertEquals("backup", validationField(r), "harus ditolak: $content")
        }
        assertEquals(listOf("Aman"), d.allItems().map { it.name })
        assertTrue(d.images.files.containsKey(existing.imageUrl), "foto lama tidak boleh tersentuh")
        assertEquals(1, d.images.files.size, "tidak boleh ada foto baru yang tertinggal")
    }

    @Test
    fun file_from_newer_version_is_reported_as_newer_not_corrupt() = runBlocking<Unit> {
        val d = Device()
        d.add(d.item("Aman"))
        // Bentuk isi sengaja tidak dikenal: versi baru boleh mengubah struktur.
        val r = d.backup.import("""{"app":"tickr","version":99,"data":{"apa":"saja"}}""")
        assertEquals("backup_version", validationField(r))
        assertEquals(1, d.allItems().size)
    }

    @Test
    fun unknown_extra_fields_are_ignored() = runBlocking<Unit> {
        val d = Device()
        val content = """{"app":"tickr","version":1,"exportedAt":"2026-12-01","futureField":true,"categories":[],
            "items":[{"name":"Roti","expiryDate":"2026-12-05","unit":"PCS","createdAt":"a","updatedAt":"a","extra":1}]}"""
        assertIs<DataResult.Success<*>>(d.backup.import(content))
        assertEquals(listOf("Roti"), d.allItems().map { it.name })
    }

    @Test
    fun failing_photo_write_rolls_back_without_leaving_files_or_changing_data() = runBlocking<Unit> {
        val source = Device()
        source.addPhoto("A", photo(1))
        source.addPhoto("B", photo(2))
        val content = source.export().content

        val target = Device()
        target.add(target.item("Tetap"))
        val flaky = object : ImageStorage by target.images {
            var calls = 0
            override suspend fun saveBytes(bytes: ByteArray): DataResult<String> =
                if (++calls == 2) DataResult.Error(AppError.Unknown("disk penuh")) else target.images.saveBytes(bytes)
        }
        val repo = LocalBackupRepository(target.db, flaky, DateTimeUtilImpl())

        assertIs<DataResult.Error>(repo.import(content))
        assertEquals(listOf("Tetap"), target.allItems().map { it.name })
        assertTrue(target.images.files.isEmpty(), "foto pertama yang sempat ditulis harus dibuang")
    }

    @Test
    fun export_of_empty_database_roundtrips() = runBlocking<Unit> {
        val a = Device()
        val export = a.export()
        assertEquals(0, export.itemCount)
        val b = Device()
        b.add(b.item("Akan hilang"))
        assertEquals(0, (b.backup.import(export.content) as DataResult.Success).data.itemCount)
        assertTrue(b.allItems().isEmpty())
    }

    @Test
    fun missing_photo_file_is_exported_as_item_without_photo() = runBlocking<Unit> {
        val a = Device()
        a.add(a.item("Hantu", image = "product_images/tidak-ada.jpg"))
        val b = Device()
        b.backup.import(a.export().content)
        assertNull(b.allItems().single().imageUrl)
    }
}
