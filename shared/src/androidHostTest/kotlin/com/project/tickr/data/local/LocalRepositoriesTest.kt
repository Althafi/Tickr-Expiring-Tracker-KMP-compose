package com.project.tickr.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.project.tickr.core.result.AppError
import com.project.tickr.core.result.DataResult
import com.project.tickr.data.local.datasource.CategoryLocalDataSource
import com.project.tickr.data.local.datasource.ItemLocalDataSource
import com.project.tickr.data.local.datasource.NotificationLocalDataSource
import com.project.tickr.data.local.db.DatabaseSeeder
import com.project.tickr.data.repository.LocalCategoryRepository
import com.project.tickr.data.repository.LocalItemRepository
import com.project.tickr.data.repository.LocalNotificationRepository
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.model.Category
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.model.NotificationLog
import com.project.tickr.domain.storage.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeImageStorage : ImageStorage {
    val deleted = mutableListOf<String>()
    override suspend fun saveProductImage(sourcePath: String): DataResult<String> = DataResult.Success(sourcePath)
    override suspend fun delete(path: String): DataResult<Unit> { deleted += path; return DataResult.Success(Unit) }
    override fun resolve(path: String): String = "/abs/$path"
}

class LocalRepositoriesTest {

    private var clock = 0
    private val now: () -> String = { "t${++clock}" }

    private val db: TickrDatabase = JdbcSqliteDriver(
        JdbcSqliteDriver.IN_MEMORY,
        Properties().apply { put("foreign_keys", "true") },
    ).let { driver ->
        TickrDatabase.Schema.create(driver)
        TickrDatabase(driver).also { DatabaseSeeder.seedIfEmpty(it, "seed") }
    }

    private val images = FakeImageStorage()
    private val items = LocalItemRepository(ItemLocalDataSource(db, now), images)
    private val categories = LocalCategoryRepository(CategoryLocalDataSource(db, now))
    private val notifications = LocalNotificationRepository(NotificationLocalDataSource(db, now))

    private fun newItem(name: String = "Susu", expiry: String = "2026-12-01", categoryId: Long? = null) = Item(
        id = 0, categoryId = categoryId, name = name, barcode = null,
        expiryDate = expiry, imageUrl = null, notes = null, isConsumed = false,
        quantity = 2, unit = "Pcs", createdAt = "", updatedAt = "",
    )

    private fun <T> DataResult<T>.ok(): T = (this as DataResult.Success).data

    @Test
    fun createItem_returns_saved_item_with_id_and_timestamps() = runBlocking<Unit> {
        val saved = items.createItem(newItem()).ok()
        assertTrue(saved.id > 0)
        assertEquals("Susu", saved.name)
        assertEquals(2, saved.quantity)
        assertEquals("Pcs", saved.unit)
        assertFalse(saved.isConsumed)
        assertEquals(saved.createdAt, saved.updatedAt)
        assertTrue(saved.createdAt.isNotBlank())
    }

    @Test
    fun getItems_sorted_by_expiry() = runBlocking<Unit> {
        items.createItem(newItem("B", "2026-12-10"))
        items.createItem(newItem("A", "2026-12-01"))
        assertEquals(listOf("A", "B"), items.getItems().ok().map { it.name })
    }

    @Test
    fun updateItem_marks_consumed_and_refreshes_updatedAt() = runBlocking<Unit> {
        val saved = items.createItem(newItem()).ok()
        val updated = items.updateItem(saved.copy(isConsumed = true, name = "Susu UHT")).ok()
        assertTrue(updated.isConsumed)
        assertEquals("Susu UHT", updated.name)
        assertEquals(saved.createdAt, updated.createdAt)
        assertTrue(updated.updatedAt != saved.updatedAt)
    }

    @Test
    fun updateItem_unknown_id_returns_NotFound() = runBlocking<Unit> {
        val result = items.updateItem(newItem().copy(id = 999))
        assertIs<AppError.NotFound>((result as DataResult.Error).error)
    }

    @Test
    fun getItem_unknown_id_returns_NotFound() = runBlocking<Unit> {
        assertIs<AppError.NotFound>((items.getItem(42) as DataResult.Error).error)
    }

