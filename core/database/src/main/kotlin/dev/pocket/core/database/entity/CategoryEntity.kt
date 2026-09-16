package dev.pocket.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color_argb")
    val colorArgb: Int,

    /** Stored as the [dev.pocket.core.model.CategoryIcon] enum name. */
    @ColumnInfo(name = "icon")
    val icon: String,

    /** True for the categories seeded on first launch; these cannot be deleted. */
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,
)
