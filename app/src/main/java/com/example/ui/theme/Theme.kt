package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ScarletPrimary,
    secondary = CrimsonBorder,
    tertiary = Color.White,
    background = MatteBlackBg,
    surface = DarkGreyBase,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = CrimsonBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ScarletPrimary,
    secondary = CrimsonBorder,
    tertiary = DarkGreyBase,
    background = LightBg,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightGrisOscuroText,
    onSurface = LightGrisOscuroText,
    outline = CrimsonBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // default to AP dark theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
