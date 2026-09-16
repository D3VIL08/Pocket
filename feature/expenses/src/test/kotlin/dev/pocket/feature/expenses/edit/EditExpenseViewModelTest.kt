package dev.pocket.feature.expenses.edit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.pocket.core.common.result.FailureReason
import dev.pocket.core.domain.usecase.AddExpense
import dev.pocket.core.domain.usecase.DeleteExpense
import dev.pocket.core.domain.usecase.ExpenseEditingUseCases
import dev.pocket.core.domain.usecase.GetExpenseDraft
import dev.pocket.core.domain.usecase.ObserveCategories
import dev.pocket.core.domain.usecase.ObserveCurrencyCode
import dev.pocket.core.domain.usecase.UpdateExpense
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EditExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var expenseRepository: TestExpenseRepository
    private lateinit var categoryRepository: TestCategoryRepository

    @Before
    fun setUp() {
        expenseRepository = TestExpenseRepository()
        categoryRepository = TestCategoryRepository()
    }

    @Test
    fun `a new expense preselects the first category so entry is quick`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEditing)
        assertEquals(1L, state.selectedCategoryId)
        assertEquals("", state.amountInput)
        assertEquals(TestData.today, state.date)
    }

    @Test
    fun `cannot save until an amount is entered`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canSave)

        viewModel.onAmountChange("12.50")

        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun `amount input rejects letters and a second separator`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onAmountChange("12a.5b0")
        assertEquals("12.50", viewModel.uiState.value.amountInput)

        // A second separator is dropped rather than accepted and rejected later.
        viewModel.onAmountChange("12.50.7")
        assertEquals("12.50", viewModel.uiState.value.amountInput)
    }

    @Test
    fun `saving a valid entry stores it and emits Saved`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onAmountChange("12.50")
        viewModel.onNoteChange("Flat white")

        viewModel.uiEvents.test {
            viewModel.onSave()

            assertIs<EditExpenseEvent.Saved>(awaitItem())
            val stored = expenseRepository.currentExpenses().single()
            assertEquals(1_250L, stored.amount.minorUnits)
            assertEquals("Flat white", stored.note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an unparseable amount surfaces as a field error, not a crash`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onAmountChange(".")

        viewModel.onSave()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.amountError)
        assertTrue(expenseRepository.currentExpenses().isEmpty())
    }

    @Test
    fun `a future date is reported against the date field`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onAmountChange("5.00")
        viewModel.onDateChange(TestData.today.plusDays(3))

        viewModel.onSave()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.dateError)
        assertNull(viewModel.uiState.value.amountError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `editing an expense loads its current values`() = runTest {
        expenseRepository.setExpenses(
            listOf(TestData.expense(id = 42L, minorUnits = 4_275, note = "Taxi")),
        )

        val viewModel = viewModel(expenseId = 42L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals("42.75", state.amountInput)
        assertEquals("Taxi", state.note)
        assertEquals(1L, state.selectedCategoryId)
    }

    @Test
    fun `saving an edit updates in place rather than adding a row`() = runTest {
        expenseRepository.setExpenses(listOf(TestData.expense(id = 42L, minorUnits = 1_000)))
        val viewModel = viewModel(expenseId = 42L)
        advanceUntilIdle()
        viewModel.onAmountChange("99.99")

        viewModel.onSave()
        advanceUntilIdle()

        val stored = expenseRepository.currentExpenses().single()
        assertEquals(42L, stored.id)
        assertEquals(9_999L, stored.amount.minorUnits)
    }

    @Test
    fun `deleting from the edit screen removes the expense`() = runTest {
        expenseRepository.setExpenses(listOf(TestData.expense(id = 42L)))
        val viewModel = viewModel(expenseId = 42L)
        advanceUntilIdle()

        viewModel.uiEvents.test {
            viewModel.onDelete()

            assertIs<EditExpenseEvent.Deleted>(awaitItem())
            assertTrue(expenseRepository.currentExpenses().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `opening an expense that no longer exists shows a general error`() = runTest {
        val viewModel = viewModel(expenseId = 999L)
        advanceUntilIdle()

        assertIs<FailureReason.NotFound>(viewModel.uiState.value.error)
        assertNotNull(viewModel.uiState.value.generalError)
    }

    private fun viewModel(expenseId: Long? = null) = EditExpenseViewModel(
        savedStateHandle = SavedStateHandle(
            expenseId?.let { mapOf(EditExpenseViewModel.EXPENSE_ID_KEY to it) } ?: emptyMap(),
        ),
        expenseEditing = ExpenseEditingUseCases(
            getDraft = GetExpenseDraft(expenseRepository),
            add = AddExpense(
                expenseRepository,
                ValidateExpenseDraft(categoryRepository, TestData.fixedClock),
                TestData.fixedClock,
            ),
            update = UpdateExpense(
                expenseRepository,
                ValidateExpenseDraft(categoryRepository, TestData.fixedClock),
            ),
            delete = DeleteExpense(expenseRepository),
        ),
        observeCategories = ObserveCategories(categoryRepository),
        observeCurrencyCode = ObserveCurrencyCode(TestUserPreferencesRepository()),
        clock = TestData.fixedClock,
    )
}
