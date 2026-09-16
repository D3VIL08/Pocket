package dev.pocket.core.data.repository

import dev.pocket.core.common.di.Dispatcher
import dev.pocket.core.common.di.PocketDispatchers
import dev.pocket.core.data.mapper.toDomain
import dev.pocket.core.data.mapper.toEntity
import dev.pocket.core.database.dao.CategoryDao
import dev.pocket.core.domain.repository.CategoryRepository
import dev.pocket.core.model.Category
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class OfflineFirstCategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    @param:Dispatcher(PocketDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeCategories()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun getCategory(id: Long): Category? = withContext(ioDispatcher) {
        categoryDao.getCategory(id)?.toDomain()
    }

    override suspend fun addCategory(category: Category): Long = withContext(ioDispatcher) {
        // A category created at runtime is never a protected default, whatever the caller passed.
        categoryDao.insert(category.toEntity().copy(id = Category.NO_ID, isDefault = false))
    }

    override suspend fun updateCategory(category: Category) = withContext(ioDispatcher) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun deleteCategory(id: Long, reassignExpensesTo: Long) =
        withContext(ioDispatcher) {
            require(id != reassignExpensesTo) { "Cannot reassign a category's expenses to itself" }
            checkNotNull(categoryDao.getCategory(reassignExpensesTo)) {
                "Reassignment target category $reassignExpensesTo does not exist"
            }
            val deleted = categoryDao.deleteAndReassign(id, reassignExpensesTo)
            check(deleted > 0) { "Category $id does not exist or is a default that cannot be deleted" }
        }
}
