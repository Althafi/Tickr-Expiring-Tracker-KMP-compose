package com.project.tickr.data.local.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import com.project.tickr.db.TickrDatabase

actual class DatabaseDriverFactory actual constructor() {
    actual fun create(): SqlDriver = NativeSqliteDriver(
        schema = TickrDatabase.Schema,
        name = DATABASE_NAME,
        onConfiguration = { config: DatabaseConfiguration ->
            config.copy(extendedConfig = config.extendedConfig.copy(foreignKeyConstraints = true))
        },
    )
}
