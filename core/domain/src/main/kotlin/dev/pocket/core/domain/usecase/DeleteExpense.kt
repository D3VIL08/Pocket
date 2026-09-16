package dev.pocket.core.domain.usecase

import dev.pocket.core.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpense @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(id: Long) = expenseRepository.deleteExpense(id)
}
