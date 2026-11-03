package com.project.tickr.data.local.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.project.tickr.data.repository.AndroidContextHolder
import com.project.tickr.domain.model.DueReminder
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.reminder.ReminderRules
import com.project.tickr.domain.repository.SettingsRepository
import com.project.tickr.domain.scheduler.ReminderScheduler
import com.project.tickr.presentation.common.notificationBody
import com.project.tickr.presentation.common.notificationTitle
import com.project.tickr.ui.i18n.applyAppLanguage
import org.jetbrains.compose.resources.getString
import tickrapp.shared.generated.resources.Res
import tickrapp.shared.generated.resources.notif_channel_description
import tickrapp.shared.generated.resources.notif_channel_name
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

internal actual fun createReminderScheduler(settings: SettingsRepository): ReminderScheduler =
    AndroidReminderScheduler(settings)

/**
 * Pengingat harian lewat WorkManager periodik pukul 08:00: worker membaca database dan menampilkan
 * pengingat yang jatuh tempo hari ini. Tahan reboot (WorkManager menyimpan jadwal), tanpa server.
 */
internal class AndroidReminderScheduler(private val settings: SettingsRepository) : ReminderScheduler {

    private val context: Context get() = AndroidContextHolder.getContext()

    override suspend fun hasPermission(): Boolean {
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val granted = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return enabled && granted
    }

    override suspend fun syncAll(items: List<Item>) {
        applyAppLanguage(settings.settings.value.language)
        ensureChannel()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(millisUntilNextReminderHour(), TimeUnit.MILLISECONDS)
                .build(),
        )
    }

    override suspend fun deliver(due: List<DueReminder>) {
        if (due.isEmpty() || !hasPermission()) return
        applyAppLanguage(settings.settings.value.language)
        ensureChannel()
        // Hanya pengingat hari ini; yang lebih lama (app lama tak dibuka) sudah basi dan cukup ada di riwayat.
        val today = LocalDate.now().toString()
        val manager = NotificationManagerCompat.from(context)
        due.filter { it.scheduledDate == today }.forEach { reminder ->
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon())
                .setContentTitle(reminder.notificationTitle())
                .setContentText(notificationBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(openAppIntent())
                .build()
            @Suppress("MissingPermission") // sudah dicek lewat hasPermission()
            manager.notify(reminder.itemId.toInt(), notification)
        }
    }

    override suspend fun cancel(itemId: Long) {
        NotificationManagerCompat.from(context).cancel(itemId.toInt())
    }

    override suspend fun cancelAll() {
        NotificationManagerCompat.from(context).cancelAll()
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private suspend fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(Res.string.notif_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = getString(Res.string.notif_channel_description) }
        // Membuat ulang channel yang sama hanya memperbarui nama/deskripsi (mengikuti bahasa).
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun smallIcon(): Int {
        val custom = context.resources.getIdentifier("ic_stat_tickr", "drawable", context.packageName)
        return if (custom != 0) custom else context.applicationInfo.icon
    }

    private fun openAppIntent(): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        return PendingIntent.getActivity(
            context, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun millisUntilNextReminderHour(): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var next = now.toLocalDate().atTime(ReminderRules.HOUR, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return java.time.Duration.between(now, next).toMillis()
    }

    companion object {
        const val WORK_NAME = "tickr_daily_reminders"
        const val CHANNEL_ID = "expiry_reminders"
    }
}
