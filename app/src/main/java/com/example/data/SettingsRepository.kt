package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.fynLegacyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "fynlegacy_settings"
)

class SettingsRepository private constructor(private val context: Context) {

    private object Keys {
        val BOLD_FIDEL_FONT = booleanPreferencesKey("bold_fidel_font")
        val KEYBOARD_HEIGHT_DP = floatPreferencesKey("keyboard_height_dp")
        val SHOW_SUB_KEYS = booleanPreferencesKey("show_sub_keys")
        val SHOW_NUMBER_ROW = booleanPreferencesKey("show_number_row")
        val SHOW_KEY_PREVIEW = booleanPreferencesKey("show_key_preview")
        val SHOW_SUGGESTIONS_AT_BOTTOM = booleanPreferencesKey("show_suggestions_at_bottom")
        val LOW_LATENCY_MODE = booleanPreferencesKey("low_latency_mode")

        val SOUND_ON_KEY_PRESS = booleanPreferencesKey("sound_on_key_press")
        val VIBRATE_ON_KEY_PRESS = booleanPreferencesKey("vibrate_on_key_press")
        val VIBRATION_STRENGTH = intPreferencesKey("vibration_strength")

        val SHOW_SUGGESTIONS = booleanPreferencesKey("show_suggestions")
        val DOUBLE_SPACE_FULL_STOP = booleanPreferencesKey("double_space_full_stop")
        val AUTO_CAPITALIZATION = booleanPreferencesKey("auto_capitalization")
        val SWAP_SPACE_AND_PUNCTUATION = booleanPreferencesKey("swap_space_and_punctuation")
        val AUTO_PICK_ON_SPACE = booleanPreferencesKey("auto_pick_on_space")
        val AUTO_SPACE_ON_HANDWRITING = booleanPreferencesKey("auto_space_on_handwriting")

        val GEEZ_LAYOUT_MODE = stringPreferencesKey("geez_layout_mode")
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val ACTIVE_LANGUAGE = stringPreferencesKey("active_language")
        val ONE_HANDED_MODE = stringPreferencesKey("one_handed_mode")

        val ENABLE_ENGLISH = booleanPreferencesKey("enable_english")
        val ENABLE_AMHARIC = booleanPreferencesKey("enable_amharic")
        val ENABLE_AFAAN_OROMO = booleanPreferencesKey("enable_afaan_oromo")
        val ENABLE_TIGRIGNA = booleanPreferencesKey("enable_tigrigna")
    }

    val settingsFlow: Flow<KeyboardSettings> = context.fynLegacyDataStore.data.map { prefs ->
        val enableAm = prefs[Keys.ENABLE_AMHARIC] ?: true
        val enableTi = prefs[Keys.ENABLE_TIGRIGNA] ?: true
        val enableOm = prefs[Keys.ENABLE_AFAAN_OROMO] ?: true
        val enableEn = prefs[Keys.ENABLE_ENGLISH] ?: true
        val requestedLang = KeyboardLanguage.fromCode(prefs[Keys.ACTIVE_LANGUAGE])

        val temp = KeyboardSettings(
            boldFidelFont = prefs[Keys.BOLD_FIDEL_FONT] ?: true,
            keyboardHeightDp = prefs[Keys.KEYBOARD_HEIGHT_DP] ?: 264f,
            showSubKeys = prefs[Keys.SHOW_SUB_KEYS] ?: true,
            showNumberRow = prefs[Keys.SHOW_NUMBER_ROW] ?: false,
            showKeyPreview = prefs[Keys.SHOW_KEY_PREVIEW] ?: true,
            showSuggestionsAtBottom = prefs[Keys.SHOW_SUGGESTIONS_AT_BOTTOM] ?: false,
            lowLatencyMode = prefs[Keys.LOW_LATENCY_MODE] ?: true,
            soundOnKeyPress = prefs[Keys.SOUND_ON_KEY_PRESS] ?: false,
            vibrateOnKeyPress = prefs[Keys.VIBRATE_ON_KEY_PRESS] ?: true,
            vibrationStrength = prefs[Keys.VIBRATION_STRENGTH] ?: 0,
            showSuggestions = prefs[Keys.SHOW_SUGGESTIONS] ?: true,
            doubleSpaceFullStop = prefs[Keys.DOUBLE_SPACE_FULL_STOP] ?: true,
            autoCapitalization = prefs[Keys.AUTO_CAPITALIZATION] ?: true,
            swapSpaceAndPunctuation = prefs[Keys.SWAP_SPACE_AND_PUNCTUATION] ?: false,
            autoPickOnSpace = prefs[Keys.AUTO_PICK_ON_SPACE] ?: false,
            autoSpaceOnHandwriting = prefs[Keys.AUTO_SPACE_ON_HANDWRITING] ?: true,
            geezLayoutMode = GeezLayoutMode.fromId(prefs[Keys.GEEZ_LAYOUT_MODE]),
            themePreset = KeyboardThemePreset.fromId(prefs[Keys.THEME_PRESET]),
            activeLanguage = requestedLang,
            oneHandedMode = OneHandedMode.fromId(prefs[Keys.ONE_HANDED_MODE]),
            enableEnglish = enableEn,
            enableAmharic = enableAm,
            enableAfaanOromo = enableOm,
            enableTigrigna = enableTi,
        )
        val enabled = temp.enabledLanguages()
        if (temp.activeLanguage in enabled) temp else temp.copy(activeLanguage = enabled.first())
    }

