package io.github.rwx.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.rwx.ui.ColorSchemeId
import io.github.rwx.ui.ColorSchemeRegistry
import io.github.rwx.ui.DEFAULT_OVERLAY_OPACITY
import io.github.rwx.ui.Palette
import io.github.rwx.ui.Scheme
import io.github.rwx.ui.normalizeOverlayOpacity
import io.github.rwx.ui.UiColor
import io.github.rwx.ui.generated.resources.NotoSansCJKsc_Regular
import io.github.rwx.ui.generated.resources.Res
import io.github.rwx.ui.generated.resources.ZenDots_Regular
import org.jetbrains.compose.resources.Font

fun UiColor.toComposeColor(): Color {
    return Color(r, g, b, a)
}

fun Color.toUiColor(): UiColor {
    return UiColor(red, green, blue, alpha)
}

fun Scheme<Palette<UiColor>>.toCompose(overlayOpacity: Float = 1f): Scheme<Palette<Color>> {
    val map = palette.map { it.toComposeColor() }
    val opacity = normalizeOverlayOpacity(overlayOpacity)
    fun Color.faded() = copy(alpha = alpha * opacity)
    return Scheme(
        id, displayName, map.copy(
            panelOverlay = map.panelOverlay.faded(),
            panelOverlayDark = map.panelOverlayDark.faded(),
            panelOverlayLight = map.panelOverlayLight.faded(),
            panelHud = map.panelHud.faded(),
            surfaceBase = map.surfaceBase.faded(),
            surfaceRaised = map.surfaceRaised.faded(),
            surfaceSunken = map.surfaceSunken.faded(),
            primaryContainer = map.primaryContainer.faded(),
        )
    )
}

fun ColorSchemeRegistry.schemeForCompose(id: ColorSchemeId, overlayOpacity: Float = 1f): Scheme<Palette<Color>> =
    schemeFor(id).toCompose(overlayOpacity)

object Spacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val tight: Dp = 6.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
}

object Corners {
    val sm: Dp = 8.dp
    val lg: Dp = 16.dp
}

object Layout {
    val mainMenuContentWidth: Dp = 1280.dp
    val mainMenuMinContentWidth: Dp = 280.dp
    val mainMenuMaxContentWidth: Dp = 1720.dp
    val mainMenuTitleHeight: Dp = 128.dp
    val mainMenuTileWidth: Dp = 172.dp
    val mainMenuTileMinWidth: Dp = 124.dp
    val mainMenuTileMaxWidth: Dp = 176.dp
    val mainMenuTileHeight: Dp = 124.dp
    val mainMenuTileIconSize: Dp = 44.dp
    val mainMenuColumnGap: Dp = 20.dp
    val mainMenuColumnStagger: Dp = 36.dp
    val mainMenuViewportHeight: Dp = 420.dp
    val mainMenuMinViewportHeight: Dp = 260.dp
    val mainMenuMaxViewportHeight: Dp = 460.dp
    val menuButtonWidth: Dp = 560.dp
    val menuButtonHeight: Dp = 56.dp
    val compactMenuButtonHeight: Dp = 34.dp
    val iconButtonSize: Dp = 56.dp
    val iconButtonGlyphSize: Dp = 28.dp
    val textButtonIconSlotSize: Dp = 32.dp
    val textButtonGlyphSize: Dp = 24.dp
    val dialogContentPadding: Dp = 20.dp
    val pageBottomReserve: Dp = 76.dp
    val barMinHeight: Dp = 48.dp
    val compactBarMinHeight: Dp = 40.dp
    val dialogElevation: Dp = 8.dp
    val contentIconSize: Dp = 24.dp
    val flagIconSize: Dp = 16.dp
    val compactHeightBreakpoint: Dp = 420.dp
    val shortHeightBreakpoint: Dp = 520.dp
}

expect object Fonts {
    val caption: TextUnit
    val hud: TextUnit
    val bodySmall: TextUnit
    val bodyMedium: TextUnit
    val bodyLarge: TextUnit
    val headingSmall: TextUnit
    val headingMedium: TextUnit
    val headingLarge: TextUnit
    val displayTitle: TextUnit
}

@Composable
private fun bodyFontFamily(): FontFamily =
    FontFamily(Font(Res.font.NotoSansCJKsc_Regular, FontWeight.Normal))

@Composable
private fun displayFontFamily(): FontFamily =
    FontFamily(Font(Res.font.ZenDots_Regular, FontWeight.Normal))

@Composable
private fun typography(): androidx.compose.material3.Typography {
    val body = bodyFontFamily()
    val display = displayFontFamily()
    return androidx.compose.material3.Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(fontFamily = display, fontWeight = FontWeight.Normal, fontSize = Fonts.displayTitle),
    displayMedium = androidx.compose.ui.text.TextStyle(fontFamily = display, fontWeight = FontWeight.Normal, fontSize = Fonts.headingLarge),
    displaySmall = androidx.compose.ui.text.TextStyle(fontFamily = display, fontWeight = FontWeight.Normal, fontSize = Fonts.headingMedium),
    headlineLarge = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.headingLarge),
    headlineMedium = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.headingMedium),
    headlineSmall = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.headingSmall),
    titleLarge = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.headingSmall),
    titleMedium = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.bodyLarge),
    titleSmall = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.bodyMedium),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.bodyLarge),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.bodyMedium),
    bodySmall = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.bodySmall),
    labelLarge = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.bodySmall),
    labelMedium = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.hud),
    labelSmall = androidx.compose.ui.text.TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = Fonts.caption),
    )
}

val LocalColorScheme = staticCompositionLocalOf {
    ColorSchemeRegistry.schemeForCompose(ColorSchemeRegistry.defaultSchemeId)
}

val LocalOverlayOpacity = staticCompositionLocalOf { DEFAULT_OVERLAY_OPACITY }

val currentColorScheme: Scheme<Palette<Color>>
    @Composable
    get() = LocalColorScheme.current

@Composable
fun UiTheme(colorSchemeId: ColorSchemeId, content: @Composable () -> Unit) =
    UiTheme(colorSchemeId, DEFAULT_OVERLAY_OPACITY, content)

@Composable
fun UiTheme(
    colorSchemeId: ColorSchemeId,
    overlayOpacity: Float,
    content: @Composable () -> Unit,
) {
    val opacity = normalizeOverlayOpacity(overlayOpacity)
    val scheme = ColorSchemeRegistry.schemeForCompose(colorSchemeId, opacity)
    val palette = scheme.palette
    androidx.compose.runtime.CompositionLocalProvider(
        LocalColorScheme provides scheme,
        LocalOverlayOpacity provides opacity,
    ) {
        androidx.compose.material3.MaterialTheme(
            colorScheme = androidx.compose.material3.darkColorScheme(
                primary = palette.primary,
                onPrimary = palette.onPrimary,
                primaryContainer = palette.primaryContainer,
                onPrimaryContainer = palette.textPrimary,
                secondary = palette.secondary,
                surface = palette.surfaceBase,
                onSurface = palette.textPrimary,
                onSurfaceVariant = palette.textSecondary,
                background = palette.panelOverlay,
                onBackground = palette.textPrimary,
                outline = palette.borderSubtle,
                error = palette.danger,
            ),
            typography = typography(),
            content = {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides palette.textPrimary,
                ) {
                    content()
                }
            },
        )
    }
}
