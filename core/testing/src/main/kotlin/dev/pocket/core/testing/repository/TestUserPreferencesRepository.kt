package dev.pocket.core.testing.repository

import dev.pocket.core.domain.repository.UserPreferencesRepository
import dev.pocket.core.testing.data.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestUserPreferencesRepository : UserPreferencesRepository {

    private val currency = MutableStateFlow(TestData.CURRENCY)
    private val defaultCategory = MutableStateFlow<Long?>(null)

    override fun observeCurrencyCode(): Flow<String> = currency

    override suspend fun setCurrencyCode(currencyCode: String) {
        currency.value = currencyCode
    }

    override fun observeDefaultCategoryId(): Flow<Long?> = defaultCategory

    override suspend fun setDefaultCategoryId(categoryId: Long?) {
        defaultCategory.value = categoryId
    }
}
