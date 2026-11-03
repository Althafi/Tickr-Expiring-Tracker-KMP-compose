package com.project.tickr.data.local.settings

/** Penyimpanan key-value sederhana per platform (SharedPreferences / NSUserDefaults). */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

internal expect fun createKeyValueStore(): KeyValueStore
