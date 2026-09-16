package dev.pocket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.pocket.core.model.Category

/** The round category badge on an expense row. */
@Composable
fun CategoryAvatar(
    category: Category,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val background = Color(category.colorArgb)
    Box(
        modifier = modifier
            .size(size)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = category.icon.asImageVector(),
            // The row already names the category in text; repeating it here would make a screen
            // reader announce it twice per item.
            contentDescription = null,
            modifier = Modifier.size(size * IconRatio),
            // Category colours are user-chosen, so the glyph picks black or white per swatch
            // rather than assuming a light background.
            tint = if (background.luminance() > LuminanceMidpoint) Color.Black else Color.White,
        )
    }
}

private const val IconRatio = 0.55f
private const val LuminanceMidpoint = 0.5f
