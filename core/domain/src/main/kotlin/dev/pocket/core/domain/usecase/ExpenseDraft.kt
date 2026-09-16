package dev.pocket.core.domain.usecase

import dev.pocket.core.model.Expense
import dev.pocket.core.model.Money
import java.time.LocalDate

/**
 * An expense as the user has entered it so far, before it has been validated or given an id.
 * Keeping the unvalidated shape separate from [Expense] means an [Expense] that exists is always
 * one that passed the domain rules.
 */
data class ExpenseDraft(
    val id: Long = Expense.NO_ID,
    val amount: Money,
    val categoryId: Long,
    val note: String = "",
    val occurredOn: LocalDate,
    val receiptUri: String? = null,
) {
    val isNew: Boolean get() = id == Expense.NO_ID
}
