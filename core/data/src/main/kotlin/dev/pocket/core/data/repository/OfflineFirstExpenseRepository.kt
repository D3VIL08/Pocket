package dev.pocket.core.data.repository

import dev.pocket.core.common.di.Dispatcher
import dev.pocket.core.common.di.PocketDispatchers
import dev.pocket.core.data.mapper.toDomain
import dev.pocket.core.data.mapper.toEntity
import dev.pocket.core.database.dao.ExpenseDao
import dev.pocket.core.domain.repository.ExpenseRepository
import dev.pocket.core.model.CategorySpend
import dev.pocket.core.model.DateRange
import dev.pocket.core.model.Expense
import dev.pocket.core.model.ExpenseFilter
import dev.pocket.core.model.ExpenseSort
import dev.pocket.core.model.Money
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The only [ExpenseRepository] in the app today. Room is the source of truth: reads are Flows
 * straight off the DAO, so any write anywhere refreshes every observer with no invalidation code
 * of our own. When sync arrives in V2 it becomes another writer into this same database rather
 * than a second data path.
 */
internal class OfflineFirstExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    @param:Dispatcher(PocketDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ExpenseRepository {

    override fun observeExpenses(filter: ExpenseFilter, sort: ExpenseSort): Flow<List<Expense>> {
        val categoryIds = filter.categoryIds?.toList().orEmpty()
        return expenseDao.observeExpenses(
            startDate = filter.dateRange?.start?.toString(),
            endDate = filter.dateRange?.endInclusive?.toString(),
            // Room cannot express "an empty IN list means no filter", so a flag carries that.
            filterByCategory = if (categoryIds.isEmpty()) 0 else 1,
            categoryIds = categoryIds,
            query = filter.query?.takeIf { it.isNotBlank() },
            sortOrder = sort.ordinal,
        ).map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun observeExpense(id: Long): Flow<Expense?> =
        expenseDao.observeExpense(id)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)

    // SUM over no rows is NULL in SQLite, which is a zero total rather than an error.
    override fun observeTotal(range: DateRange, currencyCode: String): Flow<Money> =
        expenseDao.observeTotalMinor(
            startDate = range.start.toString(),
            endDate = range.endInclusive.toString(),
            currencyCode = currencyCode,
        ).map { totalMinor -> Money(totalMinor ?: 0L, currencyCode) }
            .flowOn(ioDispatcher)

    override fun observeCategoryTotals(
        range: DateRange,
        currencyCode: String,
    ): Flow<List<CategorySpend>> =
        expenseDao.observeCategoryTotals(
            startDate = range.start.toString(),
            endDate = range.endInclusive.toString(),
            currencyCode = currencyCode,
        ).map { totals -> totals.map { it.toDomain(currencyCode) } }
            .flowOn(ioDispatcher)

    override suspend fun addExpense(expense: Expense): Long = withContext(ioDispatcher) {
        // Insert always allocates a fresh id; a caller passing a populated draft must not be able
        // to overwrite an existing row through the add path.
        expenseDao.insert(expense.toEntity().copy(id = Expense.NO_ID))
    }

    override suspend fun updateExpense(expense: Expense) = withContext(ioDispatcher) {
        expenseDao.update(expense.toEntity())
    }

    override suspend fun deleteExpense(id: Long) = withContext(ioDispatcher) {
        expenseDao.delete(id)
    }

    override suspend fun getExpense(id: Long): Expense? = withContext(ioDispatcher) {
        expenseDao.getExpense(id)?.toDomain()
    }
}
