package io.github.rwx.ui

const val DEFAULT_OVERLAY_OPACITY = 0.7f

fun normalizeOverlayOpacity(value: Float): Float =
    if (value.isFinite()) value.coerceIn(0f, 1f) else DEFAULT_OVERLAY_OPACITY

@JvmInline
value class ColorSchemeId(val value: String)

data class Palette<C>(
    val panelOverlay: C,
    val panelOverlayDark: C,
    val panelOverlayLight: C,
    val panelHud: C,
    val surfaceBase: C,
    val surfaceRaised: C,
    val surfaceSunken: C,
    val textPrimary: C,
    val textSecondary: C,
    val textDisabled: C,
    val primary: C,
    val primaryContainer: C,
    val onPrimary: C,
    val secondary: C,
    val danger: C,
    val borderSubtle: C,
) {
    fun <D> map(transform: (C) -> D): Palette<D> = Palette(
        panelOverlay = transform(panelOverlay),
        panelOverlayDark = transform(panelOverlayDark),
        panelOverlayLight = transform(panelOverlayLight),
        panelHud = transform(panelHud),
        surfaceBase = transform(surfaceBase),
        surfaceRaised = transform(surfaceRaised),
        surfaceSunken = transform(surfaceSunken),
        textPrimary = transform(textPrimary),
        textSecondary = transform(textSecondary),
        textDisabled = transform(textDisabled),
        primary = transform(primary),
        primaryContainer = transform(primaryContainer),
        onPrimary = transform(onPrimary),
        secondary = transform(secondary),
        danger = transform(danger),
        borderSubtle = transform(borderSubtle),
    )
}

data class Scheme<P>(
    val id: ColorSchemeId,
    val displayName: String,
    val palette: P,
)

object ColorSchemeRegistry {
    val defaultSchemeId: ColorSchemeId = ColorSchemeId("rwx")

    private val materialDarkDefaults = MaterialSeed()

