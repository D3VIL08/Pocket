package dev.pocket.core.domain.usecase

import dev.pocket.core.common.result.FailureReason
import dev.pocket.core.common.result.Outcome
import dev.pocket.core.testing.data.TestData
import dev.pocket.core.testing.repository.TestCategoryRepository
import dev.pocket.core.testing.repository.TestExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AddExpenseTest {

    private lateinit var expenseRepository: TestExpenseRepository
    private lateinit var categoryRepository: TestCategoryRepository
    private lateinit var addExpense: AddExpense

    @Before
    fun setUp() {
        expenseRepository = TestExpenseRepository()
        categoryRepository = TestCategoryRepository()
        addExpense = AddExpense(
            expenseRepository = expenseRepository,
            validate = ValidateExpenseDraft(categoryRepository, TestData.fixedClock),
            clock = TestData.fixedClock,
        )
    }

    @Test
    fun `stores a valid draft and returns its new id`() = runTest {
        val outcome = addExpense(draft(minorUnits = 2_599))

        assertIs<Outcome.Success<Long>>(outcome)
        val stored = expenseRepository.currentExpenses().single()
        assertEquals(outcome.value, stored.id)
        assertEquals(2_599L, stored.amount.minorUnits)
        assertEquals("Food", stored.category.name)
    }

    @Test
    fun `trims the note before storing`() = runTest {
        addExpense(draft(note = "   Lunch with Sam   "))

        assertEquals("Lunch with Sam", expenseRepository.currentExpenses().single().note)
    }

    @Test
    fun `rejects a zero amount`() = runTest {
        val outcome = addExpense(draft(minorUnits = 0))

        val reason = assertIs<Outcome.Failure>(outcome).reason
        val validation = assertIs<FailureReason.Validation>(reason)
        assertEquals(FailureReason.Validation.Field.AMOUNT, validation.field)
        assertTrue(expenseRepository.currentExpenses().isEmpty())
    }

    @Test
    fun `rejects a negative amount`() = runTest {
        val outcome = addExpense(draft(minorUnits = -500))

        val validation = assertIs<FailureReason.Validation>(assertIs<Outcome.Failure>(outcome).reason)
        assertEquals(FailureReason.Validation.Field.AMOUNT, validation.field)
    }

    @Test
    fun `rejects a date in the future`() = runTest {
        val outcome = addExpense(draft(occurredOn = TestData.today.plusDays(1)))

        val validation = assertIs<FailureReason.Validation>(assertIs<Outcome.Failure>(outcome).reason)
        assertEquals(FailureReason.Validation.Field.DATE, validation.field)
    }

    @Test
    fun `accepts today as a date`() = runTest {
        val outcome = addExpense(draft(occurredOn = TestData.today))

        assertIs<Outcome.Success<Long>>(outcome)
    }

    @Test
    fun `rejects a category that does not exist`() = runTest {
        val outcome = addExpense(draft(categoryId = 999L))

        val validation = assertIs<FailureReason.Validation>(assertIs<Outcome.Failure>(outcome).reason)
        assertEquals(FailureReason.Validation.Field.CATEGORY, validation.field)
    }

    @Test
    fun `rejects a note longer than the limit`() = runTest {
        val outcome = addExpense(draft(note = "x".repeat(201)))

        val validation = assertIs<FailureReason.Validation>(assertIs<Outcome.Failure>(outcome).reason)
        assertEquals(FailureReason.Validation.Field.NOTE, validation.field)
    }

    @Test
    fun `reports a storage failure as Unexpected rather than throwing`() = runTest {
        expenseRepository.failNextWrite = IOException("disk full")

        val outcome = addExpense(draft())

        assertIs<FailureReason.Unexpected>(assertIs<Outcome.Failure>(outcome).reason)
    }

    private fun draft(
        minorUnits: Long = 1_250L,
        categoryId: Long = 1L,
        note: String = "Coffee",
        occurredOn: java.time.LocalDate = TestData.today,
    ) = ExpenseDraft(
        amount = TestData.money(minorUnits),
        categoryId = categoryId,
        note = note,
        occurredOn = occurredOn,
    )
}
