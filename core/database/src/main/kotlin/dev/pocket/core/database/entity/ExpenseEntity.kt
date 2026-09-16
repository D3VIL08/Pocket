package dev.pocket.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            // Deleting a category reassigns its expenses in a transaction rather than cascading;
            // losing spending history because a label was removed would be a data-loss bug.
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        // The list and every month aggregate filter on this column, so it carries an index.
        Index(value = ["occurred_on"]),
        Index(value = ["category_id"]),
    ],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    /** Amount in minor units. See `dev.pocket.core.model.Money` for why this is not a REAL. */
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,

    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "note")
    val note: String,

    /** ISO-8601 local date (`2026-09-16`). Text sorts and compares correctly in this format. */
    @ColumnInfo(name = "occurred_on")
    val occurredOn: String,

    /** Epoch milliseconds, used to order entries recorded on the same day. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "receipt_uri")
    val receiptUri: String? = null,
)
