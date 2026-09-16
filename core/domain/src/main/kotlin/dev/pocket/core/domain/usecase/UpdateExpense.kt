package dev.pocket.core.domain.usecase

import dev.pocket.core.common.result.FailureReason
import dev.pocket.core.common.result.Outcome
import dev.pocket.core.domain.repository.ExpenseRepository
import javax.inject.Inject

/**
 * Validates a draft and writes it over an existing expense. The original `createdAt` is preserved
 * so editing an old entry does not reorder it among same-day entries.
 */
class UpdateExpense @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val validate: ValidateExpenseDraft,
) {
    suspend operator fun invoke(draft: ExpenseDraft): Outcome<Unit> {
        val category = when (val validation = validate(draft)) {
            is Outcome.Failure -> return validation
            is Outcome.Success -> validation.value
        }

        val existing = expenseRepository.getExpense(draft.id)
            ?: return Outcome.Failure(FailureReason.NotFound("Expense ${draft.id}"))

        return runCatching {
            expenseRepository.updateExpense(
                existing.copy(
                    amount = draft.amount,
                    category = category,
                    note = draft.note.trim(),
                    occurredOn = draft.occurredOn,
                    receiptUri = draft.receiptUri,
                ),
            )
        }.fold(
            onSuccess = { Outcome.Success(Unit) },
            onFailure = { Outcome.Failure(FailureReason.Unexpected(it)) },
        )
    }
}
