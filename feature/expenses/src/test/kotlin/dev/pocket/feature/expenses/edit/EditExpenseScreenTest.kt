package dev.pocket.feature.expenses.edit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.pocket.core.designsystem.theme.PocketTheme
import dev.pocket.core.testing.data.TestData
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EditExpenseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val categories = listOf(
        TestData.category(id = 1L, name = "Food"),
        TestData.category(id = 2L, name = "Transport"),
    ).toImmutableList()

    @Test
    fun `the save button is disabled until an amount is present`() {
        setContent(state(amountInput = ""))

        composeRule.onNodeWithTag(SaveButtonTestTag).assertIsNotEnabled()
    }

    @Test
    fun `the save button enables once the form is complete`() {
        setContent(state(amountInput = "12.50"))

        composeRule.onNodeWithTag(SaveButtonTestTag).assertIsEnabled()
    }

    @Test
    fun `typing in the amount field reports each change`() {
        var typed = ""
        setContent(state(), onAmountChange = { typed = it })

        composeRule.onNodeWithTag(AmountFieldTestTag).performTextInput("9")

        assertEquals("9", typed)
    }

    @Test
    fun `selecting a category reports its id`() {
        var selected: Long? = null
        setContent(state(), onCategorySelected = { selected = it })

        composeRule.onNodeWithText("Transport").performClick()

        assertEquals(2L, selected)
    }

    @Test
    fun `a validation error is shown under the amount field`() {
        setContent(
            state(
                amountInput = "0",
                error = dev.pocket.core.common.result.FailureReason.Validation(
                    dev.pocket.core.common.result.FailureReason.Validation.Field.AMOUNT,
                    "Enter an amount greater than zero",
                ),
            ),
        )

        composeRule.onNodeWithText("Enter an amount greater than zero").assertIsDisplayed()
    }

    @Test
    fun `a new expense has no delete action and an Add label`() {
        setContent(state(isEditing = false))

        composeRule.onNodeWithContentDescription("Delete expense").assertDoesNotExist()
        // The form scrolls, so the button sits below the fold on a short viewport; scrolling to
        // it also asserts that it is actually reachable.
        composeRule.onNodeWithTag(SaveButtonTestTag)
            .performScrollTo()
            .assertTextEquals("Add expense")
    }

    @Test
    fun `editing an expense offers delete and a save-changes label`() {
        setContent(state(isEditing = true, amountInput = "12.50"))

        composeRule.onNodeWithContentDescription("Delete expense").assertIsDisplayed()
        composeRule.onNodeWithTag(SaveButtonTestTag)
            .performScrollTo()
            .assertTextEquals("Save changes")
    }

    @Test
    fun `the loading state shows no form`() {
        setContent(state(isLoading = true))

        composeRule.onNodeWithTag(AmountFieldTestTag).assertDoesNotExist()
    }

    private fun state(
        isEditing: Boolean = false,
        isLoading: Boolean = false,
        amountInput: String = "",
        error: dev.pocket.core.common.result.FailureReason? = null,
    ) = EditExpenseUiState(
        isEditing = isEditing,
        isLoading = isLoading,
        amountInput = amountInput,
        currencyCode = "USD",
        selectedCategoryId = 1L,
        categories = categories,
        date = TestData.today,
        error = error,
    )

    private fun setContent(
        uiState: EditExpenseUiState,
        onAmountChange: (String) -> Unit = {},
        onCategorySelected: (Long) -> Unit = {},
    ) = composeRule.setContent {
        PocketTheme(useDynamicColor = false) {
            EditExpenseScreen(
                uiState = uiState,
                onAmountChange = onAmountChange,
                onCategorySelected = onCategorySelected,
                onNoteChange = {},
                onDateChange = {},
                onSave = {},
                onDelete = {},
                onNavigateBack = {},
            )
        }
    }
}
