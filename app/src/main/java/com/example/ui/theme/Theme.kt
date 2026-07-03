package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    secondary = SecondarySlate,
    tertiary = TertiaryOrange,
    background = DarkBg,
    surface = CardBg,
    onPrimary = OnPrimaryWhite,
    onSecondary = OnSecondarySlate,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    secondary = SecondarySlate,
    tertiary = TertiaryOrange,
    background = DarkBg, // Keep it premium slate/dark by default
    surface = CardBg,
    onPrimary = OnPrimaryWhite,
    onSecondary = OnSecondarySlate,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep the theme dark/slate for the premium high-contrast look
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
