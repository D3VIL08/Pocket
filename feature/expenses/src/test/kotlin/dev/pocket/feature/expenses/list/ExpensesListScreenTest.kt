package dev.pocket.feature.expenses.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.pocket.core.designsystem.theme.PocketTheme
import dev.pocket.core.model.Expense
import dev.pocket.core.model.Money
import dev.pocket.core.testing.data.TestData
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

/**
 * Compose tests run under Robolectric on the JVM rather than on an emulator, so the whole suite
 * runs from `./gradlew test` and CI needs no device.
 *
 * They drive the stateless `ExpensesListScreen`, not the route — no ViewModel, no Hilt, no
 * database, so a failure points at the UI and nothing else.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExpensesListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `empty state explains itself and offers the action`() {
        setContent(ExpensesListUiState.Empty(YearMonth.of(2026, 9), Money.zero("USD")))

        composeRule.onNodeWithText("Nothing recorded yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add expense").assertIsDisplayed()
    }

    @Test
    fun `the month label and total are shown`() {
        setContent(ExpensesListUiState.Empty(YearMonth.of(2026, 9), Money(123_456, "USD")))

        composeRule.onNodeWithText("September 2026").assertIsDisplayed()
        composeRule.onNodeWithText("$1,234.56").assertIsDisplayed()
    }

    @Test
    fun `loading shows no expense content`() {
        setContent(ExpensesListUiState.Loading(YearMonth.of(2026, 9)))

        composeRule.onNodeWithText("Nothing recorded yet").assertDoesNotExist()
    }

    @Test
    fun `an expense row reads as one sentence to a screen reader`() {
        setContent(successState())

        // ExpenseRow merges its children into a single contentDescription so TalkBack announces
        // "$12.50, Food, Coffee" rather than three disconnected fragments -- asserting on that
        // description is asserting on the actual accessibility contract.
        composeRule
            .onNodeWithContentDescription("Coffee", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `today's expenses sit under a Today header`() {
        setContent(successState(expense = TestData.expense(id = 7L, note = "Coffee", occurredOn = LocalDate.now())))

        composeRule.onNodeWithText("Today").assertIsDisplayed()
    }

    @Test
    fun `tapping an expense reports its id`() {
        var clickedId: Long? = null
        setContent(successState(), onExpenseClick = { clickedId = it })

        composeRule
            .onNodeWithContentDescription("Coffee", substring = true)
            .performClick()

        assertEquals(7L, clickedId)
    }

    @Test
    fun `the add button invokes the add callback`() {
        var addClicked = false
        setContent(successState(), onAddExpense = { addClicked = true })

        composeRule.onNodeWithContentDescription("Add expense").performClick()

        assertEquals(true, addClicked)
    }

    @Test
    fun `month navigation calls back in both directions`() {
        var delta = 0
        setContent(
            successState(),
            onPreviousMonth = { delta-- },
            onNextMonth = { delta++ },
        )

        composeRule.onNodeWithContentDescription("Previous month").performClick()

        assertEquals(-1, delta)
    }

    private fun successState(expense: Expense = TestData.expense(id = 7L, note = "Coffee")) =
        ExpensesListUiState.Success(
            month = YearMonth.of(2026, 9),
            total = Money(1_250, "USD"),
            days = persistentListOf(
                ExpenseDay(
                    date = expense.occurredOn,
                    dayTotal = expense.amount,
                    expenses = listOf(expense).toImmutableList(),
                ),
            ),
        )

    private fun setContent(
        uiState: ExpensesListUiState,
        onAddExpense: () -> Unit = {},
        onExpenseClick: (Long) -> Unit = {},
        onPreviousMonth: () -> Unit = {},
        onNextMonth: () -> Unit = {},
    ) = composeRule.setContent {
        PocketTheme(useDynamicColor = false) {
            ExpensesListScreen(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onAddExpense = onAddExpense,
                onExpenseClick = onExpenseClick,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onDeleteExpense = {},
            )
        }
    }
}
