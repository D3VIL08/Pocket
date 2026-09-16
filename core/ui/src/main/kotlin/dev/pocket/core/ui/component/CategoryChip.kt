package dev.pocket.core.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import dev.pocket.core.model.Category

/**
 * A selectable category. Colour alone never carries the selection state — the chip's own selected
 * styling and a state description do, so it works for colour-blind users and screen readers.
 */
@Composable
fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(category.name) },
        modifier = modifier.semantics {
            stateDescription = if (selected) "Selected" else "Not selected"
        },
        leadingIcon = {
            Icon(
                imageVector = category.icon.asImageVector(),
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize),
                tint = Color(category.colorArgb),
            )
        },
    )
}
