package dev.pocket.core.data.repository

import dev.pocket.core.data.preferences.PocketPreferencesDataSource
import dev.pocket.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class DataStoreUserPreferencesRepository @Inject constructor(
    private val preferences: PocketPreferencesDataSource,
) : UserPreferencesRepository {

    override fun observeCurrencyCode(): Flow<String> = preferences.currencyCode

    override suspend fun setCurrencyCode(currencyCode: String) =
        preferences.setCurrencyCode(currencyCode)

    override fun observeDefaultCategoryId(): Flow<Long?> = preferences.defaultCategoryId

    override suspend fun setDefaultCategoryId(categoryId: Long?) =
        preferences.setDefaultCategoryId(categoryId)
}
