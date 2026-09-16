package dev.pocket.core.domain.repository

import kotlinx.coroutines.flow.Flow

/** User-chosen settings that outlive a single screen. Backed by DataStore in `:core:data`. */
interface UserPreferencesRepository {

    /** ISO 4217 code every amount in the app is entered and displayed in. */
    fun observeCurrencyCode(): Flow<String>

    suspend fun setCurrencyCode(currencyCode: String)

    /** The category preselected on the add-expense screen, or `null` for none. */
    fun observeDefaultCategoryId(): Flow<Long?>

    suspend fun setDefaultCategoryId(categoryId: Long?)
}
