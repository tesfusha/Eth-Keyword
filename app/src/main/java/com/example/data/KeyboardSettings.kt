package com.example.data

enum class KeyboardLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val shortBadge: String,
    val isEthiopic: Boolean,
    val fullStopChar: String,
    val builtInWordCount: Int,
) {
    AMHARIC(
        code = "am",
        nativeName = "አማርኛ",
        englishName = "Amharic",
        shortBadge = "አማ",
        isEthiopic = true,
        fullStopChar = "።",
        builtInWordCount = 160,
    ),
    TIGRIGNA(
        code = "ti",
        nativeName = "ትግርኛ",
        englishName = "Tigrigna",
        shortBadge = "ትግ",
        isEthiopic = true,
        fullStopChar = "።",
        builtInWordCount = 110,
    ),
    AFAAN_OROMO(
        code = "om",
        nativeName = "Afaan Oromoo",
        englishName = "Afaan Oromo (Qubee)",
        shortBadge = "ORO",
        isEthiopic = false,
        fullStopChar = ".",
        builtInWordCount = 145,
    ),
    ENGLISH(
        code = "en",
        nativeName = "English",
        englishName = "English",
        shortBadge = "ENG",
        isEthiopic = false,
        fullStopChar = ".",
        builtInWordCount = 210,
    );

    companion object {
        fun fromCode(code: String?): KeyboardLanguage =
            entries.firstOrNull { it.code == code } ?: AMHARIC
    }
}

enum class KeyboardThemePreset(
    val id: String,
    val displayName: String,
    val subtitleAmharic: String,
    val description: String,
) {
    CLASSIC_FYNGEEZ(
        id = "classic_fyngeez",
        displayName = "Classic FynGeez Dark Grey",
        subtitleAmharic = "ክላሲክ ፋይንግዕዝ",
        description = "Charcoal keycaps with warm #F59E0B amber accents honoring Yohannes Ejigu's original UI.",
    ),
    MATERIAL_YOU(
        id = "material_you",
        displayName = "Material You (Dynamic)",
        subtitleAmharic = "ማቴሪያል ዩ",
        description = "Adapts automatically to your Android wallpaper palette on Android 12+.",
    ),
    PURE_AMOLED(
        id = "pure_amoled",
        displayName = "Pure AMOLED Black",
        subtitleAmharic = "ጥቁር አሞሌድ",
        description = "True #000000 pitch-black canvas crafted for OLED battery efficiency.",
    ),
    WARM_PAPER(
        id = "warm_paper",
        displayName = "Warm Paper Light",
        subtitleAmharic = "ብራና ነጭ",
        description = "Parchment daylight surface inspired by traditional Ge'ez manuscripts.",
    ),
    ETHIOPIAN_ACCENT(
        id = "ethiopian_accent",
        displayName = "Ethiopian Accent",
        subtitleAmharic = "ኢትዮጵያዊ ቀለም",
        description = "Deep obsidian keys with emerald, amber gold, and crimson heritage highlights.",
    );

    companion object {
        fun fromId(id: String?): KeyboardThemePreset =
            entries.firstOrNull { it.id == id } ?: CLASSIC_FYNGEEZ
    }
}

enum class GeezLayoutMode(
    val id: String,
    val displayName: String,
    val description: String,
) {
    SIMPLE_HAGIZ(
        id = "simple_hagiz",
        displayName = "Simple - ሀግዝ",
        description = "Iconic 11-key FynGeez base grid with 7-order floating selector.",
    ),
    PHONETIC(
        id = "phonetic",
        displayName = "Phonetic - ፎነቲክ",
        description = "Type Latin phonetics (se → ሰ, sh → ሽ, gn → ኝ) with live Ge'ez conversion.",
    ),
    CLASSIC_FYNGEEZ(
        id = "classic_fyngeez",
        displayName = "Classic FynGeez",
        description = "Traditional FynGeez layout with dedicated extra Fidel shift & direct punctuation bar.",
    );

    companion object {
        fun fromId(id: String?): GeezLayoutMode =
            entries.firstOrNull { it.id == id } ?: SIMPLE_HAGIZ
    }
}

enum class OneHandedMode(val id: String) {
    NONE("none"),
    LEFT("left"),
    RIGHT("right");

    companion object {
        fun fromId(id: String?): OneHandedMode =
            entries.firstOrNull { it.id == id } ?: NONE
    }
}

data class KeyboardSettings(
    // Look & Behavior (መልክ እና ባህሪ)
    val boldFidelFont: Boolean = true,
    val keyboardHeightDp: Float = 264f,
    val showSubKeys: Boolean = true,
    val showNumberRow: Boolean = false,
    val showKeyPreview: Boolean = true,
    val showSuggestionsAtBottom: Boolean = false,
    val lowLatencyMode: Boolean = true,

    // Sound & Vibration (ድምጽ እና ንዝረት)
    val soundOnKeyPress: Boolean = false,
    val vibrateOnKeyPress: Boolean = true,
    val vibrationStrength: Int = 0, // 0 = device default, 1..100 custom ms/amplitude

    // Word Prediction (የቃላት ትንበያ)
    val showSuggestions: Boolean = true,
    val doubleSpaceFullStop: Boolean = true,
    val autoCapitalization: Boolean = true,
    val swapSpaceAndPunctuation: Boolean = false,
    val autoPickOnSpace: Boolean = false,
    val autoSpaceOnHandwriting: Boolean = true,

    // Ge'ez Layout (ግዕዝ)
    val geezLayoutMode: GeezLayoutMode = GeezLayoutMode.SIMPLE_HAGIZ,

    // Theme & Active Language
    val themePreset: KeyboardThemePreset = KeyboardThemePreset.CLASSIC_FYNGEEZ,
    val activeLanguage: KeyboardLanguage = KeyboardLanguage.AMHARIC,
    val oneHandedMode: OneHandedMode = OneHandedMode.NONE,

    // Enabled Languages
    val enableEnglish: Boolean = true,
    val enableAmharic: Boolean = true,
    val enableAfaanOromo: Boolean = true,
    val enableTigrigna: Boolean = true,
) {
    fun enabledLanguages(): List<KeyboardLanguage> {
        val list = buildList {
            if (enableAmharic) add(KeyboardLanguage.AMHARIC)
            if (enableTigrigna) add(KeyboardLanguage.TIGRIGNA)
            if (enableAfaanOromo) add(KeyboardLanguage.AFAAN_OROMO)
            if (enableEnglish) add(KeyboardLanguage.ENGLISH)
        }
        return list.ifEmpty { listOf(KeyboardLanguage.ENGLISH) }
    }

    fun nextEnabledLanguage(): KeyboardLanguage {
        val enabled = enabledLanguages()
        val index = enabled.indexOf(activeLanguage)
        return if (index == -1) enabled.first() else enabled[(index + 1) % enabled.size]
    }

    fun previousEnabledLanguage(): KeyboardLanguage {
        val enabled = enabledLanguages()
        val index = enabled.indexOf(activeLanguage)
        return if (index == -1) enabled.first() else enabled[(index - 1 + enabled.size) % enabled.size]
    }
}
