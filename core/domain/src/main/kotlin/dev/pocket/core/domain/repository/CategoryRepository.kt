package dev.pocket.core.domain.repository

import dev.pocket.core.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun observeCategories(): Flow<List<Category>>

    suspend fun getCategory(id: Long): Category?

    suspend fun addCategory(category: Category): Long

    suspend fun updateCategory(category: Category)

    /**
     * Deletes a user-created category. Default categories cannot be deleted, and expenses that
     * referenced the deleted category are reassigned rather than dropped.
     */
    suspend fun deleteCategory(id: Long, reassignExpensesTo: Long)
}