    suspend fun updateBoldFidelFont(enabled: Boolean) = edit { it[Keys.BOLD_FIDEL_FONT] = enabled }
    suspend fun updateKeyboardHeight(dp: Float) = edit { it[Keys.KEYBOARD_HEIGHT_DP] = dp.coerceIn(220f, 340f) }
    suspend fun updateShowSubKeys(enabled: Boolean) = edit { it[Keys.SHOW_SUB_KEYS] = enabled }
    suspend fun updateShowNumberRow(enabled: Boolean) = edit { it[Keys.SHOW_NUMBER_ROW] = enabled }
    suspend fun updateShowKeyPreview(enabled: Boolean) = edit { it[Keys.SHOW_KEY_PREVIEW] = enabled }
    suspend fun updateShowSuggestionsAtBottom(enabled: Boolean) = edit { it[Keys.SHOW_SUGGESTIONS_AT_BOTTOM] = enabled }
    suspend fun updateLowLatencyMode(enabled: Boolean) = edit { it[Keys.LOW_LATENCY_MODE] = enabled }

    suspend fun updateSoundOnKeyPress(enabled: Boolean) = edit { it[Keys.SOUND_ON_KEY_PRESS] = enabled }
    suspend fun updateVibrateOnKeyPress(enabled: Boolean) = edit { it[Keys.VIBRATE_ON_KEY_PRESS] = enabled }
    suspend fun updateVibrationStrength(strength: Int) = edit { it[Keys.VIBRATION_STRENGTH] = strength.coerceIn(0, 100) }

    suspend fun updateShowSuggestions(enabled: Boolean) = edit { it[Keys.SHOW_SUGGESTIONS] = enabled }
    suspend fun updateDoubleSpaceFullStop(enabled: Boolean) = edit { it[Keys.DOUBLE_SPACE_FULL_STOP] = enabled }
    suspend fun updateAutoCapitalization(enabled: Boolean) = edit { it[Keys.AUTO_CAPITALIZATION] = enabled }
    suspend fun updateSwapSpaceAndPunctuation(enabled: Boolean) = edit { it[Keys.SWAP_SPACE_AND_PUNCTUATION] = enabled }
    suspend fun updateAutoPickOnSpace(enabled: Boolean) = edit { it[Keys.AUTO_PICK_ON_SPACE] = enabled }
    suspend fun updateAutoSpaceOnHandwriting(enabled: Boolean) = edit { it[Keys.AUTO_SPACE_ON_HANDWRITING] = enabled }

    suspend fun updateGeezLayoutMode(mode: GeezLayoutMode) = edit { it[Keys.GEEZ_LAYOUT_MODE] = mode.id }
    suspend fun updateThemePreset(preset: KeyboardThemePreset) = edit { it[Keys.THEME_PRESET] = preset.id }
    suspend fun updateActiveLanguage(language: KeyboardLanguage) = edit { it[Keys.ACTIVE_LANGUAGE] = language.code }
    suspend fun updateOneHandedMode(mode: OneHandedMode) = edit { it[Keys.ONE_HANDED_MODE] = mode.id }

    suspend fun updateLanguageEnabled(language: KeyboardLanguage, enabled: Boolean) {
        edit { prefs ->
            when (language) {
                KeyboardLanguage.AMHARIC -> prefs[Keys.ENABLE_AMHARIC] = enabled
                KeyboardLanguage.TIGRIGNA -> prefs[Keys.ENABLE_TIGRIGNA] = enabled
                KeyboardLanguage.AFAAN_OROMO -> prefs[Keys.ENABLE_AFAAN_OROMO] = enabled
                KeyboardLanguage.ENGLISH -> prefs[Keys.ENABLE_ENGLISH] = enabled
            }
            // Guarantee at least one language stays enabled
            val anyEnabled = (prefs[Keys.ENABLE_AMHARIC] ?: true) ||
                (prefs[Keys.ENABLE_TIGRIGNA] ?: true) ||
                (prefs[Keys.ENABLE_AFAAN_OROMO] ?: true) ||
                (prefs[Keys.ENABLE_ENGLISH] ?: true)
            if (!anyEnabled) {
                prefs[Keys.ENABLE_ENGLISH] = true
            }
        }
    }

    private suspend inline fun edit(crossinline block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.fynLegacyDataStore.edit { block(it) }
    }

    companion object {
        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
    }
}
