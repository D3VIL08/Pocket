package dev.pocket.core.model

import java.time.Instant
import java.time.LocalDate

/**
 * A single recorded expense.
 *
 * [occurredOn] is a [LocalDate] rather than an instant because "what day did I spend this?" is a
 * calendar question — a coffee bought at 11pm belongs to that day's total regardless of time zone
 * maths. [createdAt] keeps the real instant for ordering entries made on the same day.
 */
data class Expense(
    val id: Long,
    val amount: Money,
    val category: Category,
    val note: String,
    val occurredOn: LocalDate,
    val createdAt: Instant,
    val receiptUri: String? = null,
) {
    companion object {
        const val NO_ID: Long = 0L
        const val MAX_NOTE_LENGTH: Int = 200
    }
}
