package com.project.tickr.domain.repository

/** Mengingat apakah izin notifikasi sudah pernah diminta, supaya tidak ditanyakan berulang. */
interface ReminderPreferences {
    fun hasAskedPermission(): Boolean
    fun markPermissionAsked()
}
