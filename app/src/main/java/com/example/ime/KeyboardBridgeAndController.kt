package com.example.ime

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.data.GeezLayoutMode
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardSettings
import com.example.data.KeyboardThemePreset
import com.example.data.OneHandedMode
import com.example.data.SettingsRepository
import com.example.data.db.ClipboardEntry
import com.example.data.db.KeyboardDataRepository
import com.example.engine.EthiopianDate
import com.example.engine.FidelKeySpec
import com.example.engine.PhoneticTransliterator
import com.example.engine.TriePredictionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class KeyboardPanelMode {
    KEYBOARD,
    SYMBOLS,
    GEEZ_NUMERALS,
    HANDWRITING,
    CLIPBOARD,
    SNIPPETS,
    CALENDAR,
    EMOJI,
    THEME_PICKER,
}

enum class ShiftState {
    OFF,
    ONCE,
    LOCKED,
}

interface KeyboardInputBridge {
    fun commitText(text: String)
    fun setComposingText(text: String)
    fun finishComposingText()
    fun deleteBeforeCursor(count: Int)
    fun getTextBeforeCursor(maxChars: Int): String
    fun moveCursor(steps: Int)
    fun performEnter()
}

class ImeInputConnectionBridge(
    private val inputConnectionProvider: () -> InputConnection?,
    private val editorInfoProvider: () -> EditorInfo?,
    private val keyEventSender: (Int) -> Unit,
) : KeyboardInputBridge {

    override fun commitText(text: String) {
        inputConnectionProvider()?.commitText(text, 1)
    }

    override fun setComposingText(text: String) {
        inputConnectionProvider()?.setComposingText(text, 1)
    }

    override fun finishComposingText() {
        inputConnectionProvider()?.finishComposingText()
    }

    override fun deleteBeforeCursor(count: Int) {
        if (count <= 0) return
        inputConnectionProvider()?.deleteSurroundingText(count, 0)
    }

    override fun getTextBeforeCursor(maxChars: Int): String {
        return inputConnectionProvider()?.getTextBeforeCursor(maxChars, 0)?.toString().orEmpty()
    }

    override fun moveCursor(steps: Int) {
        if (steps == 0) return
        val keyCode = if (steps < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        repeat(kotlin.math.abs(steps)) {
            keyEventSender(keyCode)
        }
    }

    override fun performEnter() {
        val ic = inputConnectionProvider() ?: return
        val info = editorInfoProvider()
        val actionId = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        val isMultiLine = info?.inputType?.and(android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
        if (!isMultiLine && actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(actionId)
        } else {
            ic.commitText("\n", 1)
        }
    }
}

class InAppTextFieldBridge(
    private val state: MutableState<TextFieldValue>,
) : KeyboardInputBridge {

    private var composingLength = 0

    override fun commitText(text: String) {
        val current = state.value
        val selStart = current.selection.min.coerceIn(0, current.text.length)
        val selEnd = current.selection.max.coerceIn(0, current.text.length)
        val removeStart = (selStart - composingLength).coerceAtLeast(0)
        val updated = current.text.replaceRange(removeStart, selEnd, text)
        val cursor = (removeStart + text.length).coerceIn(0, updated.length)
        composingLength = 0
        state.value = TextFieldValue(updated, TextRange(cursor))
    }

    override fun setComposingText(text: String) {
        val current = state.value
        val selStart = current.selection.min.coerceIn(0, current.text.length)
        val selEnd = current.selection.max.coerceIn(0, current.text.length)
        val removeStart = (selStart - composingLength).coerceAtLeast(0)
        val updated = current.text.replaceRange(removeStart, selEnd, text)
        val cursor = (removeStart + text.length).coerceIn(0, updated.length)
        composingLength = text.length
        state.value = TextFieldValue(updated, TextRange(cursor))
    }

    override fun finishComposingText() {
        composingLength = 0
    }

    override fun deleteBeforeCursor(count: Int) {
        if (count <= 0) return
        val current = state.value
        val selStart = current.selection.min.coerceIn(0, current.text.length)
        val selEnd = current.selection.max.coerceIn(0, current.text.length)
        if (selStart != selEnd) {
            val updated = current.text.removeRange(selStart, selEnd)
            composingLength = 0
            state.value = TextFieldValue(updated, TextRange(selStart))
            return
        }
        val deleteFrom = (selStart - count).coerceAtLeast(0)
        val updated = current.text.removeRange(deleteFrom, selStart)
        composingLength = (composingLength - count).coerceAtLeast(0)
        state.value = TextFieldValue(updated, TextRange(deleteFrom))
    }

    override fun getTextBeforeCursor(maxChars: Int): String {
        val current = state.value
        val end = current.selection.min.coerceIn(0, current.text.length)
        val start = (end - maxChars).coerceAtLeast(0)
        return current.text.substring(start, end)
    }

    override fun moveCursor(steps: Int) {
        val current = state.value
        val newPos = (current.selection.end + steps).coerceIn(0, current.text.length)
        composingLength = 0
        state.value = current.copy(selection = TextRange(newPos))
    }

    override fun performEnter() {
        commitText("\n")
    }
}

class KeyboardController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val bridge: KeyboardInputBridge,
    private val settingsRepository: SettingsRepository = SettingsRepository.getInstance(context),
    private val dataRepository: KeyboardDataRepository = KeyboardDataRepository.getInstance(context),
) {
    private val predictionEngine = TriePredictionEngine(dataRepository)
    private var phoneticTransliterator = PhoneticTransliterator(isTigrigna = false)
    private var currentTypedWord = StringBuilder()
    private var lastSpaceTapMillis: Long = 0L
    private var suggestionJob: Job? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val settings: StateFlow<KeyboardSettings> = settingsRepository.settingsFlow.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = KeyboardSettings(),
    )

    val clipboardEntries: StateFlow<List<ClipboardEntry>> = dataRepository.allClipboardEntries.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val pinnedSnippets: StateFlow<List<ClipboardEntry>> = dataRepository.pinnedSnippets.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _panelMode = MutableStateFlow(KeyboardPanelMode.KEYBOARD)
    val panelMode: StateFlow<KeyboardPanelMode> = _panelMode.asStateFlow()

    private val _activeFidelKey = MutableStateFlow<FidelKeySpec?>(null)
    val activeFidelKey: StateFlow<FidelKeySpec?> = _activeFidelKey.asStateFlow()

    private val _shiftState = MutableStateFlow(ShiftState.OFF)
    val shiftState: StateFlow<ShiftState> = _shiftState.asStateFlow()

    private val _geezAltLayer = MutableStateFlow(false)
    val geezAltLayer: StateFlow<Boolean> = _geezAltLayer.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    init {
        refreshSuggestions()
    }

    fun onStartInput() {
        resetComposingState()
        updateAutoCapitalization()
        refreshSuggestions()
    }

    fun setPanelMode(mode: KeyboardPanelMode) {
        _activeFidelKey.value = null
        _panelMode.value = if (_panelMode.value == mode && mode != KeyboardPanelMode.KEYBOARD) {
            KeyboardPanelMode.KEYBOARD
        } else {
            mode
        }
    }

    fun toggleGeezAltLayer() {
        performFeedback()
        _activeFidelKey.value = null
        _geezAltLayer.value = !_geezAltLayer.value
    }

    fun toggleShift() {
        performFeedback()
        _shiftState.value = when (_shiftState.value) {
            ShiftState.OFF -> ShiftState.ONCE
            ShiftState.ONCE -> ShiftState.LOCKED
            ShiftState.LOCKED -> ShiftState.OFF
        }
    }

    fun cycleOneHandedMode() {
        performFeedback()
        val current = settings.value.oneHandedMode
        val next = when (current) {
            OneHandedMode.NONE -> OneHandedMode.LEFT
            OneHandedMode.LEFT -> OneHandedMode.RIGHT
            OneHandedMode.RIGHT -> OneHandedMode.NONE
        }
        scope.launch { settingsRepository.updateOneHandedMode(next) }
    }

    fun setOneHandedMode(mode: OneHandedMode) {
        performFeedback()
        scope.launch { settingsRepository.updateOneHandedMode(mode) }
    }

    fun selectTheme(preset: KeyboardThemePreset) {
        performFeedback()
        scope.launch { settingsRepository.updateThemePreset(preset) }
    }

    fun switchLanguage(forward: Boolean = true) {
        performFeedback()
        finalizeCurrentWord()
        val current = settings.value
        val next = if (forward) current.nextEnabledLanguage() else current.previousEnabledLanguage()
        phoneticTransliterator = PhoneticTransliterator(isTigrigna = next == KeyboardLanguage.TIGRIGNA)
        _activeFidelKey.value = null
        _geezAltLayer.value = false
        if (_panelMode.value == KeyboardPanelMode.SYMBOLS || _panelMode.value == KeyboardPanelMode.GEEZ_NUMERALS) {
            _panelMode.value = KeyboardPanelMode.KEYBOARD
        }
        scope.launch {
            settingsRepository.updateActiveLanguage(next)
            updateAutoCapitalizationForLanguage(next)
            refreshSuggestionsFor(next)
        }
    }

    fun onGeezBaseKeyTap(spec: FidelKeySpec) {
        performFeedback()
        val active = _activeFidelKey.value
        if (active != null && active.baseChar == spec.baseChar) {
            // Tapping the same base key again commits the base (1st order) directly
            commitGeezChar(spec.baseChar)
            _activeFidelKey.value = null
        } else {
            _activeFidelKey.value = spec
        }
    }

    fun onGeezOrderSelected(fidelChar: String) {
        performFeedback()
        commitGeezChar(fidelChar)
        _activeFidelKey.value = null
    }

    private fun commitGeezChar(fidelChar: String) {
        bridge.commitText(fidelChar)
        currentTypedWord.append(fidelChar)
        refreshSuggestions()
    }

    fun onLatinKeyTap(rawChar: String) {
        performFeedback()
        _activeFidelKey.value = null
        val currentSettings = settings.value
        val lang = currentSettings.activeLanguage

        if (lang.isEthiopic && currentSettings.geezLayoutMode == GeezLayoutMode.PHONETIC) {
            if (phoneticTransliterator.currentPhoneticBuffer.isEmpty()) {
                phoneticTransliterator = PhoneticTransliterator(isTigrigna = lang == KeyboardLanguage.TIGRIGNA)
            }
            val ch = rawChar.firstOrNull() ?: return
            val adjusted = if (_shiftState.value != ShiftState.OFF) ch.uppercaseChar() else ch
            val composed = phoneticTransliterator.appendChar(adjusted)
            bridge.setComposingText(composed)
            if (_shiftState.value == ShiftState.ONCE) _shiftState.value = ShiftState.OFF
            refreshSuggestions(composed)
            return
        }

        val charToCommit = if (_shiftState.value != ShiftState.OFF) {
            rawChar.uppercase()
        } else {
            rawChar
        }
        bridge.commitText(charToCommit)
        currentTypedWord.append(charToCommit)
        if (_shiftState.value == ShiftState.ONCE) {
            _shiftState.value = ShiftState.OFF
        }
        refreshSuggestions()
    }

    fun onDirectTextInsert(text: String, appendSpaceIfHandwriting: Boolean = false) {
        performFeedback()
        _activeFidelKey.value = null
        finalizePhoneticIfActive()
        val toCommit = if (appendSpaceIfHandwriting && settings.value.autoSpaceOnHandwriting) {
            "$text "
        } else {
            text
        }
        bridge.commitText(toCommit)
        if (toCommit.endsWith(" ")) {
            recordWordIfValid(text)
            currentTypedWord.clear()
        } else {
            currentTypedWord.append(text)
        }
        refreshSuggestions()
    }

    fun onPunctuationTap(symbol: String) {
        performFeedback()
        _activeFidelKey.value = null
        finalizePhoneticIfActive()
        recordWordIfValid(currentTypedWord.toString())
        currentTypedWord.clear()
        bridge.commitText(symbol)
        updateAutoCapitalization()
        refreshSuggestions()
    }

    fun onSpaceTap() {
        performFeedback()
        _activeFidelKey.value = null
        val currentSettings = settings.value
        val lang = currentSettings.activeLanguage

        if (phoneticTransliterator.currentPhoneticBuffer.isNotEmpty()) {
            val composed = phoneticTransliterator.transliterate(phoneticTransliterator.currentPhoneticBuffer)
            bridge.finishComposingText()
            phoneticTransliterator.reset()
            recordWordIfValid(composed)
            currentTypedWord.clear()
            bridge.commitText(" ")
            refreshSuggestions()
            return
        }

        // Auto pick top suggestion on SPACE if enabled and user is mid-word
        if (currentSettings.autoPickOnSpace && currentTypedWord.isNotEmpty()) {
            val top = _suggestions.value.firstOrNull()
            if (top != null && top != currentTypedWord.toString()) {
                applySuggestion(top)
                return
            }
        }

        val now = System.currentTimeMillis()
        if (currentSettings.doubleSpaceFullStop && now - lastSpaceTapMillis < 380L) {
            val before = bridge.getTextBeforeCursor(2)
            if (before.endsWith(" ") && before.trim().isNotEmpty()) {
                bridge.deleteBeforeCursor(1)
                bridge.commitText("${lang.fullStopChar} ")
                lastSpaceTapMillis = 0L
                updateAutoCapitalization()
                refreshSuggestions()
                return
            }
        }
        lastSpaceTapMillis = now

        recordWordIfValid(currentTypedWord.toString())
        currentTypedWord.clear()
        bridge.commitText(" ")
        updateAutoCapitalization()
        refreshSuggestions()
    }

    fun onBackspaceTap() {
        performFeedback()
        if (_activeFidelKey.value != null) {
            _activeFidelKey.value = null
            return
        }
        if (phoneticTransliterator.currentPhoneticBuffer.isNotEmpty()) {
            val updated = phoneticTransliterator.backspace()
            if (updated.isEmpty()) {
                bridge.setComposingText("")
                bridge.finishComposingText()
            } else {
                bridge.setComposingText(updated)
            }
            refreshSuggestions(updated)
            return
        }
        bridge.deleteBeforeCursor(1)
        if (currentTypedWord.isNotEmpty()) {
            currentTypedWord.deleteCharAt(currentTypedWord.length - 1)
        }
        updateAutoCapitalization()
        refreshSuggestions()
    }

    fun onSmartDeleteWord() {
        performFeedback()
        _activeFidelKey.value = null
        if (phoneticTransliterator.currentPhoneticBuffer.isNotEmpty()) {
            phoneticTransliterator.reset()
            bridge.setComposingText("")
            bridge.finishComposingText()
            refreshSuggestions()
            return
        }
        val before = bridge.getTextBeforeCursor(48)
        if (before.isEmpty()) return
        var idx = before.length
        while (idx > 0 && before[idx - 1].isWhitespace()) idx--
        while (idx > 0 && !before[idx - 1].isWhitespace()) idx--
        val deleteCount = before.length - idx
        if (deleteCount > 0) {
            bridge.deleteBeforeCursor(deleteCount)
        }
        currentTypedWord.clear()
        updateAutoCapitalization()
        refreshSuggestions()
    }

    fun onEnterTap() {
        performFeedback()
        _activeFidelKey.value = null
        finalizeCurrentWord()
        bridge.performEnter()
        updateAutoCapitalization()
        refreshSuggestions()
    }

    fun onCursorMove(steps: Int) {
        if (steps == 0) return
        _activeFidelKey.value = null
        finalizeCurrentWord()
        bridge.moveCursor(steps)
    }

    fun applySuggestion(word: String) {
        performFeedback()
        _activeFidelKey.value = null
        if (phoneticTransliterator.currentPhoneticBuffer.isNotEmpty()) {
            phoneticTransliterator.reset()
            bridge.setComposingText(word)
            bridge.finishComposingText()
            bridge.commitText(" ")
        } else {
            val typed = currentTypedWord.toString()
            if (typed.isNotEmpty()) {
                bridge.deleteBeforeCursor(typed.length)
            }
            bridge.commitText("$word ")
        }
        recordWordIfValid(word)
        currentTypedWord.clear()
        updateAutoCapitalization()
        refreshSuggestions()
    }

    fun insertEthiopianDate(formattedDate: String) {
        performFeedback()
        finalizeCurrentWord()
        bridge.commitText("$formattedDate ")
        setPanelMode(KeyboardPanelMode.KEYBOARD)
    }

    fun saveClipboardSnippet(text: String, isPinned: Boolean, label: String? = null) {
        scope.launch {
            dataRepository.saveClipboardText(text = text, isPinned = isPinned, label = label)
        }
    }

    fun togglePinSnippet(entry: ClipboardEntry, label: String? = entry.label) {
        scope.launch { dataRepository.togglePin(entry, label) }
    }

    fun deleteClipboardEntry(id: Long) {
        scope.launch { dataRepository.deleteClipboardEntry(id) }
    }

    fun clearUnpinnedClipboard() {
        scope.launch { dataRepository.clearUnpinnedClipboard() }
    }

    fun todayEthiopianDate(): EthiopianDate = EthiopianDate.today()

    private fun finalizeCurrentWord() {
        finalizePhoneticIfActive()
        recordWordIfValid(currentTypedWord.toString())
        currentTypedWord.clear()
    }

    private fun finalizePhoneticIfActive() {
        if (phoneticTransliterator.currentPhoneticBuffer.isNotEmpty()) {
            val word = phoneticTransliterator.transliterate(phoneticTransliterator.currentPhoneticBuffer)
            bridge.finishComposingText()
            phoneticTransliterator.reset()
            recordWordIfValid(word)
        }
    }

    private fun recordWordIfValid(rawWord: String) {
        val cleaned = rawWord.trim().trim('፡', '።', '፣', '.', ',', '!', '?')
        if (cleaned.length < 2) return
        val activeLang = settings.value.activeLanguage
        // Bilingual mixed-script memory: tag pure Latin words typed in Ethiopic mode as English so
        // tech/chat words stay remembered across both modes!
        val detectedCode = if (activeLang.isEthiopic && cleaned.all { it in 'A'..'z' || it == '\'' }) {
            KeyboardLanguage.ENGLISH.code
        } else {
            activeLang.code
        }
        scope.launch {
            dataRepository.recordLearnedWord(cleaned, detectedCode)
        }
    }

    private fun updateAutoCapitalization() {
        updateAutoCapitalizationForLanguage(settings.value.activeLanguage)
    }

    private fun updateAutoCapitalizationForLanguage(language: KeyboardLanguage) {
        if (!settings.value.autoCapitalization || language.isEthiopic) {
            if (_shiftState.value == ShiftState.ONCE) _shiftState.value = ShiftState.OFF
            return
        }
        if (_shiftState.value == ShiftState.LOCKED) return
        val before = bridge.getTextBeforeCursor(12)
        val shouldCap = before.isEmpty() ||
            before.endsWith("\n") ||
            before.trimEnd().let { it.endsWith(".") || it.endsWith("!") || it.endsWith("?") }
        _shiftState.value = if (shouldCap && (before.isEmpty() || before.last().isWhitespace())) {
            ShiftState.ONCE
        } else {
            ShiftState.OFF
        }
    }

    private fun resetComposingState() {
        _activeFidelKey.value = null
        phoneticTransliterator.reset()
        currentTypedWord.clear()
    }

    private fun refreshSuggestions(overridePrefix: String? = null) {
        refreshSuggestionsFor(settings.value.activeLanguage, overridePrefix)
    }

    private fun refreshSuggestionsFor(
        language: KeyboardLanguage,
        overridePrefix: String? = null,
    ) {
        val prefix = overridePrefix ?: currentTypedWord.toString()
        suggestionJob?.cancel()
        suggestionJob = scope.launch {
            _suggestions.value = predictionEngine.getSuggestions(
                prefix = prefix,
                language = language,
                limit = 5,
            )
        }
    }

    private fun performFeedback() {
        val current = settings.value
        if (current.soundOnKeyPress) {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
        if (current.vibrateOnKeyPress && vibrator?.hasVibrator() == true) {
            val strength = current.vibrationStrength
            if (strength == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                val durationMs = (10L + (strength * 0.35f).toLong()).coerceIn(10L, 45L)
                val amplitude = ((strength / 100f) * 255).toInt().coerceIn(25, 255)
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            }
        }
    }
}
