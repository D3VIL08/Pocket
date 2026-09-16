package dev.pocket.core.domain.usecase

import dev.pocket.core.common.result.FailureReason
import dev.pocket.core.common.result.FailureReason.Validation.Field
import dev.pocket.core.common.result.Outcome
import dev.pocket.core.domain.repository.CategoryRepository
import dev.pocket.core.model.Category
import dev.pocket.core.model.Expense
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * The single place the rules for a valid expense live, so the add screen, the edit screen and any
 * future importer all agree. Returns the resolved [Category] on success because every caller needs
 * it and the lookup has already happened here.
 *
 * [clock] is injected rather than calling [LocalDate.now] directly, so "not in the future" is
 * testable instead of depending on when the suite happens to run.
 */
class ValidateExpenseDraft @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(draft: ExpenseDraft): Outcome<Category> {
        // Rules are declared in the order the user meets them on the form, so the first error
        // they see is for the field they filled in first.
        val fieldRules: List<Rule> = listOf(
            Rule(Field.AMOUNT, "Enter an amount greater than zero") { !it.amount.isZero },
            Rule(Field.AMOUNT, "Amount cannot be negative") { !it.amount.isNegative },
            Rule(
                field = Field.NOTE,
                message = "Note cannot be longer than ${Expense.MAX_NOTE_LENGTH} characters",
            ) { it.note.length <= Expense.MAX_NOTE_LENGTH },
            Rule(Field.DATE, "Date cannot be in the future") {
                !it.occurredOn.isAfter(LocalDate.now(clock))
            },
        )

        val broken = fieldRules.firstOrNull { !it.isSatisfiedBy(draft) }
        if (broken != null) {
            return Outcome.Failure(FailureReason.Validation(broken.field, broken.message))
        }

        // Checked last because, unlike the rules above, it costs a database read.
        val category = categoryRepository.getCategory(draft.categoryId)
        return category?.let { Outcome.Success(it) }
            ?: Outcome.Failure(FailureReason.Validation(Field.CATEGORY, "Pick a category"))
    }

    private class Rule(
        val field: Field,
        val message: String,
        private val predicate: (ExpenseDraft) -> Boolean,
    ) {
        fun isSatisfiedBy(draft: ExpenseDraft): Boolean = predicate(draft)
    }
}
