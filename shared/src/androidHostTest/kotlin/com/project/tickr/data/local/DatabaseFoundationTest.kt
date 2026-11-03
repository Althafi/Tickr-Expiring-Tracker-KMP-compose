package com.project.tickr.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.project.tickr.data.local.db.DatabaseSeeder
import com.project.tickr.db.TickrDatabase
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatabaseFoundationTest {

    private fun newDb(): TickrDatabase {
        val driver = JdbcSqliteDriver(
            JdbcSqliteDriver.IN_MEMORY,
            Properties().apply { put("foreign_keys", "true") },
        )
        TickrDatabase.Schema.create(driver)
        return TickrDatabase(driver)
    }

    @Test
    fun seed_creates_four_default_categories() {
        val db = newDb()
        DatabaseSeeder.seedIfEmpty(db, "2026-01-01")
        val keys = db.categoryQueries.selectAll().executeAsList().map { it.key }
        assertEquals(listOf("food", "beauty", "medicine", "other"), keys)
    }

    @Test
    fun seed_is_idempotent() {
        val db = newDb()
        repeat(3) { DatabaseSeeder.seedIfEmpty(db, "2026-01-01") }
        assertEquals(4L, db.categoryQueries.count().executeAsOne())
    }

    @Test
    fun deleting_item_cascades_to_notification_log() {
        val db = newDb()
        db.itemQueries.insert(null, "Susu", null, "2026-12-01", null, null, 0, 1, "PCS", "t", "t")
        val itemId = db.itemQueries.lastInsertRowId().executeAsOne()
        db.notificationLogQueries.insert(itemId, "2026-11-24", 0, "t")
        assertEquals(1, db.notificationLogQueries.selectByItem(itemId).executeAsList().size)

        db.itemQueries.delete(itemId)

        assertEquals(0, db.notificationLogQueries.selectByItem(itemId).executeAsList().size)
    }

    @Test
    fun deleting_category_sets_item_category_to_null() {
        val db = newDb()
        DatabaseSeeder.seedIfEmpty(db, "2026-01-01")
        val catId = db.categoryQueries.selectAll().executeAsList().first().id
        db.itemQueries.insert(catId, "Susu", null, "2026-12-01", null, null, 0, 1, "PCS", "t", "t")
        val itemId = db.itemQueries.lastInsertRowId().executeAsOne()

        db.categoryQueries.delete(catId)

        assertNull(db.itemQueries.selectById(itemId).executeAsOne().category_id)
    }
}
