package dev.pocket.feature.expenses.list

import dev.pocket.core.model.Expense
import dev.pocket.core.model.Money
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate
import java.time.YearMonth

/**
 * Everything the list screen renders, as one value.
 *
 * A sealed state rather than a bag of nullable fields: it makes "loading with data already on
 * screen" and "empty because nothing matched" distinct cases the UI must handle, instead of
 * states that can be silently confused.
 */
sealed interface ExpensesListUiState {

    val month: YearMonth

    data class Loading(override val month: YearMonth) : ExpensesListUiState

    /** The month has no expenses at all. */
    data class Empty(
        override val month: YearMonth,
        val total: Money,
    ) : ExpensesListUiState

    data class Success(
        override val month: YearMonth,
        val total: Money,
        val days: ImmutableList<ExpenseDay>,
    ) : ExpensesListUiState {
        val transactionCount: Int get() = days.sumOf { it.expenses.size }
    }
}

/** One date header plus the expenses recorded under it. */
data class ExpenseDay(
    val date: LocalDate,
    val dayTotal: Money,
    val expenses: ImmutableList<Expense>,
)

/** A delete the user can still undo; held until the snackbar is dismissed. */
data class PendingDelete(val expense: Expense)
