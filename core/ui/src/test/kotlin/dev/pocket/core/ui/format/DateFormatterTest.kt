package dev.pocket.core.ui.format

import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DateFormatterTest {

    // Pinned so "Today" and "Yesterday" mean something fixed rather than whenever CI runs.
    private val clock: Clock = Clock.fixed(Instant.parse("2026-09-16T12:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 9, 16)

    @Test
    fun `today and yesterday are named, not dated`() {
        assertEquals("Today", DateFormatter.dayHeader(today, clock, Locale.UK))
        assertEquals("Yesterday", DateFormatter.dayHeader(today.minusDays(1), clock, Locale.UK))
    }

    @Test
    fun `older dates in the same year omit the year`() {
        val header = DateFormatter.dayHeader(LocalDate.of(2026, 9, 10), clock, Locale.UK)

        // Asserting the contract -- weekday, day, month, no year -- rather than CLDR's exact
        // month abbreviation, which varies between JDK/CLDR releases ("Sep" vs "Sept").
        assertTrue(header.startsWith("Thu 10 Sep"), "was '$header'")
        assertFalse(header.contains("2026"), "same-year dates omit the year, was '$header'")
    }

    @Test
    fun `dates in another year include it`() {
        val header = DateFormatter.dayHeader(LocalDate.of(2025, 12, 24), clock, Locale.UK)

        assertTrue(header.startsWith("24 Dec"), "was '$header'")
        assertTrue(header.contains("2025"), "other-year dates carry the year, was '$header'")
    }

    @Test
    fun `a future date is not called today`() {
        val header = DateFormatter.dayHeader(today.plusDays(1), clock, Locale.UK)

        assertTrue(header.startsWith("Thu 17 Sep"), "was '$header'")
        assertNotEquals("Today", header)
    }

    @Test
    fun `month label spells out the month and year`() {
        assertEquals("September 2026", DateFormatter.monthLabel(YearMonth.of(2026, 9), Locale.UK))
    }
}
