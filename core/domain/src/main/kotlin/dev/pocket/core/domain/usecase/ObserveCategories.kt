package dev.pocket.core.domain.usecase

import dev.pocket.core.domain.repository.CategoryRepository
import dev.pocket.core.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCategories @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.observeCategories()
}
