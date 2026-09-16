package dev.pocket.core.model

/**
 * A spending category. Categories are user-editable rows rather than an enum so that people can
 * add their own without an app update, which also means the seeded defaults are just ordinary
 * rows that happen to be inserted on first launch.
 */
data class Category(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val icon: CategoryIcon,
    val isDefault: Boolean = false,
) {
    companion object {
        const val NO_ID: Long = 0L
    }
}

/**
 * A stable, storable name for a category's glyph. The database persists this enum's [name], so
 * entries may be added but must never be renamed or removed without a migration.
 */
enum class CategoryIcon {
    FOOD,
    TRANSPORT,
    BILLS,
    SHOPPING,
    ENTERTAINMENT,
    HEALTH,
    HOME,
    EDUCATION,
    TRAVEL,
    SAVINGS,
    OTHER,
    ;

    companion object {
        fun fromNameOrOther(value: String): CategoryIcon =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}
