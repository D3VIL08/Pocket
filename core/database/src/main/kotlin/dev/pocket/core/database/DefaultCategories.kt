package dev.pocket.core.database

import dev.pocket.core.database.entity.CategoryEntity
import dev.pocket.core.model.CategoryIcon

/**
 * The categories every new install starts with. They are ordinary rows flagged `is_default`, not a
 * hardcoded enum, so the user can rename and recolour them — only deletion is blocked, because
 * expenses point at them.
 *
 * Colours are chosen to stay distinguishable against both light and dark surfaces and to remain
 * separable for the most common forms of colour vision deficiency.
 */
internal object DefaultCategories {

    val entities: List<CategoryEntity> = listOf(
        category("Food", 0xFFE8743B.toInt(), CategoryIcon.FOOD),
        category("Transport", 0xFF3B8EE8.toInt(), CategoryIcon.TRANSPORT),
        category("Bills", 0xFF7C5CD6.toInt(), CategoryIcon.BILLS),
        category("Shopping", 0xFFD64C8D.toInt(), CategoryIcon.SHOPPING),
        category("Entertainment", 0xFF23A398.toInt(), CategoryIcon.ENTERTAINMENT),
        category("Health", 0xFF4CA64C.toInt(), CategoryIcon.HEALTH),
        category("Other", 0xFF6B7280.toInt(), CategoryIcon.OTHER),
    )

    /** The category new expenses fall back to, and where orphaned expenses are reassigned. */
    const val FALLBACK_NAME: String = "Other"

    private fun category(name: String, colorArgb: Int, icon: CategoryIcon) = CategoryEntity(
        name = name,
        colorArgb = colorArgb,
        icon = icon.name,
        isDefault = true,
    )
}
