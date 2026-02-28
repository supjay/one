package com.raven.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RavenSecondary,
    onPrimary = RavenOnSecondary,
    primaryContainer = RavenPrimaryContainer,
    onPrimaryContainer = RavenOnPrimaryContainer,
    secondary = RavenTertiary,
    onSecondary = RavenOnTertiary,
    background = RavenBackground,
    onBackground = RavenOnBackground,
    surface = RavenSurface,
    onSurface = RavenOnSurface,
    surfaceVariant = RavenSurfaceVariant,
    onSurfaceVariant = RavenOnSurfaceVariant,
    error = RavenError,
    onError = RavenOnError
)

private val LightColorScheme = lightColorScheme(
    primary = RavenLightPrimary,
    onPrimary = Color.White,
    primaryContainer = RavenLightSurfaceVariant,
    onPrimaryContainer = RavenLightPrimary,
    secondary = RavenLightSecondary,
    onSecondary = Color.White,
    background = RavenLightBackground,
    onBackground = RavenLightPrimary,
    surface = RavenLightSurface,
    onSurface = RavenLightPrimary,
    surfaceVariant = RavenLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF44446A)
)

@Composable
fun RavenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RavenTypography,
        content = content
    )
}
