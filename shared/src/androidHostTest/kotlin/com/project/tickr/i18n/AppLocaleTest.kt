package com.project.tickr.i18n

import com.project.tickr.core.util.DateTimeUtilImpl
import com.project.tickr.domain.model.AppLanguage
import com.project.tickr.ui.i18n.normalizeLanguageCode
import com.project.tickr.ui.i18n.resolveLanguageTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppLocaleTest {

    @Test fun explicit_choice_overrides_device_language() {
        assertEquals("id", resolveLanguageTag(AppLanguage.INDONESIAN, "en"))
        assertEquals("en", resolveLanguageTag(AppLanguage.ENGLISH, "id"))
        assertEquals("en", resolveLanguageTag(AppLanguage.ENGLISH, "fr"))
    }

    @Test fun system_follows_supported_device_language() {
        assertEquals("id", resolveLanguageTag(AppLanguage.SYSTEM, "id"))
        assertEquals("en", resolveLanguageTag(AppLanguage.SYSTEM, "en"))
    }

    @Test fun system_with_unsupported_device_language_falls_back_to_english() {
        assertEquals("en", resolveLanguageTag(AppLanguage.SYSTEM, "fr"))
        assertEquals("en", resolveLanguageTag(AppLanguage.SYSTEM, "ja"))
    }

    @Test fun legacy_java_code_for_indonesian_is_normalized() {
        assertEquals("id", normalizeLanguageCode("in"))
        assertEquals("id", resolveLanguageTag(AppLanguage.SYSTEM, "in"))
    }

    @Test fun region_and_script_are_ignored() {
        assertEquals("en", normalizeLanguageCode("en-US"))
        assertEquals("id", normalizeLanguageCode("id_ID"))
        assertEquals("en", resolveLanguageTag(AppLanguage.SYSTEM, "en-GB"))
    }

    @Test fun dateParts_from_iso_gives_iso_weekday() {
        val util = DateTimeUtilImpl()
        // 2026-06-10 adalah hari Rabu
        val parts = util.datePartsFromIso("2026-06-10")!!
        assertEquals(3, parts.dayOfWeek)
        assertEquals(10, parts.day)
        assertEquals(6, parts.month)
        assertEquals(2026, parts.year)
        // 2026-06-14 adalah hari Minggu
        assertEquals(7, util.datePartsFromIso("2026-06-14")!!.dayOfWeek)
    }

    @Test fun dateParts_from_invalid_iso_is_null() {
        assertNull(DateTimeUtilImpl().datePartsFromIso("2026-02-31"))
        assertNull(DateTimeUtilImpl().datePartsFromIso("bukan-tanggal"))
    }
}
