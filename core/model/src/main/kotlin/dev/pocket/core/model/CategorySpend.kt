package dev.pocket.core.model

/**
 * Total spend in one category over some period. Produced by an aggregate SQL query rather than by
 * summing in Kotlin, so the dashboard stays cheap as history grows.
 */
data class CategorySpend(
    val category: Category,
    val total: Money,
    val transactionCount: Int,
)
