package io.github.rwx.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.rwx.ui.Palette
import io.github.rwx.ui.Scheme
import io.github.rwx.ui.theme.*

/**
 * Renders an icon with the given size and color tint.
 */
@Composable
fun Icon(
    icon: Icon,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    AssetIcon(Icon.valueOf(icon.name), size, tint, modifier)
}

/**
 * Icon button matching Kool UI's IconButton component.
 */
@Composable
fun IconButton(
    icon: Icon,
    scheme: Scheme<Palette<Color>>,
    size: Dp = Layout.iconButtonSize,
    iconSize: Dp = Layout.iconButtonGlyphSize,
    onPressed: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState().value

    val background = if (isHovered) scheme.palette.surfaceRaised else scheme.palette.surfaceBase
    val border = if (isHovered) scheme.palette.primary else scheme.palette.borderSubtle
    val iconColor = if (isHovered) scheme.palette.secondary else scheme.palette.primary

    Box(
        modifier = Modifier
            .size(size)
            .padding(Spacing.xs)
            .background(background, RoundedCornerShape(Spacing.xs))
            .border(2.dp, border, RoundedCornerShape(Spacing.xs))
            .hoverable(interactionSource)
            .clickable(role = Role.Button, onClick = onPressed),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, iconSize, iconColor)
    }
}

/**
 * Text button with icon matching Kool UI's TextIconButton component.
 */
@Composable
fun TextIconButton(
    label: String,
    icon: Icon,
    width: Dp?,
    scheme: Scheme<Palette<Color>>,
    emphasized: Boolean = false,
    fontSize: TextUnit = Fonts.bodySmall,
    height: Dp = Layout.menuButtonHeight,
    onPressed: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState().value

    val background = when {
        isHovered -> scheme.palette.surfaceRaised
        emphasized -> scheme.palette.primaryContainer
        else -> scheme.palette.surfaceSunken
    }
    val border = if (isHovered) scheme.palette.primary else scheme.palette.borderSubtle
    val textColor = if (isHovered) scheme.palette.primary else scheme.palette.textPrimary
    val iconColor = if (isHovered) scheme.palette.secondary else scheme.palette.primary

    Box(
        modifier = Modifier
            .then(if (width != null) Modifier.width(width) else Modifier.wrapContentWidth())
            .height(height)
            .padding(Spacing.xs)
            .background(background, RoundedCornerShape(Spacing.xs))
            .border(1.dp, border, RoundedCornerShape(Spacing.xs))
            .hoverable(interactionSource)
            .clickable(role = Role.Button, onClick = onPressed)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(Layout.textButtonIconSlotSize),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, Layout.textButtonGlyphSize, iconColor)
            }
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = fontSize,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}
