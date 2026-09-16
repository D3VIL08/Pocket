package dev.pocket.core.ui.format

import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Date labels for list headers and the entry screen.
 *
 * Takes a [Clock] so "Today" and "Yesterday" are testable rather than depending on when the suite
 * happens to run.
 */
object DateFormatter {

    private const val RELATIVE_WINDOW_DAYS = 1L

    /** `Today`, `Yesterday`, `Mon 14 Sep`, or `14 Sep 2025` once the year differs. */
    fun dayHeader(
        date: LocalDate,
        clock: Clock = Clock.systemDefaultZone(),
        locale: Locale = Locale.getDefault(),
    ): String {
        val today = LocalDate.now(clock)
        return when (ChronoUnit.DAYS.between(date, today)) {
            0L -> "Today"
            RELATIVE_WINDOW_DAYS -> "Yesterday"
            else -> {
                val pattern = if (date.year == today.year) "EEE d MMM" else "d MMM yyyy"
                date.format(DateTimeFormatter.ofPattern(pattern, locale))
            }
        }
    }

    /** `September 2026`, for the month header. */
    fun monthLabel(month: YearMonth, locale: Locale = Locale.getDefault()): String =
        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))

    /** A full date for the entry screen's date field. */
    fun fullDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
}
