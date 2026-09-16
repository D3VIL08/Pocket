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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var database: PocketDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        categoryDao = database.categoryDao()
        expenseDao = database.expenseDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `defaults sort before user categories, then alphabetically`() = runTest {
        categoryDao.insert(categoryEntity(name = "Zebra", isDefault = true))
        categoryDao.insert(categoryEntity(name = "Apple", isDefault = true))
        categoryDao.insert(categoryEntity(name = "Boats", isDefault = false))

        val names = categoryDao.observeCategories().first().map { it.name }

        assertEquals(listOf("Apple", "Zebra", "Boats"), names)
    }

    @Test
    fun `category names are unique`() = runTest {
        categoryDao.insert(categoryEntity(name = "Food"))

        assertFailsWith<android.database.sqlite.SQLiteConstraintException> {
            categoryDao.insert(categoryEntity(name = "Food"))
        }
    }

    @Test
    fun `deleting a category in use is blocked by the foreign key`() = runTest {
        val id = categoryDao.insert(categoryEntity(name = "Food"))
        expenseDao.insert(expenseEntity(categoryId = id))

        // RESTRICT means SQLite refuses rather than orphaning or cascading away the expense.
        assertFailsWith<android.database.sqlite.SQLiteConstraintException> {
            categoryDao.deleteUserCategory(id)
        }
    }

    @Test
    fun `deleteAndReassign moves expenses then removes the category`() = runTest {
        val food = categoryDao.insert(categoryEntity(name = "Food"))
        val other = categoryDao.insert(categoryEntity(name = "Other"))
        val expenseId = expenseDao.insert(expenseEntity(categoryId = food))

        val deleted = categoryDao.deleteAndReassign(id = food, reassignExpensesTo = other)

        assertEquals(1, deleted)
        assertEquals(1, categoryDao.count())
        val moved = expenseDao.getExpense(expenseId)
        assertNotNull(moved)
        assertEquals("Other", moved.category.name)
    }

    @Test
    fun `a default category cannot be deleted`() = runTest {
        val protectedId = categoryDao.insert(categoryEntity(name = "Food", isDefault = true))
        val other = categoryDao.insert(categoryEntity(name = "Other"))

        val deleted = categoryDao.deleteAndReassign(id = protectedId, reassignExpensesTo = other)

        assertEquals(0, deleted, "deleteUserCategory filters on is_default = 0")
        assertEquals(2, categoryDao.count())
    }

    @Test
    fun `insertAll ignores names that already exist`() = runTest {
        categoryDao.insert(categoryEntity(name = "Food"))

        categoryDao.insertAll(listOf(categoryEntity(name = "Food"), categoryEntity(name = "Bills")))

        assertEquals(setOf("Bills", "Food"), categoryDao.observeCategories().first().map { it.name }.toSet())
    }
}
