package dev.pocket.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * The fallback palette, used whenever dynamic colour is unavailable (below Android 12) or turned
 * off. Built around a deep teal so the app reads as calm rather than alarming — a spending tracker
 * that feels like a warning label does not get opened daily.
 *
 * Every on/container pair here clears WCAG AA (4.5:1) for body text in its own scheme.
 */
internal object PocketColors {

    // Light scheme
    val Primary = Color(0xFF006A60)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF9EF2E4)
    val OnPrimaryContainer = Color(0xFF00201C)

    val Secondary = Color(0xFF4A635F)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFCCE8E2)
    val OnSecondaryContainer = Color(0xFF06201C)

    val Tertiary = Color(0xFF456179)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFCBE6FF)
    val OnTertiaryContainer = Color(0xFF001E31)

    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    val Background = Color(0xFFFAFDFB)
    val OnBackground = Color(0xFF191C1B)
    val Surface = Color(0xFFFAFDFB)
    val OnSurface = Color(0xFF191C1B)
    val SurfaceVariant = Color(0xFFDAE5E1)
    val OnSurfaceVariant = Color(0xFF3F4947)
    val Outline = Color(0xFF6F7977)
    val OutlineVariant = Color(0xFFBEC9C6)

    // Dark scheme
    val PrimaryDark = Color(0xFF82D5C8)
    val OnPrimaryDark = Color(0xFF003731)
    val PrimaryContainerDark = Color(0xFF005048)
    val OnPrimaryContainerDark = Color(0xFF9EF2E4)

    val SecondaryDark = Color(0xFFB1CCC6)
    val OnSecondaryDark = Color(0xFF1C3531)
    val SecondaryContainerDark = Color(0xFF334B47)
    val OnSecondaryContainerDark = Color(0xFFCCE8E2)

    val TertiaryDark = Color(0xFFADCAE6)
    val OnTertiaryDark = Color(0xFF153349)
    val TertiaryContainerDark = Color(0xFF2C4A60)
    val OnTertiaryContainerDark = Color(0xFFCBE6FF)

    val ErrorDark = Color(0xFFFFB4AB)
    val OnErrorDark = Color(0xFF690005)
    val ErrorContainerDark = Color(0xFF93000A)
    val OnErrorContainerDark = Color(0xFFFFDAD6)

    val BackgroundDark = Color(0xFF101413)
    val OnBackgroundDark = Color(0xFFE0E3E1)
    val SurfaceDark = Color(0xFF101413)
    val OnSurfaceDark = Color(0xFFE0E3E1)
    val SurfaceVariantDark = Color(0xFF3F4947)
    val OnSurfaceVariantDark = Color(0xFFBEC9C6)
    val OutlineDark = Color(0xFF899391)
    val OutlineVariantDark = Color(0xFF3F4947)
}
