package dev.pocket.core.model

import java.time.LocalDate
import java.time.YearMonth

/** An inclusive range of calendar days. */
data class DateRange(
    val start: LocalDate,
    val endInclusive: LocalDate,
) {
    init {
        require(!start.isAfter(endInclusive)) { "start $start must not be after end $endInclusive" }
    }

    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(endInclusive)

    companion object {
        fun of(month: YearMonth): DateRange = DateRange(month.atDay(1), month.atEndOfMonth())

        fun singleDay(date: LocalDate): DateRange = DateRange(date, date)
    }
}
