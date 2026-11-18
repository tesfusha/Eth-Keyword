package com.example.engine

data class FidelKeySpec(
    val baseChar: String,
    val orders: List<String>,
    val subKeyChar: String? = null,
)

object GeezFidelData {

    // Complete 7-order (+ labialized 8th order where standard) Ethiopic syllabary map
    val fidelOrdersMap: Map<String, List<String>> = mapOf(
        "ሀ" to listOf("ሀ", "ሁ", "ሂ", "ሃ", "ሄ", "ህ", "ሆ", "ኋ"),
        "ለ" to listOf("ለ", "ሉ", "ሊ", "ላ", "ሌ", "ል", "ሎ", "ሏ"),
        "ሐ" to listOf("ሐ", "ሑ", "ሒ", "ሓ", "ሔ", "ሕ", "ሖ", "ሗ"),
        "መ" to listOf("መ", "ሙ", "ሚ", "ማ", "ሜ", "ም", "ሞ", "ሟ"),
        "ሠ" to listOf("ሠ", "ሡ", "ሢ", "ሣ", "ሤ", "ሥ", "ሦ", "ሧ"),
        "ረ" to listOf("ረ", "ሩ", "ሪ", "ራ", "ሬ", "ር", "ሮ", "ሯ"),
        "ሰ" to listOf("ሰ", "ሱ", "ሲ", "ሳ", "ሴ", "ስ", "ሶ", "ሷ"),
        "ሸ" to listOf("ሸ", "ሹ", "ሺ", "ሻ", "ሼ", "ሽ", "ሾ", "ሿ"),
        "ቀ" to listOf("ቀ", "ቁ", "ቂ", "ቃ", "ቄ", "ቅ", "ቆ", "ቋ"),
        "ቐ" to listOf("ቐ", "ቑ", "ቒ", "ቓ", "ቔ", "ቕ", "ቖ", "ቛ"),
        "በ" to listOf("በ", "ቡ", "ቢ", "ባ", "ቤ", "ብ", "ቦ", "ቧ"),
        "ቨ" to listOf("ቨ", "ቩ", "ቪ", "ቫ", "ቬ", "ቭ", "ቮ", "ቯ"),
        "ተ" to listOf("ተ", "ቱ", "ቲ", "ታ", "ቴ", "ት", "ቶ", "ቷ"),
        "ቸ" to listOf("ቸ", "ቹ", "ቺ", "ቻ", "ቼ", "ች", "ቾ", "ቿ"),
        "ኀ" to listOf("ኀ", "ኁ", "ኂ", "ኃ", "ኄ", "ኅ", "ኆ", "ኋ"),
        "ነ" to listOf("ነ", "ኑ", "ኒ", "ና", "ኔ", "ን", "ኖ", "ኗ"),
        "ኘ" to listOf("ኘ", "ኙ", "ኚ", "ኛ", "ኜ", "ኝ", "ኞ", "ኟ"),
        "አ" to listOf("አ", "ኡ", "ኢ", "ኣ", "ኤ", "እ", "ኦ", "ኧ"),
        "ከ" to listOf("ከ", "ኩ", "ኪ", "ካ", "ኬ", "ክ", "ኮ", "ኳ"),
        "ኸ" to listOf("ኸ", "ኹ", "ኺ", "ኻ", "ኼ", "ኽ", "ኾ", "ዃ"),
        "ዀ" to listOf("ዀ", "ዂ", "ዃ", "ዄ", "ዅ"),
        "ወ" to listOf("ወ", "ዉ", "ዊ", "ዋ", "ዌ", "ው", "ዎ"),
        "ዐ" to listOf("ዐ", "ዑ", "ዒ", "ዓ", "ዔ", "ዕ", "ዖ"),
        "ዘ" to listOf("ዘ", "ዙ", "ዚ", "ዛ", "ዜ", "ዝ", "ዞ", "ዟ"),
        "ዠ" to listOf("ዠ", "ዡ", "ዢ", "ዣ", "ዤ", "ዥ", "ዦ", "ዧ"),
        "የ" to listOf("የ", "ዩ", "ዪ", "ያ", "ዬ", "ይ", "ዮ"),
        "ደ" to listOf("ደ", "ዱ", "ዲ", "ዳ", "ዴ", "ድ", "ዶ", "ዷ"),
        "ጀ" to listOf("ጀ", "ጁ", "ጂ", "ጃ", "ጄ", "ጅ", "ጆ", "ጇ"),
        "ገ" to listOf("ገ", "ጉ", "ጊ", "ጋ", "ጌ", "ግ", "ጎ", "ጓ"),
        "ጠ" to listOf("ጠ", "ጡ", "ጢ", "ጣ", "ጤ", "ጥ", "ጦ", "ጧ"),
        "ጨ" to listOf("ጨ", "ጩ", "ጪ", "ጫ", "ጬ", "ጭ", "ጮ", "ጯ"),
        "ጰ" to listOf("ጰ", "ጱ", "ጲ", "ጳ", "ጴ", "ጵ", "ጶ", "ጷ"),
        "ጸ" to listOf("ጸ", "ጹ", "ጺ", "ጻ", "ጼ", "ጽ", "ጾ", "ጿ"),
        "ፀ" to listOf("ፀ", "ፁ", "ፂ", "ፃ", "ፄ", "ፅ", "ፆ"),
        "ፈ" to listOf("ፈ", "ፉ", "ፊ", "ፋ", "ፌ", "ፍ", "ፎ", "ፏ"),
        "ፐ" to listOf("ፐ", "ፑ", "ፒ", "ፓ", "ፔ", "ፕ", "ፖ", "ፗ"),
    )

