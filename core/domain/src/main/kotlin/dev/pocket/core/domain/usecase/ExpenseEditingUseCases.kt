package dev.pocket.core.domain.usecase

import javax.inject.Inject

/**
 * The four write/read operations the entry form needs, bundled so a screen that legitimately uses
 * all of them declares one dependency rather than four.
 *
 * Grouping is by the screen's job, not by type: this is the "edit an expense" capability, and a
 * caller that only lists expenses still injects [ObserveExpenses] on its own.
 */
class ExpenseEditingUseCases @Inject constructor(
    val getDraft: GetExpenseDraft,
    val add: AddExpense,
    val update: UpdateExpense,
    val delete: DeleteExpense,
)
