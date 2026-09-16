package dev.pocket.core.model

/**
 * Narrows an expense query. A `null` field means "no constraint", so [ExpenseFilter.All] reads
 * everything and each screen adds only the constraints it cares about.
 */
data class ExpenseFilter(
    val dateRange: DateRange? = null,
    val categoryIds: Set<Long>? = null,
    val query: String? = null,
) {
    companion object {
        val All: ExpenseFilter = ExpenseFilter()
    }
}

/** How a list of expenses is ordered. */
enum class ExpenseSort {
    /** Most recent spending day first — the default for the list screen. */
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC,
}
