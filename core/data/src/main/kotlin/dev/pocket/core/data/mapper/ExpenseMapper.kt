package dev.pocket.core.data.mapper

import dev.pocket.core.database.entity.CategoryEntity
import dev.pocket.core.database.entity.CategoryTotal
import dev.pocket.core.database.entity.ExpenseEntity
import dev.pocket.core.database.entity.PopulatedExpense
import dev.pocket.core.model.Category
import dev.pocket.core.model.CategoryIcon
import dev.pocket.core.model.CategorySpend
import dev.pocket.core.model.Expense
import dev.pocket.core.model.Money
import java.time.Instant
import java.time.LocalDate

/**
 * Translation between storage rows and domain types. Keeping it in one file means the database
 * schema can change shape without the change leaking past this boundary.
 */

internal fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    colorArgb = colorArgb,
    icon = CategoryIcon.fromNameOrOther(icon),
    isDefault = isDefault,
)

internal fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    colorArgb = colorArgb,
    icon = icon.name,
    isDefault = isDefault,
)

internal fun PopulatedExpense.toDomain(): Expense = Expense(
    id = expense.id,
    amount = Money(expense.amountMinor, expense.currencyCode),
    category = category.toDomain(),
    note = expense.note,
    occurredOn = LocalDate.parse(expense.occurredOn),
    createdAt = Instant.ofEpochMilli(expense.createdAt),
    receiptUri = expense.receiptUri,
)

internal fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amountMinor = amount.minorUnits,
    currencyCode = amount.currencyCode,
    categoryId = category.id,
    note = note,
    occurredOn = occurredOn.toString(),
    createdAt = createdAt.toEpochMilli(),
    receiptUri = receiptUri,
)

internal fun CategoryTotal.toDomain(currencyCode: String): CategorySpend = CategorySpend(
    category = category.toDomain(),
    total = Money(totalMinor, currencyCode),
    transactionCount = transactionCount,
)
