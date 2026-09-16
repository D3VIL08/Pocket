package dev.pocket.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.pocket.core.database.entity.CategoryEntity
import dev.pocket.core.database.entity.ExpenseEntity

/**
 * Builds an in-memory [PocketDatabase] for a test.
 *
 * In-memory Room is still real SQLite running the real generated queries, so these tests catch
 * the things a fake repository cannot: a wrong column name, a broken ORDER BY, a foreign key that
 * does not do what the annotation claims.
 */
internal fun createTestDatabase(): PocketDatabase =
    Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        PocketDatabase::class.java,
    )
        .allowMainThreadQueries()
        .build()

internal fun categoryEntity(
    id: Long = 0L,
    name: String = "Food",
    colorArgb: Int = 0xFFE8743B.toInt(),
    icon: String = "FOOD",
    isDefault: Boolean = false,
) = CategoryEntity(id = id, name = name, colorArgb = colorArgb, icon = icon, isDefault = isDefault)

internal fun expenseEntity(
    id: Long = 0L,
    amountMinor: Long = 1_000L,
    currencyCode: String = "USD",
    categoryId: Long = 1L,
    note: String = "",
    occurredOn: String = "2026-09-16",
    createdAt: Long = 1_757_000_000_000L,
    receiptUri: String? = null,
) = ExpenseEntity(
    id = id,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    categoryId = categoryId,
    note = note,
    occurredOn = occurredOn,
    createdAt = createdAt,
    receiptUri = receiptUri,
)
