package dev.pocket.feature.expenses.edit

import dev.pocket.core.common.result.FailureReason
import dev.pocket.core.model.Category
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

/**
 * The entry form's state.
 *
 * [amountInput] stays a [String] rather than a parsed number: while the user is typing, `"1."` and
 * `"1.0"` are different things to show but the same number, and re-rendering a parsed value would
 * fight the keyboard.
 */
data class EditExpenseUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val amountInput: String = "",
    val currencyCode: String = "USD",
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val categories: ImmutableList<Category> = persistentListOf(),
    val error: FailureReason? = null,
) {
    /** Enabled only when there is something to save; validation proper happens in the domain. */
    val canSave: Boolean
        get() = !isSaving && amountInput.isNotBlank() && selectedCategoryId != null

    val amountError: String?
        get() = validationMessageFor(FailureReason.Validation.Field.AMOUNT)

    val dateError: String?
        get() = validationMessageFor(FailureReason.Validation.Field.DATE)

    val noteError: String?
        get() = validationMessageFor(FailureReason.Validation.Field.NOTE)

    val categoryError: String?
        get() = validationMessageFor(FailureReason.Validation.Field.CATEGORY)

    /** A failure that is not tied to one field, shown as a banner rather than under an input. */
    val generalError: String?
        get() = when (error) {
            is FailureReason.NotFound -> "That expense no longer exists."
            is FailureReason.Unexpected -> "Something went wrong. Please try again."
            else -> null
        }

    private fun validationMessageFor(field: FailureReason.Validation.Field): String? =
        (error as? FailureReason.Validation)?.takeIf { it.field == field }?.message
}

sealed interface EditExpenseEvent {
    data object Saved : EditExpenseEvent
    data object Deleted : EditExpenseEvent
}
