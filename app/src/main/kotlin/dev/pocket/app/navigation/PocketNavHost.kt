package dev.pocket.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.pocket.feature.expenses.navigation.ExpensesListDestination
import dev.pocket.feature.expenses.navigation.editExpenseScreen
import dev.pocket.feature.expenses.navigation.expensesListScreen
import dev.pocket.feature.expenses.navigation.navigateToAddExpense
import dev.pocket.feature.expenses.navigation.navigateToEditExpense

/**
 * The app's single navigation graph. Features contribute their own destinations through
 * `NavGraphBuilder` extensions, so adding subscriptions or the dashboard later means one more
 * call here rather than a rewrite.
 */
@Composable
fun PocketNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = ExpensesListDestination,
        modifier = modifier,
    ) {
        expensesListScreen(
            onAddExpense = navController::navigateToAddExpense,
            onExpenseClick = navController::navigateToEditExpense,
        )
        editExpenseScreen(
            onNavigateBack = {
                // popBackStack rather than navigate: returning from the form must not stack
                // another copy of the list on the back stack.
                navController.popBackStack()
            },
        )
    }
}
