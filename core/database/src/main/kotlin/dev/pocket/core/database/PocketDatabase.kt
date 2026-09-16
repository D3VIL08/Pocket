package dev.pocket.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.pocket.core.database.dao.CategoryDao
import dev.pocket.core.database.dao.ExpenseDao
import dev.pocket.core.database.entity.CategoryEntity
import dev.pocket.core.database.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class, CategoryEntity::class],
    version = PocketDatabase.VERSION,
    exportSchema = true,
)
abstract class PocketDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        const val VERSION: Int = 1
        const val NAME: String = "pocket.db"
    }
}
