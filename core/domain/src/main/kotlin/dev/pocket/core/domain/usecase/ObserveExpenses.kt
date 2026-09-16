package dev.pocket.core.domain.usecase

import dev.pocket.core.domain.repository.ExpenseRepository
import dev.pocket.core.model.Expense
import dev.pocket.core.model.ExpenseFilter
import dev.pocket.core.model.ExpenseSort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExpenses @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    operator fun invoke(
        filter: ExpenseFilter = ExpenseFilter.All,
        sort: ExpenseSort = ExpenseSort.DATE_DESC,
    ): Flow<List<Expense>> = expenseRepository.observeExpenses(filter, sort)
}