    val schemes: List<Scheme<Palette<UiColor>>> = listOf(
        scheme(
            id = "rwx",
            displayName = "RWX",
            scheme = MaterialSeed(
                surface = UiColor("1b1212ff"),
                surfaceContainer = UiColor("444444ff"),
                onSurface = UiColor.WHITE,
                primaryContainer = UiColor("97bc62ff"),
                secondary = UiColor("5fbe5fff"),
                secondaryContainer = UiColor("97bc62ff"),
                primary = UiColor("97bc62ff"),
                onPrimary = UiColor.BLACK,
                background = UiColor("353935ff"),
                inversePrimary = UiColor("2c5f2dff"),
                surfaceTint = UiColor.WHITE,
            ),
        ),
        scheme(
            id = "material-purple",
            displayName = "Material Purple",
            scheme = materialDarkDefaults.copy(
                background = UiColor("302838ff"),
                surfaceContainer = UiColor("443c4cff"),
            ),
        ),
        scheme(
            id = "material-default",
            displayName = "Material Default",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("bb86fcff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("6200eeff"),
                secondary = UiColor("03dac6ff"),
                secondaryContainer = UiColor("005047ff"),
                tertiary = UiColor("03dac6ff"),
                tertiaryContainer = UiColor("003e3eff"),
                inversePrimary = UiColor("543f6aff"),
                background = UiColor("302838ff"),
                surfaceContainer = UiColor("443c4cff"),
            ),
        ),
        scheme(
            id = "amber-blue",
            displayName = "Amber Blue",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("ffb300ff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("c87200ff"),
                secondary = UiColor("82b1ffff"),
                secondaryContainer = UiColor("3770cfff"),
                tertiary = UiColor("448affff"),
                tertiaryContainer = UiColor("0b429cff"),
                inversePrimary = UiColor("6a510aff"),
                background = UiColor("302c28ff"),
                surfaceContainer = UiColor("44403cff"),
            ),
        ),
        scheme(
            id = "aqua-blue",
            displayName = "Aqua Blue",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("5db3d5ff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("297ea0ff"),
                secondary = UiColor("a1e9dfff"),
                secondaryContainer = UiColor("005049ff"),
                tertiary = UiColor("a0e5e5ff"),
                tertiaryContainer = UiColor("004f50ff"),
                inversePrimary = UiColor("2f515fff"),
                background = UiColor("283238ff"),
                surfaceContainer = UiColor("3c464cff"),
            ),
        ),
        scheme(
            id = "bahama-and-trinidad",
            displayName = "Bahama And Trinidad",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("4585b5ff"),
                onPrimary = UiColor.WHITE,
                primaryContainer = UiColor("095d9eff"),
                secondary = UiColor("e57c4aff"),
                secondaryContainer = UiColor("dd520fff"),
                tertiary = UiColor("9cd5f9ff"),
                tertiaryContainer = UiColor("3a7292ff"),
                inversePrimary = UiColor("253f52ff"),
                background = UiColor("283038ff"),
                surfaceContainer = UiColor("3c444cff"),
            ),
        ),
        scheme(
            id = "gold-sunset",
            displayName = "Gold Sunset",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("eda85eff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("b86914ff"),
                secondary = UiColor("d28f60ff"),
                secondaryContainer = UiColor("b5642cff"),
                tertiary = UiColor("ddab88ff"),
                tertiaryContainer = UiColor("bf7d4eff"),
                inversePrimary = UiColor("684d2fff"),
                background = UiColor("383028ff"),
                surfaceContainer = UiColor("4c443cff"),
            ),
        ),
        scheme(
            id = "flutter-dash",
            displayName = "Flutter Dash",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("b4e6ffff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("1e8fdbff"),
                secondary = UiColor("99ccf9ff"),
                secondaryContainer = UiColor("202b6dff"),
                tertiary = UiColor("baa99dff"),
                tertiaryContainer = UiColor("514239ff"),
                inversePrimary = UiColor("52666aff"),
                background = UiColor("283238ff"),
                surfaceContainer = UiColor("3c464cff"),
            ),
        ),
        scheme(
            id = "hippie-blue",
            displayName = "Hippie Blue",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("669db3ff"),
                onPrimary = UiColor.WHITE,
                primaryContainer = UiColor("078282ff"),
                secondary = UiColor("fc6e75ff"),
                secondaryContainer = UiColor("92001aff"),
                tertiary = UiColor("f75f67ff"),
                tertiaryContainer = UiColor("580810ff"),
                inversePrimary = UiColor("324851ff"),
                background = UiColor("283238ff"),
                surfaceContainer = UiColor("3c464cff"),
            ),
        ),
        scheme(
            id = "pink-sakura",
            displayName = "Pink Sakura",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("eec4d8ff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("ce5b78ff"),
                secondary = UiColor("f5d6c6ff"),
                secondaryContainer = UiColor("eba689ff"),
                tertiary = UiColor("f7e0d4ff"),
                tertiaryContainer = UiColor("eebda8ff"),
                inversePrimary = UiColor("695860ff"),
                background = UiColor("383034ff"),
                surfaceContainer = UiColor("4c4448ff"),
            ),
        ),
        scheme(
            id = "blumine",
            displayName = "Blumine",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("82baceff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("04666fff"),
                secondary = UiColor("ffd682ff"),
                secondaryContainer = UiColor("9e7910ff"),
                tertiary = UiColor("243e4dff"),
                tertiaryContainer = UiColor("426173ff"),
                inversePrimary = UiColor("3e545cff"),
                background = UiColor("283238ff"),
                surfaceContainer = UiColor("3c464cff"),
            ),
        ),
        scheme(
            id = "green-money",
            displayName = "Green Money",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("7ab893ff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("224430ff"),
                secondary = UiColor("d5d6a8ff"),
                secondaryContainer = UiColor("515402ff"),
                tertiary = UiColor("bbbe74ff"),
                tertiaryContainer = UiColor("404204ff"),
                inversePrimary = UiColor("3a5344ff"),
                background = UiColor("28322cff"),
                surfaceContainer = UiColor("3c4640ff"),
            ),
        ),
        scheme(
            id = "rosewood",
            displayName = "Rosewood",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("9c5a69ff"),
                onPrimary = UiColor.WHITE,
                primaryContainer = UiColor("5f111eff"),
                secondary = UiColor("edce9bff"),
                secondaryContainer = UiColor("805e23ff"),
                tertiary = UiColor("f5dfb9ff"),
                tertiaryContainer = UiColor("8e6e3cff"),
                inversePrimary = UiColor("482e34ff"),
                background = UiColor("382c30ff"),
                surfaceContainer = UiColor("4c4044ff"),
            ),
        ),
        scheme(
            id = "verdun-lime",
            displayName = "Verdun Lime",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("bcd063ff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("3f4c00ff"),
                secondary = UiColor("ffe17bff"),
                secondaryContainer = UiColor("3b2f00ff"),
                tertiary = UiColor("78d3ecff"),
                tertiaryContainer = UiColor("224e43ff"),
                inversePrimary = UiColor("555d31ff"),
                background = UiColor("303228ff"),
                surfaceContainer = UiColor("44463cff"),
            ),
        ),
        scheme(
            id = "grey-law",
            displayName = "Grey Law",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("90a4aeff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("37474fff"),
                secondary = UiColor("815aa3ff"),
                secondaryContainer = UiColor("421f62ff"),
                tertiary = UiColor("373d5cff"),
                tertiaryContainer = UiColor("1d2449ff"),
                inversePrimary = UiColor("434b4fff"),
                background = UiColor("303234ff"),
                surfaceContainer = UiColor("444648ff"),
            ),
        ),
        scheme(
            id = "red-tornado",
            displayName = "Red Tornado",
            scheme = materialDarkDefaults.copy(
                primary = UiColor("ef9a9aff"),
                onPrimary = UiColor.BLACK,
                primaryContainer = UiColor("b71c1cff"),
                secondary = UiColor("f8bbd0ff"),
                secondaryContainer = UiColor("ad1457ff"),
                tertiary = UiColor("fce4ecff"),
                tertiaryContainer = UiColor("c2185bff"),
                inversePrimary = UiColor("694747ff"),
                background = UiColor("382c2cff"),
                surfaceContainer = UiColor("4c4040ff"),
            ),
        ),
    )

    fun schemeFor(id: ColorSchemeId): Scheme<Palette<UiColor>> {
        return schemes.firstOrNull { it.id == id } ?: schemes.first()
    }

    private data class MaterialSeed(
        val primary: UiColor = UiColor("d0bcffff"),
        val onPrimary: UiColor = UiColor("381e72ff"),
        val primaryContainer: UiColor = UiColor("4f378bff"),
        val secondary: UiColor = UiColor("ccc2dcff"),
        val secondaryContainer: UiColor = UiColor("4a4458ff"),
        val tertiary: UiColor = UiColor("e6e0e9ff"),
        val tertiaryContainer: UiColor = UiColor("3b383eff"),
        val inversePrimary: UiColor = UiColor("d0bcffff"),
        val background: UiColor = UiColor("141218ff"),
        val surface: UiColor = UiColor("141218ff"),
        val surfaceContainer: UiColor = UiColor("211f26ff"),
        val onSurface: UiColor = UiColor("e6e0e9ff"),
        val surfaceTint: UiColor = primary,
    )

    private fun scheme(
        id: String,
        displayName: String,
        scheme: MaterialSeed,
    ): Scheme<Palette<UiColor>> = Scheme<Palette<UiColor>>(
        id = ColorSchemeId(id),
        displayName = displayName,
        palette = scheme.toPalette(),
    )

    private fun MaterialSeed.toPalette(): Palette<UiColor> = Palette(
        panelOverlay = background.withAlpha(0.82f),
        panelOverlayDark = background.withAlpha(0.9f),
        panelOverlayLight = background.withAlpha(0.72f),
        panelHud = background.withAlpha(0.82f),
        surfaceBase = surface,
        surfaceRaised = surfaceContainer,
        surfaceSunken = surface.mix(UiColor.BLACK, 0.35f),
        textPrimary = onSurface,
        textSecondary = onSurface.withAlpha(0.78f),
        textDisabled = onSurface.withAlpha(0.42f),
        primary = primary,
        primaryContainer = primaryContainer,
        onPrimary = onPrimary,
        secondary = secondary,
        danger = UiColor("ff6b6bff"),
        borderSubtle = surfaceContainer,
    )
}
