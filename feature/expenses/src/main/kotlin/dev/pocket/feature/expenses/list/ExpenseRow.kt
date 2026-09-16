package dev.pocket.feature.expenses.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.pocket.core.designsystem.theme.PocketTextStyles
import dev.pocket.core.designsystem.theme.PocketTheme
import dev.pocket.core.designsystem.theme.Spacing
import dev.pocket.core.model.Expense
import dev.pocket.core.ui.component.CategoryAvatar
import dev.pocket.core.ui.format.MoneyFormatter
import dev.pocket.core.ui.preview.PreviewData

@Composable
internal fun ExpenseRow(
    expense: Expense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val amountText = MoneyFormatter.format(expense.amount)
    val noteText = expense.note.ifBlank { expense.category.name }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = Spacing.minTouchTarget)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            // Read as one sentence rather than three disconnected fragments.
            .clearAndSetSemantics {
                contentDescription = "$amountText, ${expense.category.name}, $noteText"
            },
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryAvatar(category = expense.category)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = noteText,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = expense.category.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(text = amountText, style = PocketTextStyles.AmountRow)
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseRowPreview() {
    PocketTheme(useDynamicColor = false) {
        ExpenseRow(expense = PreviewData.singleExpense, onClick = {})
    }
}
