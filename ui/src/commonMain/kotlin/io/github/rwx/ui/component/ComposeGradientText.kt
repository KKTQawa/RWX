package io.github.rwx.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

/**
 * Compose implementation of GradientText component.
 * Replicates Kool UI's GradientText with linear gradient shader.
 */
@Composable
fun BoxScope.GradientText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit,
    startColor: Color,
    endColor: Color,
    textAlign: TextAlign = TextAlign.Center,
    fontFamily: FontFamily = MaterialTheme.typography.displayLarge.fontFamily ?: FontFamily.Default,
) {
    Text(
        text = text,
        modifier = modifier.align(Alignment.Center),
        style = TextStyle(
            fontSize = fontSize,
            fontFamily = fontFamily,
            brush = Brush.linearGradient(
                colors = listOf(startColor, endColor),
                start = Offset.Zero,
                end = Offset.Infinite,
            ),
            textAlign = textAlign,
        )
    )
}
