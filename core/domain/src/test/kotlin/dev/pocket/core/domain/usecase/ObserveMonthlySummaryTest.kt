package dev.pocket.core.domain.usecase

import app.cash.turbine.test
import dev.pocket.core.testing.data.TestData
import dev.pocket.core.testing.repository.TestExpenseRepository
import dev.pocket.core.testing.repository.TestUserPreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

class ObserveMonthlySummaryTest {

    private lateinit var expenseRepository: TestExpenseRepository
    private lateinit var observeMonthlySummary: ObserveMonthlySummary

    private val september = YearMonth.of(2026, 9)
    private val food = TestData.category(id = 1L, name = "Food")
    private val transport = TestData.category(id = 2L, name = "Transport")

    @Before
    fun setUp() {
        expenseRepository = TestExpenseRepository()
        observeMonthlySummary = ObserveMonthlySummary(
            expenseRepository = expenseRepository,
            userPreferencesRepository = TestUserPreferencesRepository(),
        )
    }

    @Test
    fun `totals only the expenses inside the month`() = runTest {
        expenseRepository.setExpenses(
            listOf(
                TestData.expense(id = 1, minorUnits = 1_000, occurredOn = LocalDate.of(2026, 9, 1)),
                TestData.expense(id = 2, minorUnits = 2_000, occurredOn = LocalDate.of(2026, 9, 30)),
                // Just outside the range on either side.
                TestData.expense(id = 3, minorUnits = 9_999, occurredOn = LocalDate.of(2026, 8, 31)),
                TestData.expense(id = 4, minorUnits = 9_999, occurredOn = LocalDate.of(2026, 10, 1)),
            ),
        )

        observeMonthlySummary(september).test {
            assertEquals(3_000L, awaitItem().total.minorUnits)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `groups by category, largest first`() = runTest {
        expenseRepository.setExpenses(
            listOf(
                TestData.expense(id = 1, minorUnits = 1_000, category = food, occurredOn = TestData.today),
                TestData.expense(id = 2, minorUnits = 500, category = food, occurredOn = TestData.today),
                TestData.expense(id = 3, minorUnits = 4_000, category = transport, occurredOn = TestData.today),
            ),
        )

        observeMonthlySummary(september).test {
            val summary = awaitItem()
            assertEquals(listOf("Transport", "Food"), summary.byCategory.map { it.category.name })
            assertEquals(4_000L, summary.byCategory.first().total.minorUnits)
            assertEquals(2, summary.byCategory.last().transactionCount)
            assertEquals(3, summary.transactionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits a zero total for a month with no spending`() = runTest {
        observeMonthlySummary(september).test {
            val summary = awaitItem()
            assertEquals(0L, summary.total.minorUnits)
            assertEquals(emptyList(), summary.byCategory)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `re-emits when an expense is added`() = runTest {
        observeMonthlySummary(september).test {
            assertEquals(0L, awaitItem().total.minorUnits)

            expenseRepository.addExpense(
                TestData.expense(id = 0, minorUnits = 2_500, occurredOn = TestData.today),
            )

            assertEquals(2_500L, awaitItem().total.minorUnits)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
