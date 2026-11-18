package com.example.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeezLayoutMode
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardSettings
import com.example.data.KeyboardThemePreset
import com.example.data.SettingsRepository
import com.example.data.db.KeyboardDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ImeActivationStatus(
    val isEnabledInSystem: Boolean = false,
    val isSelectedAsDefault: Boolean = false,
)

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository.getInstance(application)
    private val dataRepository = KeyboardDataRepository.getInstance(application)

    val settings: StateFlow<KeyboardSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KeyboardSettings(),
    )

    val totalLearnedWords: StateFlow<Int> = dataRepository.totalLearnedWordCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0,
    )

    val learnedCountsByLanguage: StateFlow<Map<KeyboardLanguage, Int>> = combine(
        dataRepository.observeLearnedCount(KeyboardLanguage.AMHARIC.code),
        dataRepository.observeLearnedCount(KeyboardLanguage.TIGRIGNA.code),
        dataRepository.observeLearnedCount(KeyboardLanguage.AFAAN_OROMO.code),
        dataRepository.observeLearnedCount(KeyboardLanguage.ENGLISH.code),
    ) { am, ti, om, en ->
        mapOf(
            KeyboardLanguage.AMHARIC to am,
            KeyboardLanguage.TIGRIGNA to ti,
            KeyboardLanguage.AFAAN_OROMO to om,
            KeyboardLanguage.ENGLISH to en,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap(),
    )

    private val _imeStatus = MutableStateFlow(ImeActivationStatus())
    val imeStatus: StateFlow<ImeActivationStatus> = _imeStatus.asStateFlow()

    init {
        refreshImeStatus()
    }

    fun refreshImeStatus() {
        val context = getApplication<Application>()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val pkg = context.packageName
        val enabled = imm?.enabledInputMethodList?.any { it.packageName == pkg } == true
        val defaultIme = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD,
        ).orEmpty()
        val selected = defaultIme.startsWith("$pkg/")
        _imeStatus.value = ImeActivationStatus(
            isEnabledInSystem = enabled,
            isSelectedAsDefault = selected,
        )
    }

    fun setBoldFidelFont(v: Boolean) = viewModelScope.launch { settingsRepository.updateBoldFidelFont(v) }
    fun setKeyboardHeight(v: Float) = viewModelScope.launch { settingsRepository.updateKeyboardHeight(v) }
    fun setShowSubKeys(v: Boolean) = viewModelScope.launch { settingsRepository.updateShowSubKeys(v) }
    fun setShowNumberRow(v: Boolean) = viewModelScope.launch { settingsRepository.updateShowNumberRow(v) }
    fun setShowKeyPreview(v: Boolean) = viewModelScope.launch { settingsRepository.updateShowKeyPreview(v) }
    fun setShowSuggestionsAtBottom(v: Boolean) = viewModelScope.launch { settingsRepository.updateShowSuggestionsAtBottom(v) }
    fun setLowLatencyMode(v: Boolean) = viewModelScope.launch { settingsRepository.updateLowLatencyMode(v) }

    fun setSoundOnKeyPress(v: Boolean) = viewModelScope.launch { settingsRepository.updateSoundOnKeyPress(v) }
    fun setVibrateOnKeyPress(v: Boolean) = viewModelScope.launch { settingsRepository.updateVibrateOnKeyPress(v) }
    fun setVibrationStrength(v: Int) = viewModelScope.launch { settingsRepository.updateVibrationStrength(v) }

    fun setShowSuggestions(v: Boolean) = viewModelScope.launch { settingsRepository.updateShowSuggestions(v) }
    fun setDoubleSpaceFullStop(v: Boolean) = viewModelScope.launch { settingsRepository.updateDoubleSpaceFullStop(v) }
    fun setAutoCapitalization(v: Boolean) = viewModelScope.launch { settingsRepository.updateAutoCapitalization(v) }
    fun setSwapSpaceAndPunctuation(v: Boolean) = viewModelScope.launch { settingsRepository.updateSwapSpaceAndPunctuation(v) }
    fun setAutoPickOnSpace(v: Boolean) = viewModelScope.launch { settingsRepository.updateAutoPickOnSpace(v) }
    fun setAutoSpaceOnHandwriting(v: Boolean) = viewModelScope.launch { settingsRepository.updateAutoSpaceOnHandwriting(v) }

    fun setGeezLayoutMode(mode: GeezLayoutMode) = viewModelScope.launch { settingsRepository.updateGeezLayoutMode(mode) }
    fun setThemePreset(preset: KeyboardThemePreset) = viewModelScope.launch { settingsRepository.updateThemePreset(preset) }
    fun setActiveLanguage(language: KeyboardLanguage) = viewModelScope.launch { settingsRepository.updateActiveLanguage(language) }
    fun setLanguageEnabled(language: KeyboardLanguage, enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateLanguageEnabled(language, enabled)
    }

    fun clearLearnedDictionary() = viewModelScope.launch {
        dataRepository.clearLearnedWords()
    }
}
