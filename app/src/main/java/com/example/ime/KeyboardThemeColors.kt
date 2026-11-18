package com.example.ime

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.KeyboardThemePreset

data class KeyboardPalette(
    val boardBackground: Color,
    val toolbarBackground: Color,
    val keyBackground: Color,
    val keyPressedBackground: Color,
    val actionKeyBackground: Color,
    val enterKeyBackground: Color,
    val enterKeyText: Color,
    val keyText: Color,
    val subKeyText: Color,
    val accent: Color,
    val accentSoft: Color,
    val popupBackground: Color,
    val popupText: Color,
    val divider: Color,
    val ethiopianGreen: Color = Color(0xFF16A34A),
    val ethiopianYellow: Color = Color(0xFFF59E0B),
    val ethiopianRed: Color = Color(0xFFDC2626),
)

@Composable
fun resolveKeyboardPalette(preset: KeyboardThemePreset): KeyboardPalette {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    return remember(preset, dark) {
        resolveKeyboardPalette(context, preset, dark)
    }
}

fun resolveKeyboardPalette(
    context: Context,
    preset: KeyboardThemePreset,
    isDark: Boolean,
): KeyboardPalette = when (preset) {
    KeyboardThemePreset.CLASSIC_FYNGEEZ -> KeyboardPalette(
        boardBackground = Color(0xFF1B1C1F),
        toolbarBackground = Color(0xFF232529),
        keyBackground = Color(0xFF2F3238),
        keyPressedBackground = Color(0xFF474B54),
        actionKeyBackground = Color(0xFF25272C),
        enterKeyBackground = Color(0xFFF59E0B),
        enterKeyText = Color(0xFF1B1300),
        keyText = Color(0xFFF8FAFC),
        subKeyText = Color(0xFFFBBF24),
        accent = Color(0xFFF59E0B),
        accentSoft = Color(0x33F59E0B),
        popupBackground = Color(0xFF3E424B),
        popupText = Color(0xFFFDE68A),
        divider = Color(0xFF35383F),
    )

    KeyboardThemePreset.PURE_AMOLED -> KeyboardPalette(
        boardBackground = Color(0xFF000000),
        toolbarBackground = Color(0xFF0A0A0A),
        keyBackground = Color(0xFF151517),
        keyPressedBackground = Color(0xFF29292E),
        actionKeyBackground = Color(0xFF0E0E10),
        enterKeyBackground = Color(0xFFF59E0B),
        enterKeyText = Color(0xFF000000),
        keyText = Color(0xFFFFFFFF),
        subKeyText = Color(0xFFF59E0B),
        accent = Color(0xFFF59E0B),
        accentSoft = Color(0x2EF59E0B),
        popupBackground = Color(0xFF222226),
        popupText = Color(0xFFFBBF24),
        divider = Color(0xFF1F1F23),
    )

    KeyboardThemePreset.WARM_PAPER -> KeyboardPalette(
        boardBackground = Color(0xFFEDE6D9),
        toolbarBackground = Color(0xFFE4DCCB),
        keyBackground = Color(0xFFFFFDF9),
        keyPressedBackground = Color(0xFFE7DFCF),
        actionKeyBackground = Color(0xFFDAD0BE),
        enterKeyBackground = Color(0xFFB45309),
        enterKeyText = Color(0xFFFFFFFF),
        keyText = Color(0xFF1C1917),
        subKeyText = Color(0xFFB45309),
        accent = Color(0xFFB45309),
        accentSoft = Color(0x26B45309),
        popupBackground = Color(0xFFFFFFFF),
        popupText = Color(0xFF78350F),
        divider = Color(0xFFD2C7B4),
    )

    KeyboardThemePreset.ETHIOPIAN_ACCENT -> KeyboardPalette(
        boardBackground = Color(0xFF121613),
        toolbarBackground = Color(0xFF19201B),
        keyBackground = Color(0xFF222B24),
        keyPressedBackground = Color(0xFF314034),
        actionKeyBackground = Color(0xFF1A221C),
        enterKeyBackground = Color(0xFF16A34A),
        enterKeyText = Color(0xFFFFFFFF),
        keyText = Color(0xFFF4F7F4),
        subKeyText = Color(0xFFFBBF24),
        accent = Color(0xFFF59E0B),
        accentSoft = Color(0x3316A34A),
        popupBackground = Color(0xFF29352C),
        popupText = Color(0xFFFDE68A),
        divider = Color(0xFF2A362C),
    )

    KeyboardThemePreset.MATERIAL_YOU -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            KeyboardPalette(
                boardBackground = scheme.surfaceContainerLow,
                toolbarBackground = scheme.surfaceContainer,
                keyBackground = if (isDark) scheme.surfaceContainerHigh else scheme.surface,
                keyPressedBackground = scheme.primaryContainer,
                actionKeyBackground = scheme.secondaryContainer,
                enterKeyBackground = scheme.primary,
                enterKeyText = scheme.onPrimary,
                keyText = scheme.onSurface,
                subKeyText = scheme.primary,
                accent = scheme.primary,
                accentSoft = scheme.primaryContainer.copy(alpha = 0.45f),
                popupBackground = scheme.primaryContainer,
                popupText = scheme.onPrimaryContainer,
                divider = scheme.outlineVariant,
            )
        } else {
            resolveKeyboardPalette(context, KeyboardThemePreset.CLASSIC_FYNGEEZ, isDark)
        }
    }
}
