package dev.pocket.core.ui.format

import dev.pocket.core.model.Money
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoneyFormatterTest {

    @Test
    fun `formats US dollars in the US locale`() {
        val formatted = MoneyFormatter.format(Money(123_456, "USD"), Locale.US)

        assertEquals("$1,234.56", formatted)
    }

    @Test
    fun `respects a locale that puts the symbol last`() {
        val formatted = MoneyFormatter.format(Money(123_456, "EUR"), Locale.GERMANY)

        // German convention: comma decimal separator, dot grouping, trailing symbol.
        assertTrue(formatted.startsWith("1.234,56"), "was '$formatted'")
        assertTrue(formatted.contains("€"), "was '$formatted'")
    }

    @Test
    fun `a zero-decimal currency shows no decimal part`() {
        val formatted = MoneyFormatter.format(Money(1_000, "JPY"), Locale.JAPAN)

        assertTrue(formatted.contains("1,000"), "was '$formatted'")
        assertTrue(!formatted.contains("."), "yen has no minor unit, was '$formatted'")
    }

    @Test
    fun `formats a negative amount`() {
        val formatted = MoneyFormatter.format(Money(-500, "USD"), Locale.US)

        assertTrue(formatted.contains("5.00"), "was '$formatted'")
    }

    @Test
    fun `amount only omits the currency symbol`() {
        val formatted = MoneyFormatter.formatAmountOnly(Money(123_456, "USD"), Locale.US)

        assertEquals("1,234.56", formatted)
    }

    @Test
    fun `falls back to the code for an unknown currency`() {
        assertEquals("XYZ", MoneyFormatter.symbolOf("XYZ", Locale.US))
        assertEquals("$", MoneyFormatter.symbolOf("USD", Locale.US))
    }

    @Test
    fun `an unknown currency code still formats without throwing`() {
        val formatted = MoneyFormatter.format(Money(1_000, "XYZ"), Locale.US)

        assertTrue(formatted.isNotBlank())
    }
}
