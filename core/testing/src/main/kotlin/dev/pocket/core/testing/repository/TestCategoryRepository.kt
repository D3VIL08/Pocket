package dev.pocket.core.testing.repository

import dev.pocket.core.domain.repository.CategoryRepository
import dev.pocket.core.model.Category
import dev.pocket.core.testing.data.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TestCategoryRepository : CategoryRepository {

    private val categories = MutableStateFlow(
        listOf(
            TestData.category(id = 1L, name = "Food"),
            TestData.category(id = 2L, name = "Transport"),
        ),
    )
    private var nextId = 3L

    fun setCategories(values: List<Category>) {
        categories.value = values
        nextId = (values.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun observeCategories(): Flow<List<Category>> = categories

    override suspend fun getCategory(id: Long): Category? =
        categories.value.firstOrNull { it.id == id }

    override suspend fun addCategory(category: Category): Long {
        val id = nextId++
        categories.update { it + category.copy(id = id) }
        return id
    }

    override suspend fun updateCategory(category: Category) {
        categories.update { all -> all.map { if (it.id == category.id) category else it } }
    }

    override suspend fun deleteCategory(id: Long, reassignExpensesTo: Long) {
        categories.update { all -> all.filterNot { it.id == id } }
    }
}
