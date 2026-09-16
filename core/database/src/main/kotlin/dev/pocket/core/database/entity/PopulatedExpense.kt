package dev.pocket.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

/** An expense together with its category, so the list needs exactly one query. */
data class PopulatedExpense(
    @Embedded val expense: ExpenseEntity,
    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: CategoryEntity,
)
