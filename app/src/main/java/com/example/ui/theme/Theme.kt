package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val FynDarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = AmberOnPrimary,
    primaryContainer = AmberPrimaryContainer,
    onPrimaryContainer = AmberOnPrimaryContainer,
    secondary = EmeraldAccent,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = Color(0xFFBBF7D0),
    tertiary = TerracottaAccent,
    background = CharcoalBackground,
    onBackground = Color(0xFFF3F4F6),
    surface = CharcoalSurface,
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = Color(0xFFB8BDC7),
    outline = CharcoalOutline,
)

private val FynLightColorScheme = lightColorScheme(
    primary = AmberLightPrimary,
    onPrimary = AmberLightOnPrimary,
    primaryContainer = AmberLightPrimaryContainer,
    onPrimaryContainer = AmberLightOnPrimaryContainer,
    secondary = EmeraldAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF052E16),
    tertiary = TerracottaAccent,
    background = ParchmentBackground,
    onBackground = Color(0xFF1C1917),
    surface = ParchmentSurface,
    onSurface = Color(0xFF1C1917),
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = Color(0xFF57534E),
    outline = ParchmentOutline,
)

@Composable
fun FynLegacyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FynDarkColorScheme
        else -> FynLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FynLegacyTypography,
        content = content,
    )
}
