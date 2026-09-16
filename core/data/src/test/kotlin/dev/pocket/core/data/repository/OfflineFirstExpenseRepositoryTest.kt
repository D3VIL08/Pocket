package dev.pocket.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dev.pocket.core.database.PocketDatabase
import dev.pocket.core.database.entity.CategoryEntity
import dev.pocket.core.model.Category
import dev.pocket.core.model.CategoryIcon
import dev.pocket.core.model.DateRange
import dev.pocket.core.model.Expense
import dev.pocket.core.model.ExpenseFilter
import dev.pocket.core.model.ExpenseSort
import dev.pocket.core.model.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Exercises the repository against a real database rather than a mocked DAO, so the mapping layer
 * and the SQL are verified together — a wrong column mapping is invisible if the DAO is faked.
 */
@RunWith(AndroidJUnit4::class)
class OfflineFirstExpenseRepositoryTest {

    private lateinit var database: PocketDatabase
    private lateinit var repository: OfflineFirstExpenseRepository
    private lateinit var food: Category

    @Before
    fun setUp() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PocketDatabase::class.java,
        ).allowMainThreadQueries().build()

        val id = database.categoryDao().insert(
            CategoryEntity(name = "Food", colorArgb = 0xFFE8743B.toInt(), icon = "FOOD", isDefault = true),
        )
        food = Category(id = id, name = "Food", colorArgb = 0xFFE8743B.toInt(), icon = CategoryIcon.FOOD, isDefault = true)

        repository = OfflineFirstExpenseRepository(
            expenseDao = database.expenseDao(),
            ioDispatcher = Dispatchers.Unconfined,
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `an added expense round-trips through every field`() = runTest {
        val id = repository.addExpense(expense(minorUnits = 2_599, note = "Lunch", receiptUri = "content://r/1"))

        val loaded = repository.getExpense(id)

        assertEquals(2_599L, loaded?.amount?.minorUnits)
        assertEquals("USD", loaded?.amount?.currencyCode)
        assertEquals("Lunch", loaded?.note)
        assertEquals(LocalDate.of(2026, 9, 16), loaded?.occurredOn)
        assertEquals("content://r/1", loaded?.receiptUri)
        assertEquals(food.id, loaded?.category?.id)
        assertEquals(CategoryIcon.FOOD, loaded?.category?.icon)
    }

    @Test
    fun `addExpense ignores a caller-supplied id`() = runTest {
        val id = repository.addExpense(expense(id = 999L))

        assertEquals(1, repository.observeExpenses().first().size)
        assertNull(repository.getExpense(999L))
        assertEquals(id, repository.observeExpenses().first().single().id)
    }

    @Test
    fun `the expense list flow re-emits after a write`() = runTest {
        repository.observeExpenses(ExpenseFilter.All, ExpenseSort.DATE_DESC).test {
            assertEquals(emptyList(), awaitItem())

            repository.addExpense(expense(note = "First"))

            assertEquals(listOf("First"), awaitItem().map { it.note })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an empty month totals to zero rather than null`() = runTest {
        val total = repository.observeTotal(DateRange.of(YearMonth.of(2026, 9)), "USD").first()

        assertEquals(Money.zero("USD"), total)
    }

    @Test
    fun `the month total sums matching rows`() = runTest {
        repository.addExpense(expense(minorUnits = 1_000, occurredOn = LocalDate.of(2026, 9, 1)))
        repository.addExpense(expense(minorUnits = 2_500, occurredOn = LocalDate.of(2026, 9, 30)))
        repository.addExpense(expense(minorUnits = 9_999, occurredOn = LocalDate.of(2026, 10, 1)))

        val total = repository.observeTotal(DateRange.of(YearMonth.of(2026, 9)), "USD").first()

        assertEquals(Money(3_500, "USD"), total)
    }

    @Test
    fun `category totals carry the requested currency`() = runTest {
        repository.addExpense(expense(minorUnits = 1_000))
        repository.addExpense(expense(minorUnits = 500))

        val totals = repository.observeCategoryTotals(DateRange.of(YearMonth.of(2026, 9)), "USD").first()

        assertEquals(1, totals.size)
        assertEquals(Money(1_500, "USD"), totals.single().total)
        assertEquals(2, totals.single().transactionCount)
        assertEquals("Food", totals.single().category.name)
    }

    @Test
    fun `update rewrites the stored row`() = runTest {
        val id = repository.addExpense(expense(minorUnits = 1_000, note = "Before"))
        val stored = repository.getExpense(id)!!

        repository.updateExpense(stored.copy(amount = Money(7_777, "USD"), note = "After"))

        val updated = repository.getExpense(id)
        assertEquals(7_777L, updated?.amount?.minorUnits)
        assertEquals("After", updated?.note)
    }

    @Test
    fun `delete removes the expense`() = runTest {
        val id = repository.addExpense(expense())

        repository.deleteExpense(id)

        assertNull(repository.getExpense(id))
        assertEquals(emptyList(), repository.observeExpenses().first())
    }

    @Test
    fun `filtering by category returns only that category`() = runTest {
        val otherId = database.categoryDao().insert(
            CategoryEntity(name = "Bills", colorArgb = 0xFF7C5CD6.toInt(), icon = "BILLS"),
        )
        repository.addExpense(expense(note = "food"))
        repository.addExpense(
            expense(note = "bills").copy(category = food.copy(id = otherId, name = "Bills")),
        )

        val filtered = repository
            .observeExpenses(ExpenseFilter(categoryIds = setOf(otherId)))
            .first()

        assertEquals(listOf("bills"), filtered.map { it.note })
    }

    private fun expense(
        id: Long = Expense.NO_ID,
        minorUnits: Long = 1_000L,
        note: String = "Coffee",
        occurredOn: LocalDate = LocalDate.of(2026, 9, 16),
        receiptUri: String? = null,
    ) = Expense(
        id = id,
        amount = Money(minorUnits, "USD"),
        category = food,
        note = note,
        occurredOn = occurredOn,
        createdAt = Instant.parse("2026-09-16T10:00:00Z"),
        receiptUri = receiptUri,
    )
}
