package com.example.engine

/**
 * Real-time Latin phonetic -> Ge'ez Fidel transliterator for Amharic & Tigrigna.
 * Supports multi-character consonant clusters (sh, ch, gn/ny, zh, ts, ph, qh, kh)
 * and smooth vowel order transitions (6th Sadis default -> 1st..7th/8th orders).
 */
class PhoneticTransliterator(private val isTigrigna: Boolean = false) {

    private val buffer = StringBuilder()

    val currentPhoneticBuffer: String
        get() = buffer.toString()

    fun reset() {
        buffer.clear()
    }

    /**
     * Appends a Latin keystroke and returns the resulting Ethiopic composing text.
     */
    fun appendChar(ch: Char): String {
        buffer.append(ch)
        return transliterate(buffer.toString())
    }

    /**
     * Removes the last Latin keystroke and returns updated Ethiopic composing text.
     */
    fun backspace(): String {
        if (buffer.isNotEmpty()) {
            buffer.deleteCharAt(buffer.length - 1)
        }
        return transliterate(buffer.toString())
    }

    fun transliterate(input: String): String {
        if (input.isEmpty()) return ""
        val out = StringBuilder()
        var i = 0
        val len = input.length

        while (i < len) {
            // Standalone vowel without preceding consonant -> አ or ዐ series
            val standaloneVowelIdx = vowelOrder(input, i)
            if (standaloneVowelIdx != null) {
                val (orderIdx, consumed) = standaloneVowelIdx
                val base = if (input[i] == 'A' || input[i] == 'O') "ዐ" else "አ"
                val orders = GeezFidelData.fidelOrdersMap[base]!!
                out.append(orders.getOrElse(orderIdx) { orders[0] })
                i += consumed
                continue
            }

            // Match consonant (longest match first: 2-char clusters then 1-char)
            val consonantMatch = matchConsonant(input, i)
            if (consonantMatch != null) {
                val (baseFidel, consLen) = consonantMatch
                i += consLen
                val orders = GeezFidelData.fidelOrdersMap[baseFidel]

                if (orders == null) {
                    out.append(baseFidel)
                    continue
                }

                // Check for labialized 'u' + 'a' or 'w' + 'a' (8th order like ኋ, ሏ, ቋ)
                if (i + 1 < len && (input[i] == 'u' || input[i] == 'w') && input[i + 1] == 'a') {
                    out.append(orders.getOrElse(7) { orders[3] })
                    i += 2
                    continue
                }

                val vowelMatch = if (i < len) vowelOrder(input, i) else null
                if (vowelMatch != null) {
                    val (orderIdx, vLen) = vowelMatch
                    out.append(orders.getOrElse(orderIdx) { orders[5] })
                    i += vLen
                } else {
                    // Default consonant alone is 6th order (Sadis), e.g., s -> ስ, m -> ም
                    out.append(orders.getOrElse(5) { orders[0] })
                }
            } else {
                out.append(input[i])
                i++
            }
        }
        return out.toString()
    }

    private fun vowelOrder(input: String, index: Int): Pair<Int, Int>? {
        if (index >= input.length) return null
        val c = input[index]
        val next = if (index + 1 < input.length) input[index + 1] else null

        // Check two-char vowel digraphs first
        if (c == 'i' && next == 'e') return 4 to 2 // 5th order (Hamis: -ē)
        if (c == 'e' && next == 'e') return 4 to 2 // 5th order (-ee)
        if (c == 'a' && next == 'e') return 4 to 2

        return when (c) {
            'e', 'ä' -> 0 to 1 // 1st Ge'ez (ሰ)
            'u' -> 1 to 1      // 2nd Ka'ib (ሱ)
            'i' -> 2 to 1      // 3rd Salis (ሲ)
            'a' -> 3 to 1      // 4th Rabi (ሳ)
            'E', 'é' -> 4 to 1 // 5th Hamis (ሴ)
            'I' -> 5 to 1      // 6th Sadis explicit (ስ)
            'o', 'O' -> 6 to 1 // 7th Sabi (ሶ)
            'A' -> 3 to 1
            else -> null
        }
    }

    private fun matchConsonant(input: String, index: Int): Pair<String, Int>? {
        if (index + 1 < input.length) {
            val two = input.substring(index, index + 2)
            val twoMatch = when (two) {
                "sh", "Sh", "SH" -> "ሸ"
                "ch", "Ch" -> "ቸ"
                "CH" -> "ጨ"
                "gn", "ny", "Gn", "Ny" -> "ኘ"
                "zh", "Zh" -> "ዠ"
                "ts", "Ts", "tz", "Tz" -> "ጸ"
                "TS", "TZ" -> "ፀ"
                "ph", "Ph" -> "ጰ"
                "qh", "Qh", "qH" -> if (isTigrigna) "ቐ" else "ቀ"
                "kh", "Kh" -> "ኸ"
                "th", "Th" -> "ጠ"
                "hh", "Hh" -> "ሐ"
                "hH", "HH" -> "ኀ"
                else -> null
            }
            if (twoMatch != null) return twoMatch to 2
        }

        val c = input[index]
        val oneMatch = when (c) {
            'h' -> "ሀ"
            'H' -> "ሐ"
            'l', 'L' -> "ለ"
            'm', 'M' -> "መ"
            'r', 'R' -> "ረ"
            's' -> "ሰ"
            'S' -> "ሠ"
            'q' -> "ቀ"
            'Q' -> if (isTigrigna) "ቐ" else "ቀ"
            'b', 'B' -> "በ"
            'v', 'V' -> "ቨ"
            't' -> "ተ"
            'T' -> "ጠ"
            'c' -> "ቸ"
            'C' -> "ጨ"
            'n' -> "ነ"
            'N' -> "ኘ"
            'k' -> "ከ"
            'K' -> "ኸ"
            'w', 'W' -> "ወ"
            'z' -> "ዘ"
            'Z' -> "ዠ"
            'y', 'Y' -> "የ"
            'd', 'D' -> "ደ"
            'j', 'J' -> "ጀ"
            'g', 'G' -> "ገ"
            'p' -> "ፐ"
            'P' -> "ጰ"
            'x', 'X' -> "ጸ"
            'f', 'F' -> "ፈ"
            else -> null
        }
        return oneMatch?.let { it to 1 }
    }
}
