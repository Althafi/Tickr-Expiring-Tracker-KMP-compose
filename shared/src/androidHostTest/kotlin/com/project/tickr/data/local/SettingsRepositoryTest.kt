package com.project.tickr.data.local

import com.project.tickr.data.local.settings.KeyValueStore
import com.project.tickr.data.repository.SettingsRepositoryImpl
import com.project.tickr.domain.model.AppLanguage
import com.project.tickr.domain.model.AppSettings
import com.project.tickr.domain.model.ThemeMode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeStore(val data: MutableMap<String, String> = mutableMapOf()) : KeyValueStore {
    override fun getString(key: String): String? = data[key]
    override fun putString(key: String, value: String) { data[key] = value }
}

class SettingsRepositoryTest {

    @Test
    fun defaults_are_system_theme_and_system_language() {
        val repo = SettingsRepositoryImpl(FakeStore())
        assertEquals(AppSettings(ThemeMode.SYSTEM, AppLanguage.SYSTEM), repo.settings.value)
    }

    @Test
    fun setThemeMode_updates_flow_immediately() = runBlocking<Unit> {
        val repo = SettingsRepositoryImpl(FakeStore())
        repo.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repo.settings.value.themeMode)
        assertEquals(AppLanguage.SYSTEM, repo.settings.value.language)
    }

    @Test
    fun setLanguage_updates_flow_and_keeps_theme() = runBlocking<Unit> {
        val repo = SettingsRepositoryImpl(FakeStore())
        repo.setThemeMode(ThemeMode.LIGHT)
        repo.setLanguage(AppLanguage.ENGLISH)
        assertEquals(AppSettings(ThemeMode.LIGHT, AppLanguage.ENGLISH), repo.settings.value)
    }

    @Test
    fun settings_survive_recreation_like_an_app_restart() = runBlocking<Unit> {
        val store = FakeStore()
        SettingsRepositoryImpl(store).apply {
            setThemeMode(ThemeMode.DARK)
            setLanguage(AppLanguage.INDONESIAN)
        }
        val reopened = SettingsRepositoryImpl(store)
        assertEquals(AppSettings(ThemeMode.DARK, AppLanguage.INDONESIAN), reopened.settings.value)
    }

    @Test
    fun unknown_stored_values_fall_back_to_defaults() {
        val store = FakeStore(mutableMapOf("theme_mode" to "SEPIA", "language" to "KLINGON"))
        assertEquals(AppSettings(), SettingsRepositoryImpl(store).settings.value)
    }

    @Test
    fun language_tags_match_resource_qualifiers() {
        assertEquals(null, AppLanguage.SYSTEM.tag)
        assertEquals("id", AppLanguage.INDONESIAN.tag)
        assertEquals("en", AppLanguage.ENGLISH.tag)
    }
}
