package dev.pocket.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.pocket.core.model.Money
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PocketPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val currencyCode: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.CURRENCY_CODE] ?: defaultCurrencyCode()
    }

    val defaultCategoryId: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_CATEGORY_ID]?.takeIf { it > 0L }
    }

    suspend fun setCurrencyCode(currencyCode: String) {
        dataStore.edit { it[Keys.CURRENCY_CODE] = currencyCode }
    }

    suspend fun setDefaultCategoryId(categoryId: Long?) {
        dataStore.edit { prefs ->
            if (categoryId == null) {
                prefs.remove(Keys.DEFAULT_CATEGORY_ID)
            } else {
                prefs[Keys.DEFAULT_CATEGORY_ID] = categoryId
            }
        }
    }

    /**
     * Falls back to the device locale's currency so a first-run user in India sees ₹ and one in
     * Germany sees €, without anybody having to visit settings first.
     */
    private fun defaultCurrencyCode(): String = runCatching {
        java.util.Currency.getInstance(java.util.Locale.getDefault()).currencyCode
    }.getOrDefault(Money.FALLBACK_CURRENCY_CODE)

    private object Keys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val DEFAULT_CATEGORY_ID = longPreferencesKey("default_category_id")
    }
}
