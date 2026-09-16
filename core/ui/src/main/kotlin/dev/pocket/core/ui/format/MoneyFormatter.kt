package dev.pocket.core.ui.format

import dev.pocket.core.model.Money
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Formats money for display. This is the only place currency formatting happens, so a rupee user
 * and a euro user both get their own conventions — symbol placement, grouping and decimal
 * separator all come from [NumberFormat] rather than from string concatenation.
 */
object MoneyFormatter {

    /** e.g. `$1,234.56`, `1.234,56 €`, `₹1,234.56` depending on [locale] and the currency. */
    fun format(money: Money, locale: Locale = Locale.getDefault()): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        runCatching { Currency.getInstance(money.currencyCode) }
            .onSuccess { currency ->
                format.currency = currency
                format.minimumFractionDigits = currency.defaultFractionDigits.coerceAtLeast(0)
                format.maximumFractionDigits = currency.defaultFractionDigits.coerceAtLeast(0)
            }
        return format.format(money.toBigDecimal())
    }

    /**
     * The bare number with no currency symbol, for the entry field where the symbol is already
     * shown beside the input.
     */
    fun formatAmountOnly(money: Money, locale: Locale = Locale.getDefault()): String {
        val format = NumberFormat.getNumberInstance(locale)
        format.minimumFractionDigits = money.fractionDigits
        format.maximumFractionDigits = money.fractionDigits
        return format.format(money.toBigDecimal())
    }

    /** Just the symbol (`$`, `€`, `₹`), for labelling an input field. */
    fun symbolOf(currencyCode: String, locale: Locale = Locale.getDefault()): String =
        runCatching { Currency.getInstance(currencyCode).getSymbol(locale) }
            .getOrDefault(currencyCode)
}
