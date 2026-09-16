package dev.pocket.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.pocket.core.database.DefaultCategories
import dev.pocket.core.database.PocketDatabase
import dev.pocket.core.database.dao.CategoryDao
import dev.pocket.core.database.dao.ExpenseDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providePocketDatabase(@ApplicationContext context: Context): PocketDatabase =
        Room.databaseBuilder(context, PocketDatabase::class.java, PocketDatabase.NAME)
            .addCallback(SeedCategoriesCallback)
            // No fallbackToDestructiveMigration: spending history is not disposable, so a
            // missing migration must fail loudly in development rather than wipe the user's data.
            .build()

    @Provides
    fun provideExpenseDao(database: PocketDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideCategoryDao(database: PocketDatabase): CategoryDao = database.categoryDao()
}

/**
 * Seeds the default categories the first time the database file is created. Running this in
 * `onCreate` (rather than on first app launch) means the rows exist before any query can run, so
 * no screen ever has to handle a category-less state.
 */
private object SeedCategoriesCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        DefaultCategories.entities.forEach { category ->
            db.execSQL(
                "INSERT INTO categories (name, color_argb, icon, is_default) VALUES (?, ?, ?, ?)",
                arrayOf<Any>(
                    category.name,
                    category.colorArgb,
                    category.icon,
                    if (category.isDefault) 1 else 0,
                ),
            )
        }
    }
}
