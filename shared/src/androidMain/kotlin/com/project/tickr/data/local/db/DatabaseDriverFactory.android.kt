package com.project.tickr.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.project.tickr.data.repository.AndroidContextHolder
import com.project.tickr.db.TickrDatabase

actual class DatabaseDriverFactory actual constructor() {
    actual fun create(): SqlDriver = AndroidSqliteDriver(
        schema = TickrDatabase.Schema,
        context = AndroidContextHolder.getContext(),
        name = DATABASE_NAME,
        callback = object : AndroidSqliteDriver.Callback(TickrDatabase.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.setForeignKeyConstraintsEnabled(true)
            }
        },
    )
}
