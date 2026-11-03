package com.project.tickr.data.local.reminder

import com.project.tickr.domain.model.DueReminder
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.reminder.ReminderRules
import com.project.tickr.domain.repository.SettingsRepository
import com.project.tickr.domain.scheduler.ReminderScheduler
import com.project.tickr.presentation.common.notificationBody
import com.project.tickr.presentation.common.notificationTitle
import com.project.tickr.ui.i18n.applyAppLanguage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

internal actual fun createReminderScheduler(settings: SettingsRepository): ReminderScheduler =
    IosReminderScheduler(settings)

/**
 * `UNUserNotificationCenter` dengan trigger kalender. iOS membatasi 64 notifikasi tertunda per app,
 * jadi hanya yang terdekat dijadwalkan dan semuanya dijadwal ulang setiap [syncAll] (app dibuka, data berubah,
 * bahasa berganti).
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosReminderScheduler(private val settings: SettingsRepository) : ReminderScheduler {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun hasPermission(): Boolean = suspendCancellableCoroutine { cont ->
        center.getNotificationSettingsWithCompletionHandler { s ->
            val status = s?.authorizationStatus
            cont.resume(status == UNAuthorizationStatusAuthorized || status == UNAuthorizationStatusProvisional)
        }
    }

    override suspend fun syncAll(items: List<Item>) {
        applyAppLanguage(settings.settings.value.language)
        removeAllOurRequests()

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val upcoming = items.flatMap { item ->
            val expiry = runCatching { LocalDate.parse(item.expiryDate) }.getOrNull() ?: return@flatMap emptyList()
            ReminderRules.upcoming(expiry, now).map { slot -> Triple(item, slot.daysBefore, slot.at) }
        }.sortedBy { it.third }.take(MAX_SCHEDULED)

        for ((item, daysBefore, at) in upcoming) {
            val reminder = DueReminder(item.id, item.name, daysBefore, at.date.toString())
            val content = UNMutableNotificationContent().apply {
                setTitle(reminder.notificationTitle())
                setBody(notificationBody())
                setSound(UNNotificationSound.defaultSound)
            }
            val components = NSDateComponents().apply {
                setYear(at.year.toLong())
                setMonth(at.month.number.toLong())
                setDay(at.day.toLong())
                setHour(at.hour.toLong())
                setMinute(at.minute.toLong())
            }
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = false)
            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = identifier(item.id, daysBefore),
                content = content,
                trigger = trigger,
            )
            center.addNotificationRequest(request, withCompletionHandler = null)
        }
    }

    // iOS menampilkan notifikasi terjadwal sendiri; yang perlu dilakukan app hanya mencatat riwayat.
    override suspend fun deliver(due: List<DueReminder>) = Unit

    override suspend fun cancel(itemId: Long) {
        val ids = ReminderRules.DAYS_BEFORE.map { identifier(itemId, it) }
        center.removePendingNotificationRequestsWithIdentifiers(ids)
        center.removeDeliveredNotificationsWithIdentifiers(ids)
    }

    override suspend fun cancelAll() {
        removeAllOurRequests()
        center.removeAllDeliveredNotifications()
    }

    private suspend fun removeAllOurRequests() {
        val ids = suspendCancellableCoroutine<List<String>> { cont ->
            center.getPendingNotificationRequestsWithCompletionHandler { requests ->
                cont.resume(
                    requests.orEmpty().mapNotNull { (it as? UNNotificationRequest)?.identifier }
                        .filter { it.startsWith(PREFIX) },
                )
            }
        }
        if (ids.isNotEmpty()) center.removePendingNotificationRequestsWithIdentifiers(ids)
    }

    private fun identifier(itemId: Long, daysBefore: Int) = "$PREFIX$itemId.$daysBefore"

    private companion object {
        const val PREFIX = "tickr.reminder."
        const val MAX_SCHEDULED = 60 // batas sistem 64; sisakan ruang
    }
}
