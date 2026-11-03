package com.project.tickr.i18n

import com.project.tickr.domain.model.CategoryKeys
import com.project.tickr.domain.model.ItemUnits
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Pengaman terjemahan: mencegah teks terlewat, placeholder salah, atau string yatim. */
class StringResourcesTest {

    private val moduleDir: File = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .map { File(it, "src/commonMain") }
        .first { File(it, "composeResources/values/strings.xml").exists() }

    private fun read(folder: String) =
        File(moduleDir, "composeResources/$folder/strings.xml").readText()

    private val keyRegex = Regex("""<(string|plurals) name="([^"]+)"""")
    private val placeholderRegex = Regex("""%\d+\$[ds]""")

    private fun keys(xml: String) = keyRegex.findAll(xml).map { it.groupValues[2] }.toList()

    /** key → daftar placeholder terurut (untuk plurals digabung dari semua item). */
    private fun placeholders(xml: String): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL).findAll(xml).forEach {
            result[it.groupValues[1]] = placeholderRegex.findAll(it.groupValues[2]).map { m -> m.value }.toList().sorted()
        }
        Regex("""<plurals name="([^"]+)">(.*?)</plurals>""", RegexOption.DOT_MATCHES_ALL).findAll(xml).forEach {
            val items = Regex("""<item quantity="[a-z]+">(.*?)</item>""").findAll(it.groupValues[2]).toList()
            // setiap bentuk (one/other) harus memakai placeholder yang sama
            result[it.groupValues[1]] = items.flatMap { i -> placeholderRegex.findAll(i.groupValues[1]).map { m -> m.value } }.sorted()
        }
        return result
    }

    @Test
    fun indonesian_and_english_have_identical_keys() {
        val id = keys(read("values")).toSet()
        val en = keys(read("values-en")).toSet()
        assertEquals(emptySet(), id - en, "Kunci belum diterjemahkan ke English")
        assertEquals(emptySet(), en - id, "Kunci English tanpa padanan Indonesia")
    }

    @Test
    fun no_duplicate_keys() {
        for (folder in listOf("values", "values-en")) {
            val k = keys(read(folder))
            assertEquals(emptyList(), k.groupBy { it }.filter { it.value.size > 1 }.keys.toList(), "Kunci ganda di $folder")
        }
    }

    @Test
    fun placeholders_match_between_languages() {
        val id = placeholders(read("values"))
        val en = placeholders(read("values-en"))
        for ((key, expected) in id) {
            assertEquals(expected, en[key], "Placeholder berbeda untuk '$key'")
        }
    }

    @Test
    fun no_literal_double_percent_which_compose_resources_does_not_unescape() {
        for (folder in listOf("values", "values-en")) {
            assertTrue("%%" !in read(folder), "Compose Resources tidak memproses '%%' (folder $folder)")
        }
    }

    @Test
    fun every_string_is_referenced_in_code() {
        // Semua source set produksi (commonMain, androidMain, iosMain), bukan folder test.
        val source = moduleDir.parentFile.listFiles().orEmpty()
            .filter { it.isDirectory && it.name.endsWith("Main") }
            .flatMap { it.walkTopDown().toList() }
            .filter { it.extension == "kt" }
            .joinToString("\n") { it.readText() }
        val unused = keys(read("values")).filter { key ->
            !Regex("""\b$key\b""").containsMatchIn(source)
        }
        assertEquals(emptyList(), unused, "String tidak terpakai (hapus atau pakai)")
    }

    @Test
    fun every_default_category_and_unit_has_a_translation() {
        val k = keys(read("values")).toSet()
        CategoryKeys.DEFAULTS.forEach { assertTrue("category_$it" in k, "category_$it hilang") }
        ItemUnits.ALL.forEach { assertTrue("unit_${it.lowercase()}" in k, "unit_${it.lowercase()} hilang") }
    }

    @Test
    fun every_weekday_and_month_exists() {
        val k = keys(read("values")).toSet()
        (1..7).forEach { assertTrue("weekday_$it" in k) }
        (1..12).forEach { assertTrue("month_$it" in k); assertTrue("month_short_$it" in k) }
    }
}
