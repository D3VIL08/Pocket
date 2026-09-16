package dev.pocket.feature.expenses.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pocket.core.designsystem.component.PocketButton
import dev.pocket.core.designsystem.component.PocketTopAppBar
import dev.pocket.core.designsystem.theme.PocketTextStyles
import dev.pocket.core.designsystem.theme.PocketTheme
import dev.pocket.core.designsystem.theme.Spacing
import dev.pocket.core.model.Category
import dev.pocket.core.ui.component.CategoryChip
import dev.pocket.core.ui.format.DateFormatter
import dev.pocket.core.ui.format.MoneyFormatter
import dev.pocket.core.ui.preview.PreviewData
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun EditExpenseRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditExpenseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { onNavigateBack() }
    }

    EditExpenseScreen(
        uiState = uiState,
        onAmountChange = viewModel::onAmountChange,
        onCategorySelected = viewModel::onCategorySelected,
        onNoteChange = viewModel::onNoteChange,
        onDateChange = viewModel::onDateChange,
        onSave = viewModel::onSave,
        onDelete = viewModel::onDelete,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditExpenseScreen(
    uiState: EditExpenseUiState,
    onAmountChange: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val amountFocusRequester = remember { FocusRequester() }
    var showDatePicker by remember { mutableStateOf(false) }

    // Quick entry means the keyboard is already up on the amount when the screen opens; a new
    // expense should be two taps, not four.
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && !uiState.isEditing) {
            amountFocusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            PocketTopAppBar(
                title = if (uiState.isEditing) "Edit expense" else "New expense",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = "Back",
                onNavigationClick = onNavigateBack,
                actions = {
                    if (uiState.isEditing) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete expense")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            uiState.generalError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            AmountField(
                value = uiState.amountInput,
                currencyCode = uiState.currencyCode,
                errorMessage = uiState.amountError,
                onValueChange = onAmountChange,
                focusRequester = amountFocusRequester,
            )

            CategorySelector(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                errorMessage = uiState.categoryError,
                onCategorySelected = onCategorySelected,
            )

            DateField(
                date = uiState.date,
                errorMessage = uiState.dateError,
                onClick = { showDatePicker = true },
            )

            OutlinedTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(NoteFieldTestTag),
                label = { Text("Note (optional)") },
                isError = uiState.noteError != null,
                supportingText = uiState.noteError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )

            PocketButton(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().testTag(SaveButtonTestTag),
                enabled = uiState.canSave,
                loading = uiState.isSaving,
            ) {
                Text(if (uiState.isEditing) "Save changes" else "Add expense")
            }
        }

        if (showDatePicker) {
            ExpenseDatePickerDialog(
                initialDate = uiState.date,
                onDismiss = { showDatePicker = false },
                onDateSelected = {
                    onDateChange(it)
                    showDatePicker = false
                },
            )
        }
    }
}

@Composable
private fun AmountField(
    value: String,
    currencyCode: String,
    errorMessage: String?,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag(AmountFieldTestTag),
        label = { Text("Amount") },
        prefix = { Text(MoneyFormatter.symbolOf(currencyCode)) },
        textStyle = PocketTextStyles.AmountInput,
        isError = errorMessage != null,
        supportingText = errorMessage?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
        ),
    )
}

@Composable
private fun CategorySelector(
    categories: List<Category>,
    selectedCategoryId: Long?,
    errorMessage: String?,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = "Category", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            items(categories, key = { it.id }) { category ->
                CategoryChip(
                    category = category,
                    selected = category.id == selectedCategoryId,
                    onClick = { onCategorySelected(category.id) },
                )
            }
        }
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DateField(
    date: LocalDate,
    errorMessage: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = "Date", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onClick, modifier = Modifier.testTag(DateFieldTestTag)) {
                Text(DateFormatter.fullDate(date))
            }
        }
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        // The picker works in UTC-midnight millis; converting through UTC keeps
                        // the date the user tapped from sliding a day in either direction.
                        onDateSelected(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                        )
                    }
                },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state)
    }
}

internal const val AmountFieldTestTag = "amountField"
internal const val NoteFieldTestTag = "noteField"
internal const val SaveButtonTestTag = "saveButton"
internal const val DateFieldTestTag = "dateField"

@Preview(showBackground = true)
@Composable
private fun EditExpenseScreenPreview() {
    PocketTheme(useDynamicColor = false) {
        EditExpenseScreen(
            uiState = EditExpenseUiState(
                isLoading = false,
                amountInput = "12.50",
                categories = PreviewData.categories.toImmutableList(),
                selectedCategoryId = PreviewData.food.id,
                note = "Flat white",
            ),
            onAmountChange = {},
            onCategorySelected = {},
            onNoteChange = {},
            onDateChange = {},
            onSave = {},
            onDelete = {},
            onNavigateBack = {},
        )
    }
}
