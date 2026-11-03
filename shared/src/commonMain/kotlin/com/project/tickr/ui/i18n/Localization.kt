package com.project.tickr.ui.i18n

import androidx.compose.runtime.Composable
import com.project.tickr.core.util.DateParts
import com.project.tickr.domain.model.CategoryKeys
import com.project.tickr.domain.model.ItemUnits
import com.project.tickr.domain.model.Urgency
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tickrapp.shared.generated.resources.*

private val WEEKDAYS: List<StringResource> = listOf(
    Res.string.weekday_1, Res.string.weekday_2, Res.string.weekday_3, Res.string.weekday_4,
    Res.string.weekday_5, Res.string.weekday_6, Res.string.weekday_7,
)

private val MONTHS: List<StringResource> = listOf(
    Res.string.month_1, Res.string.month_2, Res.string.month_3, Res.string.month_4,
    Res.string.month_5, Res.string.month_6, Res.string.month_7, Res.string.month_8,
    Res.string.month_9, Res.string.month_10, Res.string.month_11, Res.string.month_12,
)

private val MONTHS_SHORT: List<StringResource> = listOf(
    Res.string.month_short_1, Res.string.month_short_2, Res.string.month_short_3, Res.string.month_short_4,
    Res.string.month_short_5, Res.string.month_short_6, Res.string.month_short_7, Res.string.month_short_8,
    Res.string.month_short_9, Res.string.month_short_10, Res.string.month_short_11, Res.string.month_short_12,
)

/** Nama kategori: kategori default dari resource (menurut `key`), kategori buatan pengguna memakai [name]. */
@Composable
fun categoryLabel(key: String?, name: String): String = when (key) {
    CategoryKeys.FOOD -> stringResource(Res.string.category_food)
    CategoryKeys.BEAUTY -> stringResource(Res.string.category_beauty)
    CategoryKeys.MEDICINE -> stringResource(Res.string.category_medicine)
    CategoryKeys.OTHER -> stringResource(Res.string.category_other)
    else -> name
}

/** Satuan dari kunci (`PCS`, …). Nilai lama berbahasa Indonesia ("Botol") dikenali; yang tak dikenal ditampilkan apa adanya. */
@Composable
fun unitLabel(unit: String): String = when (unit.uppercase()) {
    ItemUnits.PCS -> stringResource(Res.string.unit_pcs)
    ItemUnits.GRAM -> stringResource(Res.string.unit_gram)
    ItemUnits.KILOGRAM -> stringResource(Res.string.unit_kilogram)
    ItemUnits.LITER -> stringResource(Res.string.unit_liter)
    ItemUnits.MILLILITER -> stringResource(Res.string.unit_milliliter)
    ItemUnits.BOTTLE, "BOTOL" -> stringResource(Res.string.unit_bottle)
    ItemUnits.PACK, "BUNGKUS" -> stringResource(Res.string.unit_pack)
    ItemUnits.DOZEN, "LUSIN" -> stringResource(Res.string.unit_dozen)
    else -> unit
}

@Composable
fun urgencyLabel(urgency: Urgency): String = stringResource(
    when (urgency) {
        Urgency.CRITICAL -> Res.string.urgency_critical
        Urgency.WARNING -> Res.string.urgency_warning
        Urgency.SAFE -> Res.string.urgency_safe
    }
)

/** "Lewat 3 hari" / "Hari ini" / "5 hari" / "2 bulan 10 hari". */
@Composable
fun countdownText(days: Int): String = when {
    days < 0 -> pluralStringResource(Res.plurals.countdown_overdue, -days, -days)
    days == 0 -> stringResource(Res.string.countdown_today)
    days < 30 -> pluralStringResource(Res.plurals.countdown_days, days, days)
    else -> {
        val months = days / 30
        val rest = days % 30
        val monthText = pluralStringResource(Res.plurals.countdown_months, months, months)
        if (rest == 0) monthText
        else "$monthText ${pluralStringResource(Res.plurals.countdown_days, rest, rest)}"
    }
}

/** "Rabu, 10 Juni 2026" / "Wednesday, 10 June 2026". */
@Composable
fun formatLongDate(parts: DateParts): String = stringResource(
    Res.string.date_long,
    stringResource(WEEKDAYS[(parts.dayOfWeek - 1).coerceIn(0, 6)]),
    parts.day,
    stringResource(MONTHS[(parts.month - 1).coerceIn(0, 11)]),
    parts.year,
)

/** "10 Jun 2026" dari tanggal ISO `YYYY-MM-DD`; teks asli dikembalikan bila format tidak valid. */
@Composable
fun formatShortDate(isoDate: String): String {
    val p = isoDate.split("-")
    val year = p.getOrNull(0)?.toIntOrNull()
    val month = p.getOrNull(1)?.toIntOrNull()
    val day = p.getOrNull(2)?.toIntOrNull()
    if (year == null || month == null || day == null || month !in 1..12) return isoDate
    return stringResource(Res.string.date_short, day, stringResource(MONTHS_SHORT[month - 1]), year)
}
