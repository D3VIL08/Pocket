package dev.pocket.feature.expenses.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.pocket.core.designsystem.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Wraps a row in swipe-to-delete.
 *
 * The dismissed row is *not* removed from the list here — the delete goes through the ViewModel
 * and the row disappears when the database Flow re-emits. Removing it locally as well would mean
 * two sources of truth for what is on screen, and an undo would have to reconcile them.
 *
 * The trigger is [SwipeToDismissBox]'s own `onDismiss` callback, which the library fires off
 * `settledValue` — i.e. only once the swipe gesture has actually finished. Resetting after the
 * delete trigger is a fallback: without it, a slow or failed delete would leave the row
 * non-interactive forever, since [SwipeToDismissBox] disables its own drag gesture once
 * `settledValue` leaves `Settled`.
 *
 * [content] (an [ExpenseRow]) paints no background of its own — it relies on whatever sits behind
 * it. `SwipeToDismissBox` stacks it directly on top of `backgroundContent`, so without an opaque
 * layer here the red/bin background was visible *at rest*, through every row, on every screen —
 * not only mid-swipe. Confirmed live on-device: the row looked "stuck" without ever being swiped.
 */
@Composable
internal fun SwipeToDeleteRow(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        onDismiss = {
            onDelete()
            scope.launch { state.reset() }
        },
        backgroundContent = {
            val alignment = when (state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = Spacing.lg),
                contentAlignment = alignment,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                content()
            }
        },
    )
}

/** Remembers a dismiss state keyed to the row, so recycled rows do not inherit a swipe. */
@Composable
internal fun rememberRowKey(id: Long): Long = remember(id) { id }
