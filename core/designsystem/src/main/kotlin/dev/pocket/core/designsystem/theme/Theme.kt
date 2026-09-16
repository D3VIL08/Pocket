package dev.pocket.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors: ColorScheme = lightColorScheme(
    primary = PocketColors.Primary,
    onPrimary = PocketColors.OnPrimary,
    primaryContainer = PocketColors.PrimaryContainer,
    onPrimaryContainer = PocketColors.OnPrimaryContainer,
    secondary = PocketColors.Secondary,
    onSecondary = PocketColors.OnSecondary,
    secondaryContainer = PocketColors.SecondaryContainer,
    onSecondaryContainer = PocketColors.OnSecondaryContainer,
    tertiary = PocketColors.Tertiary,
    onTertiary = PocketColors.OnTertiary,
    tertiaryContainer = PocketColors.TertiaryContainer,
    onTertiaryContainer = PocketColors.OnTertiaryContainer,
    error = PocketColors.Error,
    onError = PocketColors.OnError,
    errorContainer = PocketColors.ErrorContainer,
    onErrorContainer = PocketColors.OnErrorContainer,
    background = PocketColors.Background,
    onBackground = PocketColors.OnBackground,
    surface = PocketColors.Surface,
    onSurface = PocketColors.OnSurface,
    surfaceVariant = PocketColors.SurfaceVariant,
    onSurfaceVariant = PocketColors.OnSurfaceVariant,
    outline = PocketColors.Outline,
    outlineVariant = PocketColors.OutlineVariant,
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = PocketColors.PrimaryDark,
    onPrimary = PocketColors.OnPrimaryDark,
    primaryContainer = PocketColors.PrimaryContainerDark,
    onPrimaryContainer = PocketColors.OnPrimaryContainerDark,
    secondary = PocketColors.SecondaryDark,
    onSecondary = PocketColors.OnSecondaryDark,
    secondaryContainer = PocketColors.SecondaryContainerDark,
    onSecondaryContainer = PocketColors.OnSecondaryContainerDark,
    tertiary = PocketColors.TertiaryDark,
    onTertiary = PocketColors.OnTertiaryDark,
    tertiaryContainer = PocketColors.TertiaryContainerDark,
    onTertiaryContainer = PocketColors.OnTertiaryContainerDark,
    error = PocketColors.ErrorDark,
    onError = PocketColors.OnErrorDark,
    errorContainer = PocketColors.ErrorContainerDark,
    onErrorContainer = PocketColors.OnErrorContainerDark,
    background = PocketColors.BackgroundDark,
    onBackground = PocketColors.OnBackgroundDark,
    surface = PocketColors.SurfaceDark,
    onSurface = PocketColors.OnSurfaceDark,
    surfaceVariant = PocketColors.SurfaceVariantDark,
    onSurfaceVariant = PocketColors.OnSurfaceVariantDark,
    outline = PocketColors.OutlineDark,
    outlineVariant = PocketColors.OutlineVariantDark,
)

/**
 * @param useDynamicColor honour the wallpaper-derived palette on Android 12+. Defaults to true
 *   because matching the user's system theme is what makes the app feel native; the hand-tuned
 *   palette above is the fallback everywhere else.
 */
@Composable
fun PocketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        useDynamicColor && supportsDynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PocketTypography,
        content = content,
    )
}
