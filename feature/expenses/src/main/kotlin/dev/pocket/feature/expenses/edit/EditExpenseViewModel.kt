package dev.pocket.feature.expenses.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pocket.core.common.result.FailureReason
import dev.pocket.core.common.result.Outcome
import dev.pocket.core.domain.usecase.ExpenseDraft
import dev.pocket.core.domain.usecase.ExpenseEditingUseCases
import dev.pocket.core.domain.usecase.ObserveCategories
import dev.pocket.core.domain.usecase.ObserveCurrencyCode
import dev.pocket.core.model.Expense
import dev.pocket.core.model.Money
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseEditing: ExpenseEditingUseCases,
    private val observeCategories: ObserveCategories,
    private val observeCurrencyCode: ObserveCurrencyCode,
    private val clock: Clock,
) : ViewModel() {

    private val expenseId: Long = savedStateHandle.get<Long>(EXPENSE_ID_KEY) ?: Expense.NO_ID

    private val _uiState = MutableStateFlow(
        EditExpenseUiState(isEditing = expenseId != Expense.NO_ID, date = LocalDate.now(clock)),
    )
    val uiState: StateFlow<EditExpenseUiState> = _uiState.asStateFlow()

    private val events = Channel<EditExpenseEvent>(Channel.BUFFERED)
    val uiEvents: Flow<EditExpenseEvent> = events.receiveAsFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val categories = observeCategories().first().toImmutableList()
        val currencyCode = observeCurrencyCode().first()
        val existing = if (expenseId != Expense.NO_ID) expenseEditing.getDraft(expenseId) else null

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                categories = categories,
                currencyCode = existing?.amount?.currencyCode ?: currencyCode,
                amountInput = existing?.let { formatForEditing(it.amount) } ?: "",
                // Preselecting the first category means the common case is one tap on amount and
                // one on save; the user only touches the chips when the guess is wrong.
                selectedCategoryId = existing?.categoryId ?: categories.firstOrNull()?.id,
                note = existing?.note.orEmpty(),
                date = existing?.occurredOn ?: LocalDate.now(clock),
                error = if (expenseId != Expense.NO_ID && existing == null) {
                    FailureReason.NotFound("Expense $expenseId")
                } else {
                    null
                },
            )
        }
    }

    fun onAmountChange(input: String) {
        // Accept only digits and a single separator; rejecting the keystroke is less surprising
        // than accepting it and showing an error after the fact.
        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
        if (filtered.count { it == '.' || it == ',' } > 1) return
        _uiState.update { it.copy(amountInput = filtered, error = clearFieldError(it)) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, error = clearFieldError(it)) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note, error = clearFieldError(it)) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(date = date, error = clearFieldError(it)) }
    }

    fun onSave() {
        val state = _uiState.value
        val categoryId = state.selectedCategoryId ?: return
        val amount = Money.parseOrNull(state.amountInput, state.currencyCode)
            ?: run {
                _uiState.update {
                    it.copy(
                        error = FailureReason.Validation(
                            FailureReason.Validation.Field.AMOUNT,
                            "Enter a valid amount",
                        ),
                    )
                }
                return
            }

        val draft = ExpenseDraft(
            id = if (state.isEditing) expenseId else Expense.NO_ID,
            amount = amount,
            categoryId = categoryId,
            note = state.note,
            occurredOn = state.date,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val outcome = if (state.isEditing) {
                expenseEditing.update(draft)
            } else {
                expenseEditing.add(draft)
            }
            when (outcome) {
                is Outcome.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    events.send(EditExpenseEvent.Saved)
                }
                is Outcome.Failure -> _uiState.update {
                    it.copy(isSaving = false, error = outcome.reason)
                }
            }
        }
    }

    fun onDelete() {
        if (!_uiState.value.isEditing) return
        viewModelScope.launch {
            expenseEditing.delete(expenseId)
            events.send(EditExpenseEvent.Deleted)
        }
    }

    /** Clears an inline error as soon as the user edits anything, so it never lingers stale. */
    private fun clearFieldError(state: EditExpenseUiState): FailureReason? =
        state.error?.takeUnless { it is FailureReason.Validation }

    private fun formatForEditing(money: Money): String = money.toBigDecimal().toPlainString()

    companion object {
        const val EXPENSE_ID_KEY: String = "expenseId"
    }
}
