package dev.pocket.core.testing.repository

import dev.pocket.core.domain.repository.ExpenseRepository
import dev.pocket.core.model.CategorySpend
import dev.pocket.core.model.DateRange
import dev.pocket.core.model.Expense
import dev.pocket.core.model.ExpenseFilter
import dev.pocket.core.model.ExpenseSort
import dev.pocket.core.model.Money
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * An in-memory [ExpenseRepository] that behaves like the real one: writes are visible to every
 * observer, filters and sorts apply.
 *
 * A hand-written fake rather than a mock, deliberately — a mock would assert that a particular
 * call happened, whereas these tests want to assert what the user ends up seeing.
 */
class TestExpenseRepository : ExpenseRepository {

    private val expenses = MutableStateFlow<List<Expense>>(emptyList())
    private var nextId = 1L

    /** Lets a test make the next write fail, to exercise error paths. */
    var failNextWrite: Throwable? = null

    fun setExpenses(values: List<Expense>) {
        expenses.value = values
        nextId = (values.maxOfOrNull { it.id } ?: 0L) + 1
    }

    fun currentExpenses(): List<Expense> = expenses.value

    override fun observeExpenses(filter: ExpenseFilter, sort: ExpenseSort): Flow<List<Expense>> =
        expenses.map { all -> all.filter { it.matches(filter) }.sortedWith(sort.comparator()) }

    override fun observeExpense(id: Long): Flow<Expense?> =
        expenses.map { all -> all.firstOrNull { it.id == id } }

    override fun observeTotal(range: DateRange, currencyCode: String): Flow<Money> =
        expenses.map { all ->
            all.filter { it.occurredOn in range && it.amount.currencyCode == currencyCode }
                .fold(Money.zero(currencyCode)) { acc, expense -> acc + expense.amount }
        }

    override fun observeCategoryTotals(
        range: DateRange,
        currencyCode: String,
    ): Flow<List<CategorySpend>> = expenses.map { all ->
        all.filter { it.occurredOn in range && it.amount.currencyCode == currencyCode }
            .groupBy { it.category }
            .map { (category, items) ->
                CategorySpend(
                    category = category,
                    total = items.fold(Money.zero(currencyCode)) { acc, e -> acc + e.amount },
                    transactionCount = items.size,
                )
            }
            .sortedByDescending { it.total.minorUnits }
    }

    override suspend fun addExpense(expense: Expense): Long {
        failNextWrite?.let { failNextWrite = null; throw it }
        val id = nextId++
        expenses.update { it + expense.copy(id = id) }
        return id
    }

    override suspend fun updateExpense(expense: Expense) {
        failNextWrite?.let { failNextWrite = null; throw it }
        expenses.update { all -> all.map { if (it.id == expense.id) expense else it } }
    }

    override suspend fun deleteExpense(id: Long) {
        expenses.update { all -> all.filterNot { it.id == id } }
    }

    override suspend fun getExpense(id: Long): Expense? = expenses.value.firstOrNull { it.id == id }

    private fun Expense.matches(filter: ExpenseFilter): Boolean {
        filter.dateRange?.let { if (occurredOn !in it) return false }
        filter.categoryIds?.let { if (it.isNotEmpty() && category.id !in it) return false }
        filter.query?.takeIf { it.isNotBlank() }?.let {
            if (!note.contains(it, ignoreCase = true)) return false
        }
        return true
    }

    private fun ExpenseSort.comparator(): Comparator<Expense> = when (this) {
        ExpenseSort.DATE_DESC ->
            compareByDescending<Expense> { it.occurredOn }.thenByDescending { it.createdAt }
        ExpenseSort.DATE_ASC ->
            compareBy<Expense> { it.occurredOn }.thenBy { it.createdAt }
        ExpenseSort.AMOUNT_DESC -> compareByDescending { it.amount.minorUnits }
        ExpenseSort.AMOUNT_ASC -> compareBy { it.amount.minorUnits }
    }
}
