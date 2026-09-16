package dev.pocket.core.model

import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateRangeTest {

    @Test
    fun `a month range covers the whole month inclusively`() {
        val range = DateRange.of(YearMonth.of(2026, 2))

        assertTrue(LocalDate.of(2026, 2, 1) in range)
        // 2026 is not a leap year, so February ends on the 28th.
        assertTrue(LocalDate.of(2026, 2, 28) in range)
        assertFalse(LocalDate.of(2026, 3, 1) in range)
        assertFalse(LocalDate.of(2026, 1, 31) in range)
    }

    @Test
    fun `a leap February includes the 29th`() {
        val range = DateRange.of(YearMonth.of(2028, 2))

        assertTrue(LocalDate.of(2028, 2, 29) in range)
    }

    @Test
    fun `a single day range contains only that day`() {
        val day = LocalDate.of(2026, 9, 16)
        val range = DateRange.singleDay(day)

        assertTrue(day in range)
        assertFalse(day.plusDays(1) in range)
        assertFalse(day.minusDays(1) in range)
    }

    @Test
    fun `rejects a backwards range`() {
        assertFailsWith<IllegalArgumentException> {
            DateRange(LocalDate.of(2026, 9, 16), LocalDate.of(2026, 9, 15))
        }
    }
}
