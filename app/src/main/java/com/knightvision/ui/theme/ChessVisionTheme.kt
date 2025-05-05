package com.knightvision.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


// Knight Vision colors - Isn't this colour notation peculiar
val NavyBlue = Color(0xFF4D4B6E)     // primary button colour
val DarkerNavyBlue = Color(0xFF4D4B6E)
val LightGray = Color(0xFFE8E8E8)     // secondary button
val DarkGray = Color(0xFF666666)      // button text
val White = Color(0xFFFFFFFF)         // text on darker surfaces
val Black = Color(0xFF000000)         // text on lighter surfaces


private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = White,
    primaryContainer = DarkerNavyBlue,
    onPrimaryContainer = White,
    secondary = LightGray,
    onSecondary = DarkGray,
    secondaryContainer = LightGray,
    onSecondaryContainer = DarkGray,
    tertiary = NavyBlue,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray
)


@Composable
fun ChessVisionTheme(

    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}