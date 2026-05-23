package com.juvisus.copa2026app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = YellowGold,
    secondary = GreenAccent,
    background = MidnightBg,
    surface = MidnightBg,
    primaryContainer = GlassBg,
    onPrimaryContainer = TextWhite
)

@Composable
fun CopaAlerta2026Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
