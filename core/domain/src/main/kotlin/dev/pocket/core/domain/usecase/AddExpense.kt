package dev.pocket.core.domain.usecase

import dev.pocket.core.common.result.FailureReason
import dev.pocket.core.common.result.Outcome
import dev.pocket.core.domain.repository.ExpenseRepository
import dev.pocket.core.model.Expense
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/** Validates a draft and stores it, returning the new expense's id. */
class AddExpense @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val validate: ValidateExpenseDraft,
    private val clock: Clock,
) {
    suspend operator fun invoke(draft: ExpenseDraft): Outcome<Long> {
        val category = when (val validation = validate(draft)) {
            is Outcome.Failure -> return validation
            is Outcome.Success -> validation.value
        }

        return runCatching {
            expenseRepository.addExpense(
                Expense(
                    id = Expense.NO_ID,
                    amount = draft.amount,
                    category = category,
                    note = draft.note.trim(),
                    occurredOn = draft.occurredOn,
                    createdAt = Instant.now(clock),
                    receiptUri = draft.receiptUri,
                ),
            )
        }.fold(
            onSuccess = { Outcome.Success(it) },
            onFailure = { Outcome.Failure(FailureReason.Unexpected(it)) },
        )
    }
}
