package dev.pocket.core.domain.repository

import dev.pocket.core.model.CategorySpend
import dev.pocket.core.model.DateRange
import dev.pocket.core.model.Expense
import dev.pocket.core.model.ExpenseFilter
import dev.pocket.core.model.ExpenseSort
import dev.pocket.core.model.Money
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes expenses. Implemented in `:core:data` over Room; the UI and use cases only
 * ever see this interface, which is what keeps the app testable without a database and leaves
 * room for a sync-aware implementation in V2.
 *
 * Reads return [Flow] so the UI updates itself after a write with no manual refresh.
 */
interface ExpenseRepository {

    fun observeExpenses(
        filter: ExpenseFilter = ExpenseFilter.All,
        sort: ExpenseSort = ExpenseSort.DATE_DESC,
    ): Flow<List<Expense>>

    fun observeExpense(id: Long): Flow<Expense?>

    /** Total spend over [range]; emits zero in [currencyCode] when nothing matches. */
    fun observeTotal(range: DateRange, currencyCode: String): Flow<Money>

    /** Per-category totals over [range], highest first — the dashboard's data source. */
    fun observeCategoryTotals(range: DateRange, currencyCode: String): Flow<List<CategorySpend>>

    /** Inserts [expense] and returns the new row id. */
    suspend fun addExpense(expense: Expense): Long

    suspend fun updateExpense(expense: Expense)

    suspend fun deleteExpense(id: Long)

    suspend fun getExpense(id: Long): Expense?
}
