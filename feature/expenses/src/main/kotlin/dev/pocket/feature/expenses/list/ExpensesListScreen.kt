package dev.pocket.feature.expenses.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pocket.core.designsystem.component.EmptyState
import dev.pocket.core.designsystem.component.PocketButton
import dev.pocket.core.designsystem.theme.PocketTheme
import dev.pocket.core.designsystem.theme.Spacing
import dev.pocket.core.model.Expense
import dev.pocket.core.model.Money
import dev.pocket.core.ui.format.DateFormatter
import dev.pocket.core.ui.format.MoneyFormatter
import dev.pocket.core.ui.preview.PreviewData
import kotlinx.collections.immutable.toImmutableList
import java.time.YearMonth

/**
 * Stateful entry point. Everything below this is a pure function of its arguments, which is what
 * lets previews and Robolectric tests drive the screen without a ViewModel or a database.
 */
@Composable
fun ExpensesListRoute(
    onAddExpense: () -> Unit,
    onExpenseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is ExpensesListEvent.ExpenseDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Deleted ${MoneyFormatter.format(event.expense.amount)}",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onUndoDelete(event.expense)
                    }
                }
            }
        }
    }

    ExpensesListScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAddExpense = onAddExpense,
        onExpenseClick = onExpenseClick,
        onPreviousMonth = { viewModel.onMonthChange(-1) },
        onNextMonth = { viewModel.onMonthChange(1) },
        onDeleteExpense = viewModel::onDeleteExpense,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpensesListScreen(
    uiState: ExpensesListUiState,
    snackbarHostState: SnackbarHostState,
    onAddExpense: () -> Unit,
    onExpenseClick: (Long) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val currentMonth = remember { YearMonth.now() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {
                is ExpensesListUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                is ExpensesListUiState.Empty -> EmptyMonth(
                    uiState = uiState,
                    currentMonth = currentMonth,
                    onAddExpense = onAddExpense,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                )

                is ExpensesListUiState.Success -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xxl * 2),
                ) {
                    item(key = "month-header") {
                        MonthHeader(
                            month = uiState.month,
                            total = uiState.total,
                            onPreviousMonth = onPreviousMonth,
                            onNextMonth = onNextMonth,
                            canGoForward = uiState.month < currentMonth,
                        )
                        HorizontalDivider()
                    }

                    uiState.days.forEach { day ->
                        item(key = "day-${day.date}") {
                            DayHeader(day)
                        }
                        items(
                            items = day.expenses,
                            // Stable keys keep scroll position and item animations correct when
                            // a row is added or deleted above the viewport.
                            key = { expense -> expense.id },
                        ) { expense ->
                            SwipeToDeleteRow(onDelete = { onDeleteExpense(expense) }) {
                                ExpenseRow(
                                    expense = expense,
                                    onClick = { onExpenseClick(expense.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(day: ExpenseDay, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = DateFormatter.dayHeader(day.date),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = MoneyFormatter.format(day.dayTotal),
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyMonth(
    uiState: ExpensesListUiState.Empty,
    currentMonth: YearMonth,
    onAddExpense: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MonthHeader(
            month = uiState.month,
            total = uiState.total,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            canGoForward = uiState.month < currentMonth,
        )
        HorizontalDivider()
        EmptyState(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            title = "Nothing recorded yet",
            description = "Add your first expense for this month and it will show up here.",
            action = {
                PocketButton(onClick = onAddExpense) { Text("Add expense") }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesListScreenEmptyPreview() {
    PocketTheme(useDynamicColor = false) {
        ExpensesListScreen(
            uiState = ExpensesListUiState.Empty(
                month = YearMonth.of(2026, 9),
                total = Money.zero("USD"),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAddExpense = {},
            onExpenseClick = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onDeleteExpense = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesListScreenSuccessPreview() {
    val expenses = PreviewData.expenses
    PocketTheme(useDynamicColor = false) {
        ExpensesListScreen(
            uiState = ExpensesListUiState.Success(
                month = YearMonth.of(2026, 9),
                total = Money(123_456, "USD"),
                days = expenses.groupBy { it.occurredOn }
                    .map { (date, items) ->
                        ExpenseDay(
                            date = date,
                            dayTotal = Money(items.sumOf { it.amount.minorUnits }, "USD"),
                            expenses = items.toImmutableList(),
                        )
                    }
                    .toImmutableList(),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAddExpense = {},
            onExpenseClick = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onDeleteExpense = {},
        )
    }
}
