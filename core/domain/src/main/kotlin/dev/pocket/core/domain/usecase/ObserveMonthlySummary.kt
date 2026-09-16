package dev.pocket.core.domain.usecase

import dev.pocket.core.domain.repository.ExpenseRepository
import dev.pocket.core.domain.repository.UserPreferencesRepository
import dev.pocket.core.model.CategorySpend
import dev.pocket.core.model.DateRange
import dev.pocket.core.model.Money
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.time.YearMonth
import javax.inject.Inject

/** Everything the month header — and later the dashboard — needs about one calendar month. */
data class MonthlySummary(
    val month: YearMonth,
    val total: Money,
    val byCategory: List<CategorySpend>,
) {
    val transactionCount: Int get() = byCategory.sumOf { it.transactionCount }
}

/**
 * Totals one month's spending. Built on the repository's aggregate queries rather than loading
 * every expense and summing in memory, so it stays cheap as history grows — and it is already the
 * shape the dashboard feature will need.
 */
class ObserveMonthlySummary @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(month: YearMonth): Flow<MonthlySummary> =
        userPreferencesRepository.observeCurrencyCode().flatMapLatest { currencyCode ->
            val range = DateRange.of(month)
            combine(
                expenseRepository.observeTotal(range, currencyCode),
                expenseRepository.observeCategoryTotals(range, currencyCode),
            ) { total, byCategory ->
                MonthlySummary(month = month, total = total, byCategory = byCategory)
            }
        }
}
