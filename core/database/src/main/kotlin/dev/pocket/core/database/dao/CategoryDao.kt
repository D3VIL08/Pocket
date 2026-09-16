package dev.pocket.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.pocket.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * An abstract class rather than an interface so [deleteAndReassign] can carry a real
 * `@Transaction` body. That keeps Room — and the fact that this is two statements — entirely
 * inside this module instead of leaking a transaction API into `:core:data`.
 */
@Dao
abstract class CategoryDao {

    @Query("SELECT * FROM categories ORDER BY is_default DESC, name ASC")
    abstract fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    abstract suspend fun getCategory(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    abstract suspend fun update(category: CategoryEntity)

    /** Returns the number of rows removed; default categories are protected by the WHERE clause. */
    @Query("DELETE FROM categories WHERE id = :id AND is_default = 0")
    abstract suspend fun deleteUserCategory(id: Long): Int

    @Query("UPDATE expenses SET category_id = :newCategoryId WHERE category_id = :oldCategoryId")
    abstract suspend fun reassignExpenses(oldCategoryId: Long, newCategoryId: Long)

    @Query("SELECT COUNT(*) FROM categories")
    abstract suspend fun count(): Int

    /**
     * Moves every expense off [id] and then deletes it, atomically.
     *
     * Order matters and so does atomicity: the expense -> category foreign key is RESTRICT, so
     * deleting first would simply fail, and reassigning without a transaction would leave the
     * user's history pointing at the wrong category if the delete then failed.
     *
     * @return the number of categories deleted — zero means [id] was a protected default.
     */
    @Transaction
    open suspend fun deleteAndReassign(id: Long, reassignExpensesTo: Long): Int {
        reassignExpenses(oldCategoryId = id, newCategoryId = reassignExpensesTo)
        return deleteUserCategory(id)
    }
}
