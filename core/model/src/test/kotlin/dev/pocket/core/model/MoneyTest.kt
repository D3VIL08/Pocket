package dev.pocket.core.model

import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun `adds exactly where floating point would drift`() {
        // 0.1 + 0.2 != 0.3 in binary floating point; in minor units it is simply 10 + 20 == 30.
        val result = Money(10, "USD") + Money(20, "USD")

        assertEquals(Money(30, "USD"), result)
        assertEquals(BigDecimal("0.30"), result.toBigDecimal())
    }

    @Test
    fun `summing a hundred cents gives exactly one unit`() {
        val total = List(100) { Money(1, "USD") }.sum("USD")

        assertEquals(Money(100, "USD"), total)
        assertEquals(BigDecimal("1.00"), total.toBigDecimal())
    }

    @Test
    fun `refuses to combine different currencies`() {
        assertFailsWith<IllegalArgumentException> { Money(100, "USD") + Money(100, "EUR") }
    }

    @Test
    fun `rejects a currency code that is not three letters`() {
        assertFailsWith<IllegalArgumentException> { Money(100, "US") }
    }

    @Test
    fun `uses the currency's own precision`() {
        assertEquals(2, Money(100, "USD").fractionDigits)
        // The yen has no minor unit, so 100 minor units is 100 yen, not 1.00.
        assertEquals(0, Money(100, "JPY").fractionDigits)
        assertEquals(BigDecimal("100"), Money(100, "JPY").toBigDecimal())
    }

    @Test
    fun `fromDecimal rounds half up to the currency precision`() {
        assertEquals(Money(1_235, "USD"), Money.fromDecimal(BigDecimal("12.345"), "USD"))
        assertEquals(Money(1_234, "USD"), Money.fromDecimal(BigDecimal("12.344"), "USD"))
    }

    @Test
    fun `parses plain and grouped input`() {
        assertEquals(Money(1_234, "USD"), Money.parseOrNull("12.34", "USD"))
        assertEquals(Money(123_456, "USD"), Money.parseOrNull("1,234.56", "USD"))
        assertEquals(Money(1_200, "USD"), Money.parseOrNull("  12  ", "USD"))
    }

    @Test
    fun `returns null for input that is not an amount`() {
        assertNull(Money.parseOrNull("", "USD"))
        assertNull(Money.parseOrNull("abc", "USD"))
        assertNull(Money.parseOrNull("12.34.56", "USD"))
    }

    @Test
    fun `compares and negates`() {
        assertTrue(Money(200, "USD") > Money(100, "USD"))
        assertEquals(Money(-100, "USD"), -Money(100, "USD"))
        assertEquals(Money(100, "USD"), Money(-100, "USD").abs())
    }

    @Test
    fun `detects overflow rather than wrapping around`() {
        assertFailsWith<ArithmeticException> {
            Money(Long.MAX_VALUE, "USD") + Money(1, "USD")
        }
    }

    @Test
    fun `zero reports itself as zero`() {
        assertTrue(Money.zero("USD").isZero)
        assertTrue(Money(1, "USD").isPositive)
        assertTrue(Money(-1, "USD").isNegative)
    }
}
