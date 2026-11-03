package com.project.tickr.data.local.db

import app.cash.sqldelight.db.SqlDriver

/** Membuat [SqlDriver] SQLite per platform. Foreign key WAJIB aktif agar ON DELETE CASCADE bekerja. */
expect class DatabaseDriverFactory() {
    fun create(): SqlDriver
}

internal const val DATABASE_NAME = "tickr.db"
