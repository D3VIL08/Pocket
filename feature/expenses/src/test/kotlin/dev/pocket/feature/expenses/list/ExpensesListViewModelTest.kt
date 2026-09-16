package dev.pocket.feature.expenses.list

import app.cash.turbine.test
import dev.pocket.core.domain.usecase.AddExpense
import dev.pocket.core.domain.usecase.DeleteExpense
import dev.pocket.core.domain.usecase.ObserveExpenses
import dev.pocket.core.domain.usecase.ObserveMonthlySummary
import dev.pocket.core.domain.usecase.ValidateExpenseDraft
import dev.pocket.core.testing.data.TestData
import dev.pocket.core.testing.repository.TestCategoryRepository
import dev.pocket.core.testing.repository.TestExpenseRepository
import dev.pocket.core.testing.repository.TestUserPreferencesRepository
import dev.pocket.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var expenseRepository: TestExpenseRepository
    private lateinit var viewModel: ExpensesListViewModel

    @Before
    fun setUp() {
        expenseRepository = TestExpenseRepository()
        val categoryRepository = TestCategoryRepository()
        viewModel = ExpensesListViewModel(
            observeExpenses = ObserveExpenses(expenseRepository),
            observeMonthlySummary = ObserveMonthlySummary(
                expenseRepository,
                TestUserPreferencesRepository(),
            ),
            deleteExpense = DeleteExpense(expenseRepository),
            addExpense = AddExpense(
                expenseRepository,
                ValidateExpenseDraft(categoryRepository, TestData.fixedClock),
                TestData.fixedClock,
            ),
            clock = TestData.fixedClock,
        )
    }

    @Test
    fun `starts in Loading before anything is collected`() {
        assertIs<ExpensesListUiState.Loading>(viewModel.uiState.value)
        assertEquals(YearMonth.of(2026, 9), viewModel.uiState.value.month)
    }

    @Test
    fun `becomes Empty when the month has no expenses`() = runTest {
        viewModel.uiState.test {
            assertIs<ExpensesListUiState.Loading>(awaitItem())

            val empty = assertIs<ExpensesListUiState.Empty>(awaitItem())
            assertEquals(0L, empty.total.minorUnits)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `groups expenses by day, most recent day first`() = runTest {
        expenseRepository.setExpenses(
            listOf(
                TestData.expense(id = 1, minorUnits = 1_000, occurredOn = LocalDate.of(2026, 9, 14)),
                TestData.expense(id = 2, minorUnits = 2_000, occurredOn = LocalDate.of(2026, 9, 16)),
                TestData.expense(id = 3, minorUnits = 500, occurredOn = LocalDate.of(2026, 9, 16)),
            ),
        )

        viewModel.uiState.test {
            skipItems(1)
            val success = assertIs<ExpensesListUiState.Success>(awaitItem())

            assertEquals(
                listOf(LocalDate.of(2026, 9, 16), LocalDate.of(2026, 9, 14)),
                success.days.map { it.date },
            )
            assertEquals(2, success.days.first().expenses.size)
            assertEquals(2_500L, success.days.first().dayTotal.minorUnits)
            assertEquals(3, success.transactionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a day total ignores amounts in another currency`() = runTest {
        expenseRepository.setExpenses(
            listOf(
                TestData.expense(id = 1, minorUnits = 1_000, occurredOn = TestData.today),
                TestData.expense(id = 2, minorUnits = 5_000, occurredOn = TestData.today, currencyCode = "EUR"),
            ),
        )

        viewModel.uiState.test {
            skipItems(1)
            val success = assertIs<ExpensesListUiState.Success>(awaitItem())

            // Both rows are listed, but only the active currency counts toward the total.
            assertEquals(2, success.days.single().expenses.size)
            assertEquals(1_000L, success.days.single().dayTotal.minorUnits)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing month moves the window`() = runTest {
        expenseRepository.setExpenses(
            listOf(TestData.expense(id = 1, occurredOn = LocalDate.of(2026, 8, 20))),
        )

        viewModel.uiState.test {
            skipItems(1)
            assertIs<ExpensesListUiState.Empty>(awaitItem())

            viewModel.onMonthChange(-1)

            val august = awaitItem()
            assertEquals(YearMonth.of(2026, 8), august.month)
            assertIs<ExpensesListUiState.Success>(august)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleting an expense removes it and emits an undo event`() = runTest {
        val expense = TestData.expense(id = 1, occurredOn = TestData.today)
        expenseRepository.setExpenses(listOf(expense))

        viewModel.uiEvents.test {
            viewModel.onDeleteExpense(expense)

            val event = assertIs<ExpensesListEvent.ExpenseDeleted>(awaitItem())
            assertEquals(1L, event.expense.id)
            assertEquals(emptyList(), expenseRepository.currentExpenses())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `undo restores the expense`() = runTest {
        val expense = TestData.expense(id = 1, minorUnits = 3_300, occurredOn = TestData.today)
        expenseRepository.setExpenses(listOf(expense))
        viewModel.onDeleteExpense(expense)
        advanceUntilIdle()

        viewModel.onUndoDelete(expense)
        advanceUntilIdle()

        val restored = expenseRepository.currentExpenses().single()
        assertEquals(3_300L, restored.amount.minorUnits)
        assertEquals(expense.note, restored.note)
    }
}
