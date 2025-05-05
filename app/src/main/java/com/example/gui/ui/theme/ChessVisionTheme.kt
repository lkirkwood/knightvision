package com.example.gui.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorPalette = lightColorScheme(
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.White
)

@Composable
fun ChessVisionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorPalette,
        content = content
    )
}