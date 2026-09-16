package dev.pocket.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.pocket.core.database.entity.CategoryTotal
import dev.pocket.core.database.entity.ExpenseEntity
import dev.pocket.core.database.entity.PopulatedExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    /**
     * One query serving every list view. The filter arguments are nullable so a single prepared
     * statement covers "all expenses", "this month" and "this category" without string-building
     * SQL at runtime.
     *
     * `sortOrder` is an integer rather than interpolated SQL because Room cannot bind an ORDER BY
     * clause as a parameter, and interpolating one would be an injection vector.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM expenses
        WHERE (:startDate IS NULL OR occurred_on >= :startDate)
          AND (:endDate IS NULL OR occurred_on <= :endDate)
          AND (:filterByCategory = 0 OR category_id IN (:categoryIds))
          AND (:query IS NULL OR note LIKE '%' || :query || '%')
        ORDER BY
            CASE WHEN :sortOrder = 0 THEN occurred_on END DESC,
            CASE WHEN :sortOrder = 0 THEN created_at END DESC,
            CASE WHEN :sortOrder = 1 THEN occurred_on END ASC,
            CASE WHEN :sortOrder = 1 THEN created_at END ASC,
            CASE WHEN :sortOrder = 2 THEN amount_minor END DESC,
            CASE WHEN :sortOrder = 3 THEN amount_minor END ASC,
            id DESC
        """,
    )
    fun observeExpenses(
        startDate: String?,
        endDate: String?,
        filterByCategory: Int,
        categoryIds: List<Long>,
        query: String?,
        sortOrder: Int,
    ): Flow<List<PopulatedExpense>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id")
    fun observeExpense(id: Long): Flow<PopulatedExpense?>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpense(id: Long): PopulatedExpense?

    /**
     * Month total as one aggregate. Returns null when no rows match — SUM over an empty set is
     * NULL in SQLite — which the repository turns into a zero [dev.pocket.core.model.Money].
     */
    @Query(
        """
        SELECT SUM(amount_minor) FROM expenses
        WHERE occurred_on >= :startDate AND occurred_on <= :endDate
          AND currency_code = :currencyCode
        """,
    )
    fun observeTotalMinor(startDate: String, endDate: String, currencyCode: String): Flow<Long?>

    @Query(
        """
        SELECT c.*, SUM(e.amount_minor) AS total_minor, COUNT(e.id) AS transaction_count
        FROM expenses e
        INNER JOIN categories c ON c.id = e.category_id
        WHERE e.occurred_on >= :startDate AND e.occurred_on <= :endDate
          AND e.currency_code = :currencyCode
        GROUP BY c.id
        ORDER BY total_minor DESC
        """,
    )
    fun observeCategoryTotals(
        startDate: String,
        endDate: String,
        currencyCode: String,
    ): Flow<List<CategoryTotal>>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun count(): Int
}
