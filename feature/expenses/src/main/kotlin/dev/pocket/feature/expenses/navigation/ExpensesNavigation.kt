package dev.pocket.feature.expenses.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.pocket.core.model.Expense
import dev.pocket.feature.expenses.edit.EditExpenseRoute
import dev.pocket.feature.expenses.list.ExpensesListRoute
import kotlinx.serialization.Serializable

/**
 * Type-safe destinations. Routes are `@Serializable` classes rather than interpolated strings, so
 * a wrong or missing argument is a compile error instead of a crash at navigation time.
 *
 * `EditExpenseDestination.expenseId` is read straight out of `SavedStateHandle` by
 * [dev.pocket.feature.expenses.edit.EditExpenseViewModel] under the key `expenseId`, which
 * Navigation derives from the property name — so renaming this property means renaming that key.
 */
@Serializable
data object ExpensesListDestination

@Serializable
data class EditExpenseDestination(val expenseId: Long = Expense.NO_ID)

fun NavController.navigateToExpensesList(navOptions: NavOptions? = null) =
    navigate(route = ExpensesListDestination, navOptions = navOptions)

fun NavController.navigateToAddExpense(navOptions: NavOptions? = null) =
    navigate(route = EditExpenseDestination(), navOptions = navOptions)

fun NavController.navigateToEditExpense(expenseId: Long, navOptions: NavOptions? = null) =
    navigate(route = EditExpenseDestination(expenseId), navOptions = navOptions)

fun NavGraphBuilder.expensesListScreen(
    onAddExpense: () -> Unit,
    onExpenseClick: (Long) -> Unit,
) {
    composable<ExpensesListDestination> {
        ExpensesListRoute(onAddExpense = onAddExpense, onExpenseClick = onExpenseClick)
    }
}

fun NavGraphBuilder.editExpenseScreen(onNavigateBack: () -> Unit) {
    composable<EditExpenseDestination> {
        EditExpenseRoute(onNavigateBack = onNavigateBack)
    }
}
