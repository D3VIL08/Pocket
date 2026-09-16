package dev.pocket.core.testing.data

import dev.pocket.core.model.Category
import dev.pocket.core.model.CategoryIcon
import dev.pocket.core.model.Expense
import dev.pocket.core.model.Money
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Builders with sensible defaults, so a test only states the field it actually cares about and
 * the assertion is not buried in setup noise.
 */
object TestData {

    const val CURRENCY: String = "USD"

    /** A clock fixed to 2026-09-16, so "today" never shifts under the suite. */
    val fixedClock: Clock = Clock.fixed(Instant.parse("2026-09-16T12:00:00Z"), ZoneOffset.UTC)

    val today: LocalDate = LocalDate.of(2026, 9, 16)

    fun category(
        id: Long = 1L,
        name: String = "Food",
        colorArgb: Int = 0xFFE8743B.toInt(),
        icon: CategoryIcon = CategoryIcon.FOOD,
        isDefault: Boolean = true,
    ) = Category(id = id, name = name, colorArgb = colorArgb, icon = icon, isDefault = isDefault)

    fun money(minorUnits: Long, currencyCode: String = CURRENCY) = Money(minorUnits, currencyCode)

    fun expense(
        id: Long = 1L,
        minorUnits: Long = 1_250L,
        category: Category = category(),
        note: String = "Coffee",
        occurredOn: LocalDate = today,
        createdAt: Instant = Instant.parse("2026-09-16T10:00:00Z"),
        currencyCode: String = CURRENCY,
        receiptUri: String? = null,
    ) = Expense(
        id = id,
        amount = Money(minorUnits, currencyCode),
        category = category,
        note = note,
        occurredOn = occurredOn,
        createdAt = createdAt,
        receiptUri = receiptUri,
    )
}
