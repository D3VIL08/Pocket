package dev.pocket.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/** Projection returned by the per-category aggregate query. */
data class CategoryTotal(
    @Embedded val category: CategoryEntity,
    @ColumnInfo(name = "total_minor") val totalMinor: Long,
    @ColumnInfo(name = "transaction_count") val transactionCount: Int,
)
