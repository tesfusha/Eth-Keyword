package com.example.engine

import com.example.data.KeyboardLanguage

/**
 * Contract implemented by each language-specific IME layout & input module.
 */
interface ImeLanguageModule {
    val language: KeyboardLanguage
    val usesShiftForCaps: Boolean
    val supportsPhoneticMode: Boolean
    val hasDedicatedHudhaaKey: Boolean
    val fullStopCharacter: String

    fun primaryQuickPunctuation(): List<String>
    fun baseRows(): List<List<String>>
}

object AmharicLanguageModule : ImeLanguageModule {
    override val language = KeyboardLanguage.AMHARIC
    override val usesShiftForCaps = false
    override val supportsPhoneticMode = true
    override val hasDedicatedHudhaaKey = false
    override val fullStopCharacter = "።"

    override fun primaryQuickPunctuation(): List<String> = GeezFidelData.ethiopicPunctuation

    override fun baseRows(): List<List<String>> = listOf(
        GeezFidelData.simpleRow1Amharic.map { it.baseChar },
        GeezFidelData.simpleRow2.map { it.baseChar },
        GeezFidelData.simpleRow3.map { it.baseChar },
    )
}

object TigrignaLanguageModule : ImeLanguageModule {
    override val language = KeyboardLanguage.TIGRIGNA
    override val usesShiftForCaps = false
    override val supportsPhoneticMode = true
    override val hasDedicatedHudhaaKey = false
    override val fullStopCharacter = "።"

    override fun primaryQuickPunctuation(): List<String> = GeezFidelData.ethiopicPunctuation

    override fun baseRows(): List<List<String>> = listOf(
        GeezFidelData.simpleRow1Tigrigna.map { it.baseChar },
        GeezFidelData.simpleRow2.map { it.baseChar },
        GeezFidelData.simpleRow3.map { it.baseChar },
    )
}

object AfaanOromoLanguageModule : ImeLanguageModule {
    override val language = KeyboardLanguage.AFAAN_OROMO
    override val usesShiftForCaps = true
    override val supportsPhoneticMode = false
    override val hasDedicatedHudhaaKey = true
    override val fullStopCharacter = "."

    override fun primaryQuickPunctuation(): List<String> = listOf("'", ",", ".", "?", "!", "-", ":")

    // Qubee QWERTY with dedicated 1-tap Hudhaa (') key on main keyboard layer
    override fun baseRows(): List<List<String>> = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "'"),
        listOf("z", "x", "c", "v", "b", "n", "m"),
    )
}

object EnglishLanguageModule : ImeLanguageModule {
    override val language = KeyboardLanguage.ENGLISH
    override val usesShiftForCaps = true
    override val supportsPhoneticMode = false
    override val hasDedicatedHudhaaKey = false
    override val fullStopCharacter = "."

    override fun primaryQuickPunctuation(): List<String> = listOf(",", ".", "?", "!", "'", "-", ":")

    override fun baseRows(): List<List<String>> = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m"),
    )
}

object LanguageModuleRegistry {
    fun getModule(language: KeyboardLanguage): ImeLanguageModule = when (language) {
        KeyboardLanguage.AMHARIC -> AmharicLanguageModule
        KeyboardLanguage.TIGRIGNA -> TigrignaLanguageModule
        KeyboardLanguage.AFAAN_OROMO -> AfaanOromoLanguageModule
        KeyboardLanguage.ENGLISH -> EnglishLanguageModule
    }
}
