package dev.pocket.feature.expenses.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.pocket.core.designsystem.theme.PocketTextStyles
import dev.pocket.core.designsystem.theme.PocketTheme
import dev.pocket.core.designsystem.theme.Spacing
import dev.pocket.core.model.Money
import dev.pocket.core.ui.format.DateFormatter
import dev.pocket.core.ui.format.MoneyFormatter
import java.time.YearMonth

/** Month navigation and the running total — the first thing the user reads on opening the app. */
@Composable
internal fun MonthHeader(
    month: YearMonth,
    total: Money,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
    canGoForward: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                )
            }
            Text(
                text = DateFormatter.monthLabel(month),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onNextMonth, enabled = canGoForward) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                )
            }
        }

        Text(
            text = MoneyFormatter.format(total),
            style = PocketTextStyles.AmountDisplay,
            modifier = Modifier.semantics { },
        )
        Text(
            text = "spent this month",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthHeaderPreview() {
    PocketTheme(useDynamicColor = false) {
        MonthHeader(
            month = YearMonth.of(2026, 9),
            total = Money(123_456, "USD"),
            onPreviousMonth = {},
            onNextMonth = {},
        )
    }
}
