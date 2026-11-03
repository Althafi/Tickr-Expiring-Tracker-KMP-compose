package com.project.tickr.data.local.reminder

import com.project.tickr.data.local.settings.KeyValueStore
import com.project.tickr.domain.repository.ReminderPreferences
import com.project.tickr.domain.repository.SettingsRepository
import com.project.tickr.domain.scheduler.ReminderScheduler

internal expect fun createReminderScheduler(settings: SettingsRepository): ReminderScheduler

internal class ReminderPreferencesImpl(private val store: KeyValueStore) : ReminderPreferences {
    override fun hasAskedPermission(): Boolean = store.getString(KEY_ASKED) == "1"
    override fun markPermissionAsked() = store.putString(KEY_ASKED, "1")

    private companion object {
        const val KEY_ASKED = "notification_permission_asked"
    }
}
