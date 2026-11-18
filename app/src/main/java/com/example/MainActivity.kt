package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.CompanionTab
import com.example.ui.FynLegacyApp
import com.example.ui.theme.FynLegacyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialTab = when (intent?.getStringExtra("start_tab")) {
            "settings" -> CompanionTab.SETTINGS
            "themes" -> CompanionTab.THEMES
            "languages" -> CompanionTab.LANGUAGES
            else -> CompanionTab.HOME
        }

        setContent {
            FynLegacyTheme {
                FynLegacyApp(initialTab = initialTab)
            }
        }
    }
}
