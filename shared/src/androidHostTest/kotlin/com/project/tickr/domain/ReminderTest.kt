package com.project.tickr.domain

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.project.tickr.core.result.DataResult
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.core.util.DateTimeUtilImpl
import com.project.tickr.data.local.datasource.ItemLocalDataSource
import com.project.tickr.data.local.datasource.NotificationLocalDataSource
import com.project.tickr.data.repository.LocalItemRepository
import com.project.tickr.data.repository.LocalNotificationRepository
import com.project.tickr.db.TickrDatabase
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.reminder.ReminderRules
import com.project.tickr.domain.storage.ImageStorage
import com.project.tickr.domain.usecase.reminder.GetNotificationHistoryUseCase
import com.project.tickr.domain.usecase.reminder.ProcessDueRemindersUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun at(y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0) = LocalDateTime(y, m, d, h, min)
private fun LocalDateTime.millis() = toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
private fun LocalDateTime.iso() = toInstant(TimeZone.currentSystemDefault()).toString()

class ReminderRulesTest {

    private val expiry = LocalDate(2026, 12, 10)

    @Test fun slots_are_h7_h3_h1_h0_at_0800() {
        val slots = ReminderRules.slots(expiry)
        assertEquals(listOf(7, 3, 1, 0), slots.map { it.daysBefore })
        assertEquals(
            listOf(at(2026, 12, 3, 8), at(2026, 12, 7, 8), at(2026, 12, 9, 8), at(2026, 12, 10, 8)),
            slots.map { it.at },
        )
    }

    @Test fun slots_cross_month_and_leap_year_boundaries() {
        assertEquals(LocalDate(2026, 2, 26), ReminderRules.slots(LocalDate(2026, 3, 5)).first().at.date)
        val leap = ReminderRules.slots(LocalDate(2028, 3, 3))
        assertEquals(LocalDate(2028, 2, 25), leap[0].at.date)
        assertEquals(LocalDate(2028, 2, 29), leap[1].at.date)
    }

    @Test fun upcoming_includes_all_when_far_ahead() =
        assertEquals(4, ReminderRules.upcoming(expiry, at(2026, 12, 1, 10)).size)

    @Test fun upcoming_skips_slots_already_passed() =
        assertEquals(listOf(3, 1, 0), ReminderRules.upcoming(expiry, at(2026, 12, 4)).map { it.daysBefore })

    @Test fun upcoming_excludes_a_slot_exactly_at_now() =
        assertEquals(listOf(3, 1, 0), ReminderRules.upcoming(expiry, at(2026, 12, 3, 8)).map { it.daysBefore })

    @Test fun upcoming_keeps_a_slot_one_minute_in_the_future() =
        assertEquals(listOf(7, 3, 1, 0), ReminderRules.upcoming(expiry, at(2026, 12, 3, 7, 59)).map { it.daysBefore })

    @Test fun upcoming_is_empty_after_expiry_day_reminder() =
        assertTrue(ReminderRules.upcoming(expiry, at(2026, 12, 10, 8, 1)).isEmpty())

    @Test fun due_only_includes_slots_passed_and_after_creation() {
        val due = ReminderRules.due(expiry, createdAt = at(2026, 12, 1), now = at(2026, 12, 8, 12))
        assertEquals(listOf(7, 3), due.map { it.daysBefore })
    }

    @Test fun due_ignores_slots_before_the_item_was_created() {
        val due = ReminderRules.due(expiry, createdAt = at(2026, 12, 5), now = at(2026, 12, 8, 12))
        assertEquals(listOf(3), due.map { it.daysBefore })
    }

    @Test fun due_ignores_a_slot_earlier_the_same_day_as_creation() {
        val due = ReminderRules.due(expiry, createdAt = at(2026, 12, 3, 9), now = at(2026, 12, 3, 12))
        assertTrue(due.isEmpty())
    }
}

private class FixedClock(private var millis: Long) : DateTimeUtil by DateTimeUtilImpl() {
    override fun nowEpochMillis() = millis
    fun set(m: Long) { millis = m }
}

private class NoopImages : ImageStorage {
    override suspend fun saveProductImage(sourcePath: String) = DataResult.Success(sourcePath)
    override suspend fun delete(path: String) = DataResult.Success(Unit)
    override fun resolve(path: String) = path
}

class ProcessDueRemindersTest {

    private var createdIso = at(2026, 12, 1, 9).iso()
    private val clock = FixedClock(at(2026, 12, 1, 9).millis())

    private val db = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, Properties().apply { put("foreign_keys", "true") })
        .let { TickrDatabase.Schema.create(it); TickrDatabase(it) }
    private val items = LocalItemRepository(ItemLocalDataSource(db) { createdIso }, NoopImages())
    private val notifications = LocalNotificationRepository(NotificationLocalDataSource(db))
    private val process = ProcessDueRemindersUseCase(items, notifications, clock)
    private val history = GetNotificationHistoryUseCase(notifications, items)

    private suspend fun add(name: String, expiry: String, consumed: Boolean = false): Item =
        (items.createItem(
            Item(0, "x", null, name, null, expiry, null, null, consumed, 1, "PCS", "", ""),
        ) as DataResult.Success).data

    private fun due() = runBlocking { (process() as DataResult.Success).data }

    @Test fun nothing_is_due_right_after_adding() = runBlocking<Unit> {
        add("Susu", "2026-12-10")
        assertTrue(due().isEmpty())
    }

    @Test fun records_each_due_reminder_exactly_once() = runBlocking<Unit> {
        add("Susu", "2026-12-10")
        clock.set(at(2026, 12, 8, 12).millis())
        assertEquals(listOf(7, 3), due().map { it.daysBefore })
        assertTrue(due().isEmpty(), "pemanggilan kedua tidak boleh mencatat ulang")
    }

    @Test fun later_run_only_returns_the_newly_due_reminder() = runBlocking<Unit> {
        add("Susu", "2026-12-10")
        clock.set(at(2026, 12, 8, 12).millis()); due()
        clock.set(at(2026, 12, 9, 8, 30).millis())
        val newOnes = due()
        assertEquals(listOf(1), newOnes.map { it.daysBefore })
        assertEquals("2026-12-09", newOnes.single().scheduledDate)
        assertEquals("Susu", newOnes.single().itemName)
    }

    @Test fun consumed_items_get_no_reminders() = runBlocking<Unit> {
        add("Habis", "2026-12-10", consumed = true)
        clock.set(at(2026, 12, 10, 12).millis())
        assertTrue(due().isEmpty())
    }

    @Test fun items_added_after_a_slot_do_not_get_that_slot() = runBlocking<Unit> {
        createdIso = at(2026, 12, 5, 9).iso()
        add("Telat dicatat", "2026-12-10")
        clock.set(at(2026, 12, 10, 12).millis())
        assertEquals(listOf(3, 1, 0), due().map { it.daysBefore })
    }

    @Test fun history_is_newest_first_with_days_before() = runBlocking<Unit> {
        add("Susu", "2026-12-10")
        clock.set(at(2026, 12, 10, 12).millis()); due()
        val entries = (history() as DataResult.Success).data
        assertEquals(listOf(0, 1, 3, 7), entries.map { it.daysBefore })
        assertEquals("2026-12-10", entries.first().scheduledDate)
    }

    @Test fun deleting_an_item_removes_its_history() = runBlocking<Unit> {
        val item = add("Susu", "2026-12-10")
        clock.set(at(2026, 12, 10, 12).millis()); due()
        items.deleteItem(item.id)
        assertTrue((history() as DataResult.Success).data.isEmpty())
    }
}
