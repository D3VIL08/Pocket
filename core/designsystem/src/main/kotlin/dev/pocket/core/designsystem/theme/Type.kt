package dev.pocket.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Material 3 typography with the platform font. Amounts get their own styles: they are the thing
 * the user actually scans, and tabular figures keep columns of numbers from jittering as digits
 * change.
 */
internal val PocketTypography = Typography()

object PocketTextStyles {

    /** The month total at the top of the list — the largest number on screen. */
    val AmountDisplay = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        textAlign = TextAlign.Center,
        fontFeatureSettings = "tnum",
    )

    /** The amount on a single expense row. */
    val AmountRow = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontFeatureSettings = "tnum",
    )

    /** The amount being typed on the entry screen. */
    val AmountInput = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        fontFeatureSettings = "tnum",
    )
}
