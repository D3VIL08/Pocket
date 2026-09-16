package dev.pocket.core.domain.usecase

import dev.pocket.core.common.result.FailureReason
import dev.pocket.core.common.result.Outcome
import dev.pocket.core.testing.data.TestData
import dev.pocket.core.testing.repository.TestCategoryRepository
import dev.pocket.core.testing.repository.TestExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdateExpenseTest {

    private lateinit var expenseRepository: TestExpenseRepository
    private lateinit var updateExpense: UpdateExpense

    private val originalCreatedAt = Instant.parse("2026-09-01T08:00:00Z")

    @Before
    fun setUp() {
        expenseRepository = TestExpenseRepository()
        expenseRepository.setExpenses(
            listOf(
                TestData.expense(id = 7L, minorUnits = 1_000, note = "Old note", createdAt = originalCreatedAt),
            ),
        )
        updateExpense = UpdateExpense(
            expenseRepository = expenseRepository,
            validate = ValidateExpenseDraft(TestCategoryRepository(), TestData.fixedClock),
        )
    }

    @Test
    fun `writes the new values over the existing expense`() = runTest {
        val outcome = updateExpense(
            ExpenseDraft(
                id = 7L,
                amount = TestData.money(4_500),
                categoryId = 2L,
                note = "New note",
                occurredOn = TestData.today,
            ),
        )

        assertIs<Outcome.Success<Unit>>(outcome)
        val updated = expenseRepository.currentExpenses().single()
        assertEquals(4_500L, updated.amount.minorUnits)
        assertEquals("New note", updated.note)
        assertEquals("Transport", updated.category.name)
    }

    @Test
    fun `preserves createdAt so editing does not reorder same-day entries`() = runTest {
        updateExpense(
            ExpenseDraft(
                id = 7L,
                amount = TestData.money(4_500),
                categoryId = 1L,
                note = "Edited",
                occurredOn = TestData.today,
            ),
        )

        assertEquals(originalCreatedAt, expenseRepository.currentExpenses().single().createdAt)
    }

    @Test
    fun `fails with NotFound when the expense is gone`() = runTest {
        val outcome = updateExpense(
            ExpenseDraft(
                id = 999L,
                amount = TestData.money(100),
                categoryId = 1L,
                occurredOn = TestData.today,
            ),
        )

        assertIs<FailureReason.NotFound>(assertIs<Outcome.Failure>(outcome).reason)
    }

    @Test
    fun `validates before touching storage`() = runTest {
        val outcome = updateExpense(
            ExpenseDraft(
                id = 7L,
                amount = TestData.money(0),
                categoryId = 1L,
                occurredOn = TestData.today,
            ),
        )

        assertIs<FailureReason.Validation>(assertIs<Outcome.Failure>(outcome).reason)
        // The stored row is untouched.
        assertEquals(1_000L, expenseRepository.currentExpenses().single().amount.minorUnits)
    }
}
