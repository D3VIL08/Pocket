package dev.pocket.core.designsystem.theme

import androidx.compose.ui.unit.dp

/** One spacing scale for the whole app, so padding is never an arbitrary number at a call site. */
object Spacing {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp

    /** Minimum touch target, per the Material accessibility guidance. */
    val minTouchTarget = 48.dp
}