    @Test
    fun deleteItem_removes_it_and_its_notifications() = runBlocking<Unit> {
        val saved = items.createItem(newItem()).ok()
        notifications.createNotification(NotificationLog(0, saved.id, "2026-11-24", false, ""))
        items.deleteItem(saved.id).ok()
        assertTrue(items.getItems().ok().isEmpty())
        assertTrue(notifications.getNotificationsByItem(saved.id).ok().isEmpty())
    }

    @Test
    fun createItem_with_missing_category_fails_foreign_key() = runBlocking<Unit> {
        assertIs<DataResult.Error>(items.createItem(newItem(categoryId = 9999)))
    }

    @Test
    fun categories_seeded_with_keys() = runBlocking<Unit> {
        val all = categories.getCategories().ok()
        assertEquals(listOf("food", "beauty", "medicine", "other"), all.map { it.key })
    }

    @Test
    fun custom_category_crud_has_null_key() = runBlocking<Unit> {
        val created = categories.createCategory(Category(0, "Hewan", "ic_pet", "#112233", "")).ok()
        assertNull(created.key)
        val renamed = categories.updateCategory(created.copy(name = "Peliharaan")).ok()
        assertEquals("Peliharaan", renamed.name)
        categories.deleteCategory(created.id).ok()
        assertIs<DataResult.Error>(categories.getCategory(created.id))
    }

    @Test
    fun notification_roundtrip() = runBlocking<Unit> {
        val item = items.createItem(newItem()).ok()
        val n = notifications.createNotification(NotificationLog(0, item.id, "2026-11-24", false, "")).ok()
        assertEquals(item.id, n.itemId)
        assertFalse(n.isSent)
        assertEquals(1, notifications.getNotifications().ok().size)
        notifications.deleteNotification(n.id).ok()
        assertTrue(notifications.getNotifications().ok().isEmpty())
    }

    @Test
    fun deleteItem_deletes_its_image_file() = runBlocking<Unit> {
        val saved = items.createItem(newItem().copy(imageUrl = "product_images/a.jpg")).ok()
        items.deleteItem(saved.id).ok()
        assertEquals(listOf("product_images/a.jpg"), images.deleted)
    }

    @Test
    fun deleteItem_without_image_deletes_no_file() = runBlocking<Unit> {
        val saved = items.createItem(newItem()).ok()
        items.deleteItem(saved.id).ok()
        assertTrue(images.deleted.isEmpty())
    }

    @Test
    fun updateItem_replacing_image_deletes_old_file_only() = runBlocking<Unit> {
        val saved = items.createItem(newItem().copy(imageUrl = "product_images/old.jpg")).ok()
        items.updateItem(saved.copy(imageUrl = "product_images/new.jpg")).ok()
        assertEquals(listOf("product_images/old.jpg"), images.deleted)
    }

    @Test
    fun updateItem_keeping_same_image_deletes_nothing() = runBlocking<Unit> {
        val saved = items.createItem(newItem().copy(imageUrl = "product_images/a.jpg")).ok()
        items.updateItem(saved.copy(name = "Renamed")).ok()
        assertTrue(images.deleted.isEmpty())
    }

    @Test
    fun updateItem_removing_image_deletes_old_file() = runBlocking<Unit> {
        val saved = items.createItem(newItem().copy(imageUrl = "product_images/a.jpg")).ok()
        items.updateItem(saved.copy(imageUrl = null)).ok()
        assertEquals(listOf("product_images/a.jpg"), images.deleted)
    }

    private suspend fun awaitUntil(condition: () -> Boolean) = withTimeout(5_000) {
        while (!condition()) delay(10)
    }

    @Test
    fun observeItems_emits_current_state_then_every_change() = runBlocking<Unit> {
        val seen = java.util.concurrent.CopyOnWriteArrayList<List<String>>()
        val job = launch(Dispatchers.Default) {
            items.observeItems().collect { list -> seen += list.map { it.name } }
        }
        awaitUntil { seen.size >= 1 }
        assertEquals(listOf(emptyList()), seen.toList())

        val saved = items.createItem(newItem("Susu")).ok()
        awaitUntil { seen.size >= 2 }
        assertEquals(listOf("Susu"), seen.last())

        items.updateItem(saved.copy(isConsumed = true))
        awaitUntil { seen.size >= 3 }
        items.deleteItem(saved.id)
        awaitUntil { seen.size >= 4 }
        assertEquals(emptyList(), seen.last())
        job.cancel()
    }
}
