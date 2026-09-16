package dev.pocket.feature.expenses.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pocket.core.domain.usecase.AddExpense
import dev.pocket.core.domain.usecase.DeleteExpense
import dev.pocket.core.domain.usecase.ExpenseDraft
import dev.pocket.core.domain.usecase.MonthlySummary
import dev.pocket.core.domain.usecase.ObserveExpenses
import dev.pocket.core.domain.usecase.ObserveMonthlySummary
import dev.pocket.core.model.DateRange
import dev.pocket.core.model.Expense
import dev.pocket.core.model.ExpenseFilter
import dev.pocket.core.model.ExpenseSort
import dev.pocket.core.model.Money
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ExpensesListViewModel @Inject constructor(
    observeExpenses: ObserveExpenses,
    observeMonthlySummary: ObserveMonthlySummary,
    private val deleteExpense: DeleteExpense,
    private val addExpense: AddExpense,
    clock: Clock,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now(clock))

    private val events = Channel<ExpensesListEvent>(Channel.BUFFERED)

    /**
     * One-shot effects (the undo snackbar) go through a channel rather than living in [uiState]:
     * a snackbar is an event that happens once, not a piece of state that should replay on the
     * next configuration change.
     */
    val uiEvents: Flow<ExpensesListEvent> = events.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ExpensesListUiState> = selectedMonth
        .flatMapLatest { month ->
            combine(
                observeExpenses(
                    filter = ExpenseFilter(dateRange = DateRange.of(month)),
                    sort = ExpenseSort.DATE_DESC,
                ),
                observeMonthlySummary(month),
            ) { expenses, summary -> toUiState(month, expenses, summary) }
        }
        .stateIn(
            scope = viewModelScope,
            // Survives a configuration change but releases on real teardown, so rotating the
            // device does not re-run the query.
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ExpensesListUiState.Loading(selectedMonth.value),
        )

    fun onMonthChange(delta: Long) {
        selectedMonth.value = selectedMonth.value.plusMonths(delta)
    }

    fun onMonthSelected(month: YearMonth) {
        selectedMonth.value = month
    }

    /**
     * Deletes straight away and offers an undo instead of asking "are you sure?" first. A
     * confirmation dialog taxes the common case to protect the rare one; undo does the reverse.
     */
    fun onDeleteExpense(expense: Expense) {
        viewModelScope.launch {
            deleteExpense(expense.id)
            events.send(ExpensesListEvent.ExpenseDeleted(expense))
        }
    }

    /** Re-inserts a deleted expense. It gets a new id — the row is gone, not merely hidden. */
    fun onUndoDelete(expense: Expense) {
        viewModelScope.launch {
            addExpense(
                ExpenseDraft(
                    amount = expense.amount,
                    categoryId = expense.category.id,
                    note = expense.note,
                    occurredOn = expense.occurredOn,
                    receiptUri = expense.receiptUri,
                ),
            )
        }
    }

    private fun toUiState(
        month: YearMonth,
        expenses: List<Expense>,
        summary: MonthlySummary,
    ): ExpensesListUiState = if (expenses.isEmpty()) {
        ExpensesListUiState.Empty(month = month, total = summary.total)
    } else {
        ExpensesListUiState.Success(
            month = month,
            total = summary.total,
            days = groupByDay(expenses, summary.total.currencyCode),
        )
    }

    private fun groupByDay(expenses: List<Expense>, currencyCode: String) = expenses
        .groupBy { it.occurredOn }
        .toSortedMap(reverseOrder())
        .map { (date, dayExpenses) ->
            ExpenseDay(
                date = date,
                dayTotal = dayExpenses.fold(Money.zero(currencyCode)) { acc, expense ->
                    // History can contain other currencies after a currency change; only amounts
                    // in the active currency contribute, matching what the SQL aggregate counts.
                    if (expense.amount.currencyCode == currencyCode) acc + expense.amount else acc
                },
                expenses = dayExpenses.sortedByDescending { it.createdAt }.toImmutableList(),
            )
        }
        .toImmutableList()

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

sealed interface ExpensesListEvent {
    data class ExpenseDeleted(val expense: Expense) : ExpensesListEvent
}
