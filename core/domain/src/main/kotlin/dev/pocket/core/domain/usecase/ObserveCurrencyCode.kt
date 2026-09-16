package dev.pocket.core.domain.usecase

import dev.pocket.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrencyCode @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = userPreferencesRepository.observeCurrencyCode()
}
