package dev.pocket.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The seed runs from `RoomDatabase.Callback.onCreate`, which only fires when the database file is
 * actually created — so this test builds one the same way the Hilt module does.
 */
@RunWith(AndroidJUnit4::class)
class DefaultCategoriesSeedTest {

    private fun seededDatabase(): PocketDatabase =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PocketDatabase::class.java,
        )
            .allowMainThreadQueries()
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        DefaultCategories.entities.forEach { category ->
                            db.execSQL(
                                "INSERT INTO categories (name, color_argb, icon, is_default) " +
                                    "VALUES (?, ?, ?, ?)",
                                arrayOf<Any>(
                                    category.name,
                                    category.colorArgb,
                                    category.icon,
                                    if (category.isDefault) 1 else 0,
                                ),
                            )
                        }
                    }
                },
            )
            .build()

    @Test
    fun `a new database starts with the default categories`() = runTest {
        val database = seededDatabase()
        try {
            val categories = database.categoryDao().observeCategories().first()

            assertEquals(DefaultCategories.entities.size, categories.size)
            assertTrue(categories.all { it.isDefault })
            assertTrue(categories.any { it.name == DefaultCategories.FALLBACK_NAME })
        } finally {
            database.close()
        }
    }

    @Test
    fun `every seeded icon maps to a known CategoryIcon`() {
        val known = dev.pocket.core.model.CategoryIcon.entries.map { it.name }.toSet()

        assertTrue(DefaultCategories.entities.all { it.icon in known })
    }

    @Test
    fun `seeded category names are unique`() {
        val names = DefaultCategories.entities.map { it.name }

        assertEquals(names.size, names.toSet().size)
    }
}
