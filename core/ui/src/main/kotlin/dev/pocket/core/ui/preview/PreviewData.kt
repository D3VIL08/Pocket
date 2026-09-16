package dev.pocket.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.pocket.core.model.Category
import dev.pocket.core.model.CategoryIcon
import dev.pocket.core.model.Expense
import dev.pocket.core.model.Money
import java.time.Instant
import java.time.LocalDate

/**
 * Sample data for `@Preview` composables. Shared from `:core:ui` so previews across features show
 * the same plausible figures instead of each screen inventing its own.
 */
object PreviewData {

    val food = Category(1, "Food", 0xFFE8743B.toInt(), CategoryIcon.FOOD, isDefault = true)
    val transport = Category(2, "Transport", 0xFF3B8EE8.toInt(), CategoryIcon.TRANSPORT, isDefault = true)
    val bills = Category(3, "Bills", 0xFF7C5CD6.toInt(), CategoryIcon.BILLS, isDefault = true)
    val shopping = Category(4, "Shopping", 0xFFD64C8D.toInt(), CategoryIcon.SHOPPING, isDefault = true)

    val categories: List<Category> = listOf(food, transport, bills, shopping)

    private val today: LocalDate = LocalDate.of(2026, 9, 16)

    val expenses: List<Expense> = listOf(
        expense(1, 1_250, food, "Flat white and a pastry", today),
        expense(2, 3_480, transport, "Monthly travel card top-up", today),
        expense(3, 8_999, shopping, "Running shoes", today.minusDays(1)),
        expense(4, 4_200, food, "Groceries", today.minusDays(1)),
        expense(5, 65_00, bills, "Broadband", today.minusDays(3)),
    )

    val singleExpense: Expense = expenses.first()

    private fun expense(
        id: Long,
        minorUnits: Long,
        category: Category,
        note: String,
        date: LocalDate,
    ) = Expense(
        id = id,
        amount = Money(minorUnits, "USD"),
        category = category,
        note = note,
        occurredOn = date,
        createdAt = Instant.parse("2026-09-16T10:15:30Z").plusSeconds(id),
    )
}

class ExpensePreviewParameterProvider : PreviewParameterProvider<Expense> {
    override val values: Sequence<Expense> = PreviewData.expenses.asSequence()
}

class CategoryPreviewParameterProvider : PreviewParameterProvider<Category> {
    override val values: Sequence<Category> = PreviewData.categories.asSequence()
}
