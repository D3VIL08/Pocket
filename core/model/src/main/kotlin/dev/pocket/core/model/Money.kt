package dev.pocket.core.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

/**
 * An amount of money held as a whole number of minor units (cents, paise, pence).
 *
 * Money is deliberately never a [Double]. Binary floating point cannot represent most decimal
 * fractions exactly, so `0.1 + 0.2 != 0.3` and repeated arithmetic silently drifts — unacceptable
 * when the numbers are somebody's spending. Storing minor units as a [Long] makes addition exact
 * and overflow the only failure mode, and it maps cleanly onto a single SQLite INTEGER column.
 *
 * @property minorUnits the amount in the currency's smallest unit; negative values are allowed so
 *   that refunds and deltas are representable.
 * @property currencyCode an ISO 4217 code such as `USD`, `EUR` or `INR`.
 */
data class Money(
    val minorUnits: Long,
    val currencyCode: String,
) : Comparable<Money> {

    init {
        require(currencyCode.length == ISO_CODE_LENGTH) {
            "currencyCode must be a 3-letter ISO 4217 code, was '$currencyCode'"
        }
    }

    /** Number of decimal places this currency uses — 2 for USD, 0 for JPY, 3 for KWD. */
    val fractionDigits: Int
        get() = runCatching { Currency.getInstance(currencyCode).defaultFractionDigits }
            .getOrDefault(DEFAULT_FRACTION_DIGITS)
            .coerceAtLeast(0)

    val isZero: Boolean get() = minorUnits == 0L
    val isPositive: Boolean get() = minorUnits > 0L
    val isNegative: Boolean get() = minorUnits < 0L

    /** The amount as an exact decimal, e.g. `1234` minor units of USD becomes `12.34`. */
    fun toBigDecimal(): BigDecimal = BigDecimal.valueOf(minorUnits, fractionDigits)

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = Math.addExact(minorUnits, other.minorUnits))
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = Math.subtractExact(minorUnits, other.minorUnits))
    }

    operator fun unaryMinus(): Money = copy(minorUnits = -minorUnits)

    fun abs(): Money = if (isNegative) -this else this

    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return minorUnits.compareTo(other.minorUnits)
    }

    private fun requireSameCurrency(other: Money) = require(currencyCode == other.currencyCode) {
        "Cannot combine $currencyCode with ${other.currencyCode}"
    }

    companion object {
        private const val ISO_CODE_LENGTH = 3
        private const val DEFAULT_FRACTION_DIGITS = 2

        /** `USD` unless the user picks otherwise; the real default comes from device locale at runtime. */
        const val FALLBACK_CURRENCY_CODE: String = "USD"

        fun zero(currencyCode: String): Money = Money(0L, currencyCode)

        /**
         * Builds a [Money] from a decimal amount, rounding half-up to the currency's precision.
         * Half-up matches what a person expects when they type `1.005` into a currency field.
         */
        fun fromDecimal(amount: BigDecimal, currencyCode: String): Money {
            val digits = runCatching { Currency.getInstance(currencyCode).defaultFractionDigits }
                .getOrDefault(DEFAULT_FRACTION_DIGITS)
                .coerceAtLeast(0)
            val scaled = amount.setScale(digits, RoundingMode.HALF_UP)
            return Money(scaled.movePointRight(digits).longValueExact(), currencyCode)
        }

        /**
         * Parses user input such as `"12.34"`, returning `null` when the text is not a valid
         * amount. Grouping separators and surrounding whitespace are tolerated; anything else is
         * rejected rather than silently coerced.
         */
        fun parseOrNull(input: String, currencyCode: String): Money? {
            val normalised = input.trim().replace(",", "").replace(" ", "")
            if (normalised.isEmpty()) return null
            val decimal = normalised.toBigDecimalOrNull() ?: return null
            return runCatching { fromDecimal(decimal, currencyCode) }.getOrNull()
        }
    }
}

/** Sums money that is known to share a currency; returns zero in [currencyCode] when empty. */
fun Iterable<Money>.sum(currencyCode: String): Money =
    fold(Money.zero(currencyCode)) { acc, money -> acc + money }
