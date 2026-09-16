package dev.pocket.core.domain.usecase

import dev.pocket.core.domain.repository.ExpenseRepository
import javax.inject.Inject

/** Loads an existing expense back into an editable [ExpenseDraft], or `null` if it is gone. */
class GetExpenseDraft @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(id: Long): ExpenseDraft? =
        expenseRepository.getExpense(id)?.let { expense ->
            ExpenseDraft(
                id = expense.id,
                amount = expense.amount,
                categoryId = expense.category.id,
                note = expense.note,
                occurredOn = expense.occurredOn,
                receiptUri = expense.receiptUri,
            )
        }
}