    // Classic FynGeez 11-key Row 1: ሀ ለ ሐ መ ሠ ረ ሰ ሸ ቀ በ ቨ
    val simpleRow1Amharic: List<FidelKeySpec> = listOf(
        key("ሀ", sub = "ኀ"),
        key("ለ"),
        key("ሐ"),
        key("መ"),
        key("ሠ"),
        key("ረ"),
        key("ሰ"),
        key("ሸ"),
        key("ቀ", sub = "ቐ"),
        key("በ"),
        key("ቨ"),
    )

    // Tigrigna Row 1 surfaces ቐ directly alongside ቀ
    val simpleRow1Tigrigna: List<FidelKeySpec> = listOf(
        key("ሀ", sub = "ኀ"),
        key("ለ"),
        key("ሐ"),
        key("መ"),
        key("ረ"),
        key("ሰ"),
        key("ሸ"),
        key("ቀ"),
        key("ቐ"),
        key("በ"),
        key("ቨ"),
    )

    // Classic FynGeez 11-key Row 2: ተ ቸ የ ነ ኘ አ ከ ኸ ወ ዘ ዠ
    val simpleRow2: List<FidelKeySpec> = listOf(
        key("ተ"),
        key("ቸ"),
        key("የ"),
        key("ነ"),
        key("ኘ"),
        key("አ", sub = "ዐ"),
        key("ከ"),
        key("ኸ", sub = "ዀ"),
        key("ወ"),
        key("ዘ"),
        key("ዠ"),
    )

    // Classic FynGeez Row 3 (8 base keys + Shift/Alt + Backspace): ደ ጀ ገ ጠ ጨ ጰ ጸ ፈ
    val simpleRow3: List<FidelKeySpec> = listOf(
        key("ደ"),
        key("ጀ"),
        key("ገ"),
        key("ጠ"),
        key("ጨ"),
        key("ጰ"),
        key("ጸ", sub = "ፀ"),
        key("ፈ", sub = "ፐ"),
    )

    // Alternate/Extra Fidel layer toggled by Shift (ዀ/ዐ/ፀ/ፐ/ኀ/ቐ/ሠ + labialized)
    val extraAltRow1: List<FidelKeySpec> = listOf(
        key("ኀ"),
        key("ዐ"),
        key("ዀ"),
        key("ፀ"),
        key("ፐ"),
        key("ቐ"),
        key("ሠ"),
        key("ጰ"),
        key("ቨ"),
        key("ሐ"),
        key("ኸ"),
    )

    // Ethiopic Punctuation Top Quick-Strip
    val ethiopicPunctuation: List<String> = listOf("፡", "።", "፣", "፤", "፥", "፦", "፧", "፨", "?")

    // Ge'ez Numerals Grid
    val geezNumeralsRow1: List<String> = listOf("፩", "፪", "፫", "፬", "፭", "፮", "፯", "፰", "፱", "፲")
    val geezNumeralsRow2: List<String> = listOf("፳", "፴", "፵", "፶", "፷", "፸", "፹", "፺", "፻", "፼")

    // Helper to look up all orders for any Ethiopic character
    fun ordersForChar(char: String): List<String>? {
        fidelOrdersMap[char]?.let { return it }
        return fidelOrdersMap.values.firstOrNull { char in it }
    }

    private fun key(base: String, sub: String? = null): FidelKeySpec =
        FidelKeySpec(
            baseChar = base,
            orders = fidelOrdersMap[base] ?: listOf(base),
            subKeyChar = sub,
        )
}
