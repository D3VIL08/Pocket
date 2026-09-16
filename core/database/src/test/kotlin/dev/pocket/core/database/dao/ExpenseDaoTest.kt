package dev.pocket.core.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.pocket.core.database.PocketDatabase
import dev.pocket.core.database.categoryEntity
import dev.pocket.core.database.createTestDatabase
import dev.pocket.core.database.expenseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ExpenseDaoTest {

    private lateinit var database: PocketDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var categoryDao: CategoryDao

    private var foodId = 0L
    private var transportId = 0L

    @Before
    fun setUp() = runTest {
        database = createTestDatabase()
        expenseDao = database.expenseDao()
        categoryDao = database.categoryDao()
        foodId = categoryDao.insert(categoryEntity(name = "Food", icon = "FOOD"))
        transportId = categoryDao.insert(categoryEntity(name = "Transport", icon = "TRANSPORT"))
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `insert then read back joins the category`() = runTest {
        val id = expenseDao.insert(expenseEntity(categoryId = foodId, note = "Coffee"))

        val loaded = expenseDao.getExpense(id)

        assertEquals("Coffee", loaded?.expense?.note)
        assertEquals("Food", loaded?.category?.name)
    }

    @Test
    fun `date range filter is inclusive at both ends`() = runTest {
        expenseDao.insert(expenseEntity(categoryId = foodId, occurredOn = "2026-08-31", note = "before"))
        expenseDao.insert(expenseEntity(categoryId = foodId, occurredOn = "2026-09-01", note = "first"))
        expenseDao.insert(expenseEntity(categoryId = foodId, occurredOn = "2026-09-30", note = "last"))
        expenseDao.insert(expenseEntity(categoryId = foodId, occurredOn = "2026-10-01", note = "after"))

        val notes = expenseDao.observeExpenses(
            startDate = "2026-09-01",
            endDate = "2026-09-30",
            filterByCategory = 0,
            categoryIds = emptyList(),
            query = null,
            sortOrder = 1,
        ).first().map { it.expense.note }

        assertEquals(listOf("first", "last"), notes)
    }

    @Test
    fun `null dates mean no date filter`() = runTest {
        expenseDao.insert(expenseEntity(categoryId = foodId, occurredOn = "2020-01-01"))
        expenseDao.insert(expenseEntity(categoryId = foodId, occurredOn = "2030-01-01"))

        val all = expenseDao.observeExpenses(null, null, 0, emptyList(), null, 0).first()

        assertEquals(2, all.size)
    }

    @Test
    fun `category filter applies only when the flag is set`() = runTest {
        expenseDao.insert(expenseEntity(categoryId = foodId, note = "food"))
        expenseDao.insert(expenseEntity(categoryId = transportId, note = "transport"))

        val filtered = expenseDao
            .observeExpenses(null, null, 1, listOf(transportId), null, 0)
            .first()
            .map { it.expense.note }
        val unfiltered = expenseDao
            .observeExpenses(null, null, 0, listOf(transportId), null, 0)
            .first()

        assertEquals(listOf("transport"), filtered)
        assertEquals(2, unfiltered.size)
    }

    @Test
    fun `note search matches a substring`() = runTest {
        expenseDao.insert(expenseEntity(categoryId = foodId, note = "Flat white"))
        expenseDao.insert(expenseEntity(categoryId = foodId, note = "Groceries"))

        val found = expenseDao
            .observeExpenses(null, null, 0, emptyList(), "white", 0)
            .first()
            .map { it.expense.note }

        assertEquals(listOf("Flat white"), found)
    }

    @Test
    fun `each sort order does what its name says`() = runTest {
        expenseDao.insert(expenseEntity(categoryId = foodId, amountMinor = 300, occurredOn = "2026-09-02", note = "b"))
        expenseDao.insert(expenseEntity(categoryId = foodId, amountMinor = 100, occurredOn = "2026-09-03", note = "c"))
        expenseDao.insert(expenseEntity(categoryId = foodId, amountMinor = 200, occurredOn = "2026-09-01", note = "a"))

        suspend fun notesSortedBy(order: Int) = expenseDao
            .observeExpenses(null, null, 0, emptyList(), null, order)
            .first()
            .map { it.expense.note }

        assertEquals(listOf("c", "b", "a"), notesSortedBy(0), "DATE_DESC")
        assertEquals(listOf("a", "b", "c"), notesSortedBy(1), "DATE_ASC")
        assertEquals(listOf("b", "a", "c"), notesSortedBy(2), "AMOUNT_DESC")
        assertEquals(listOf("c", "a", "b"), notesSortedBy(3), "AMOUNT_ASC")
    }

    @Test
    fun `total is null for a month with no rows`() = runTest {
        val total = expenseDao.observeTotalMinor("2026-09-01", "2026-09-30", "USD").first()

        assertNull(total, "SUM over no rows is NULL in SQLite; the repository maps it to zero")
    }

    @Test
    fun `total counts only the requested currency`() = runTest {
        expenseDao.insert(expenseEntity(categoryId = foodId, amountMinor = 1_000, currencyCode = "USD"))
        expenseDao.insert(expenseEntity(categoryId = foodId, amountMinor = 2_000, currencyCode = "USD"))
        expenseDao.insert(expenseEntity(categoryId = foodId, amountMinor = 9_999, currencyCode = "EUR"))

        val total = expenseDao.observeTotalMinor("2026-09-01", "2026-09-30", "USD").first()

        assertEquals(3_000L, total)
    }

    @Test
    fun `category totals aggregate and order by size`() = runTest {
        expenseDao.insert(expenseEntity(categoryId = foodId, amountMinor = 1_000))
        expenseDao.insert(expenseEntity(categoryId = foodId, amountMinor = 500))
        expenseDao.insert(expenseEntity(categoryId = transportId, amountMinor = 4_000))

        val totals = expenseDao.observeCategoryTotals("2026-09-01", "2026-09-30", "USD").first()

        assertEquals(listOf("Transport", "Food"), totals.map { it.category.name })
        assertEquals(4_000L, totals.first().totalMinor)
        assertEquals(1, totals.first().transactionCount)
        assertEquals(1_500L, totals.last().totalMinor)
        assertEquals(2, totals.last().transactionCount)
    }

    @Test
    fun `delete removes the row`() = runTest {
        val id = expenseDao.insert(expenseEntity(categoryId = foodId))

        expenseDao.delete(id)

        assertEquals(0, expenseDao.count())
        assertNull(expenseDao.getExpense(id))
    }

    @Test
    fun `a flow re-emits after a write`() = runTest {
        val flow = expenseDao.observeExpenses(null, null, 0, emptyList(), null, 0)
        assertEquals(0, flow.first().size)

        expenseDao.insert(expenseEntity(categoryId = foodId))

        assertEquals(1, flow.first().size)
    }
}
