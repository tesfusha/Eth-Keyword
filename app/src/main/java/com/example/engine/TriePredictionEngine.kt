package com.example.engine

import com.example.data.KeyboardLanguage
import com.example.data.db.KeyboardDataRepository

class TrieNode {
    val children: MutableMap<Char, TrieNode> = HashMap()
    var isWord: Boolean = false
    var frequency: Int = 0
}

class PrefixTrie {
    private val root = TrieNode()

    fun insert(word: String, frequency: Int = 10) {
        var node = root
        for (ch in word) {
            node = node.children.getOrPut(ch) { TrieNode() }
        }
        node.isWord = true
        if (frequency > node.frequency) {
            node.frequency = frequency
        }
    }

    fun searchPrefix(prefix: String, limit: Int = 6): List<Pair<String, Int>> {
        if (prefix.isEmpty()) return emptyList()
        var node = root
        for (ch in prefix) {
            node = node.children[ch] ?: return emptyList()
        }
        val results = mutableListOf<Pair<String, Int>>()
        collectWords(node, StringBuilder(prefix), results)
        return results.sortedByDescending { it.second }.take(limit)
    }

    private fun collectWords(
        node: TrieNode,
        current: StringBuilder,
        out: MutableList<Pair<String, Int>>,
    ) {
        if (out.size >= 40) return
        if (node.isWord) {
            out.add(current.toString() to node.frequency)
        }
        for ((ch, child) in node.children) {
            current.append(ch)
            collectWords(child, current, out)
            current.deleteCharAt(current.length - 1)
        }
    }
}

private object SharedTrieCache {
    val tries: Map<KeyboardLanguage, PrefixTrie> by lazy {
        mapOf(
            KeyboardLanguage.AMHARIC to buildTrie(OfflineDictionaries.amharicWords),
            KeyboardLanguage.TIGRIGNA to buildTrie(OfflineDictionaries.tigrignaWords),
            KeyboardLanguage.AFAAN_OROMO to buildTrie(OfflineDictionaries.afaanOromoWords),
            KeyboardLanguage.ENGLISH to buildTrie(OfflineDictionaries.englishWords),
        )
    }

    private fun buildTrie(words: List<Pair<String, Int>>): PrefixTrie =
        PrefixTrie().apply {
            for ((word, freq) in words) {
                insert(word, freq)
                val lower = word.lowercase()
                if (lower != word) insert(lower, freq - 1)
            }
        }
}

class TriePredictionEngine(
    private val dataRepository: KeyboardDataRepository? = null,
) {
    private val tries: Map<KeyboardLanguage, PrefixTrie>
        get() = SharedTrieCache.tries

    suspend fun getSuggestions(
        prefix: String,
        language: KeyboardLanguage,
        limit: Int = 5,
    ): List<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        val trimmed = prefix.trim()
        if (trimmed.isEmpty()) {
            return@withContext defaultQuickWords(language).take(limit)
        }

        val combined = LinkedHashMap<String, Int>()

        // 1. Personal & bilingual mixed-script learned words from Room
        if (dataRepository != null) {
            val learned = dataRepository.findLearnedSuggestions(trimmed, language.code)
            for (entry in learned) {
                val boost = if (entry.languageCode == language.code) 250 else 140
                combined[entry.word] = entry.frequency * 25 + boost
            }
        }

        // 2. Primary language Trie lookup
        val primaryTrie = tries[language]
        if (primaryTrie != null) {
            for ((word, freq) in primaryTrie.searchPrefix(trimmed, limit * 2)) {
                combined.merge(word, freq) { old, new -> maxOf(old, new) }
            }
            val lower = trimmed.lowercase()
            if (lower != trimmed) {
                for ((word, freq) in primaryTrie.searchPrefix(lower, limit)) {
                    val cased = if (trimmed.firstOrNull()?.isUpperCase() == true) {
                        word.replaceFirstChar { it.uppercase() }
                    } else {
                        word
                    }
                    combined.merge(cased, freq) { old, new -> maxOf(old, new) }
                }
            }
        }

        // 3. Bilingual mixed-script fallback: if user types Latin inside Amharic/Tigrigna, suggest English tech/chat terms
        if (language.isEthiopic && trimmed.firstOrNull()?.let { it in 'A'..'z' } == true) {
            val englishTrie = tries[KeyboardLanguage.ENGLISH]
            englishTrie?.searchPrefix(trimmed.lowercase(), limit)?.forEach { (word, freq) ->
                combined.merge(word, freq / 2) { old, new -> maxOf(old, new) }
            }
        }

        combined.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(limit)
    }

    fun getSyncSuggestions(
        prefix: String,
        language: KeyboardLanguage,
        limit: Int = 5,
    ): List<String> {
        val trimmed = prefix.trim()
        if (trimmed.isEmpty()) return defaultQuickWords(language).take(limit)
        val trie = tries[language] ?: return emptyList()
        val matches = trie.searchPrefix(trimmed, limit)
            .ifEmpty { trie.searchPrefix(trimmed.lowercase(), limit) }
        return matches.map { it.first }.take(limit)
    }

    private fun defaultQuickWords(language: KeyboardLanguage): List<String> = when (language) {
        KeyboardLanguage.AMHARIC -> listOf("ሰላም", "እንዴት", "እሺ", "አመሰግናለሁ", "ጥሩ")
        KeyboardLanguage.TIGRIGNA -> listOf("ሰላም", "ከመይ", "የቐንየለይ", "ጽቡቕ", "እወ")
        KeyboardLanguage.AFAAN_OROMO -> listOf("Akkam", "Galatoomi", "Nagaadha", "Tole", "Har'a")
        KeyboardLanguage.ENGLISH -> listOf("Hello", "Thanks", "Please", "Okay", "Morning")
    }
}

object OfflineDictionaries {
    val amharicWords: List<Pair<String, Int>> = listOf(
        "ሰላም" to 120, "እንዴት" to 115, "ነህ" to 105, "ነሽ" to 104, "ናችሁ" to 95,
        "ደህና" to 110, "ነኝ" to 108, "አመሰግናለሁ" to 112, "እሺ" to 118, "በጣም" to 109,
        "ጥሩ" to 107, "ኢትዮጵያ" to 106, "አዲስ" to 102, "አበባ" to 101, "ቡና" to 100,
        "ዛሬ" to 99, "ነገ" to 98, "ትላንት" to 94, "አሁን" to 97, "ቤት" to 96,
        "ስራ" to 95, "ትምህርት" to 90, "ፍቅር" to 93, "ሰዓት" to 92, "ብር" to 96,
        "ቴሌብር" to 88, "ባንክ" to 89, "ገንዘብ" to 86, "ውሃ" to 85, "ምግብ" to 87,
        "እንጀራ" to 84, "ጓደኛ" to 86, "ቤተሰብ" to 88, "እናት" to 85, "አባት" to 84,
        "ወንድም" to 82, "እህት" to 82, "ልጅ" to 83, "ሀገር" to 86, "ህዝብ" to 80,
        "መልካም" to 95, "ቀን" to 94, "ሌሊት" to 78, "ጠዋት" to 89, "ማታ" to 88,
        "ምሽት" to 83, "ሳምንት" to 80, "ወር" to 81, "ዓመት" to 84, "ሕዳር" to 79,
        "መስከረም" to 78, "ጥቅምት" to 76, "ታኅሣሥ" to 74, "ጥር" to 74, "የካቲት" to 74,
        "መጋቢት" to 74, "ሚያዝያ" to 74, "ግንቦት" to 74, "ሰኔ" to 74, "ሐምሌ" to 74,
        "ነሐሴ" to 74, "ጳጉሜ" to 70, "እባክዎ" to 86, "ይቅርታ" to 91, "ችግር" to 85,
        "የለም" to 94, "አለ" to 95, "ነው" to 108, "ናት" to 92, "ናቸው" to 90,
        "ሆነ" to 80, "መጣ" to 83, "ሄደ" to 82, "ሰጠ" to 79, "ወሰደ" to 76,
        "አየ" to 78, "ሰማ" to 77, "ተናገረ" to 75, "ጻፈ" to 76, "አነበበ" to 75,
        "ስልክ" to 89, "መልዕክት" to 86, "ቁጥር" to 87, "ስም" to 88, "ቦታ" to 84,
        "መንገድ" to 82, "ከተማ" to 81, "መኪና" to 80, "አውቶቡስ" to 72, "ባቡር" to 70,
        "ሆስፒታል" to 73, "ዩኒቨርሲቲ" to 75, "ኮምፒውተር" to 74, "ኪቦርድ" to 76,
        "ፊደል" to 88, "ግዕዝ" to 86, "አማርኛ" to 95, "ኦሮምኛ" to 82, "ትግርኛ" to 82,
        "እንግሊዝኛ" to 79, "ታሪክ" to 78, "ሙዚቃ" to 77, "ዘፈን" to 75, "በዓል" to 79,
        "ገና" to 74, "ፋሲካ" to 73, "መስቀል" to 74, "እንቁጣጣሽ" to 73, "ጥምቀት" to 74,
        "ሰላምታ" to 81, "ምስጋና" to 80, "ተስፋ" to 82, "እምነት" to 79, "ብርሃን" to 78,
        "ውበት" to 76, "ድምጽ" to 77, "ቀለም" to 75, "መጽሐፍ" to 80, "ወረቀት" to 74,
        "ብዕር" to 73, "ትክክል" to 85, "ሀሳብ" to 84, "ውሳኔ" to 77, "ፕሮጀክት" to 76,
        "መረጃ" to 83, "ግላዊነት" to 79, "ደህንነት" to 80, "ፍጥነት" to 78,
    )

    val tigrignaWords: List<Pair<String, Int>> = listOf(
        "ሰላም" to 120, "ከመይ" to 116, "ኣለኻ" to 112, "ኣለኺ" to 111, "ኣለኹም" to 104,
        "የቐንየለይ" to 115, "ጽቡቕ" to 114, "ትግርኛ" to 110, "መቐለ" to 102, "ኣስመራ" to 101,
        "ሎሚ" to 106, "ጽባሕ" to 100, "ትማሊ" to 98, "ሕጂ" to 103, "ገዛ" to 99,
        "ስራሕ" to 100, "ቡን" to 99, "ማይ" to 96, "መግቢ" to 95, "እንጀራ" to 90,
        "ሓደ" to 97, "ክልተ" to 95, "ሰለስተ" to 93, "ኣርባዕተ" to 90, "ሓሙሽተ" to 89,
        "ቐዳም" to 92, "ቕነ" to 91, "ቍጽሪ" to 94, "ብሓቂ" to 96, "እወ" to 108,
        "ኣይፋል" to 97, "ደሓን" to 105, "ብጣዕሚ" to 102, "ፍቕሪ" to 95, "ሰዓት" to 93,
        "ብር" to 92, "ባንኪ" to 86, "ስልኪ" to 91, "መልእኽቲ" to 89, "ስም" to 90,
        "ቦታ" to 87, "ከተማ" to 86, "ዓዲ" to 94, "ሃገር" to 93, "ህዝቢ" to 88,
        "ወዲ" to 91, "ጓል" to 91, "ኣቦ" to 92, "ኣደ" to 93, "ሓው" to 89,
        "ሓፍቲ" to 89, "ዓርኪ" to 88, "ስድራ" to 90, "ትምህርቲ" to 92, "መጽሓፍ" to 87,
        "ፊደል" to 90, "ግእዝ" to 88, "ቐሊል" to 89, "ቕዱስ" to 84, "ቐዳማይ" to 86,
        "ቕድሚ" to 87, "ቍርሲ" to 85, "ምሳሕ" to 85, "ድራር" to 85, "ንግሆ" to 88,
        "ምሸት" to 87, "ለይቲ" to 84, "ሰሙን" to 83, "ወርሒ" to 84, "ዓመት" to 86,
        "ተስፋ" to 85, "ሓጎስ" to 88, "ብርሃን" to 84, "ጸሓይ" to 83, "ወርቂ" to 82,
        "መንገዲ" to 84, "ኣየር" to 80, "ዝናብ" to 81, "ሓበሬታ" to 83, "ምኽሪ" to 80,
        "ውሳነ" to 79, "ድምጺ" to 81, "ቋንቋ" to 89, "ባህሊ" to 85, "ታሪኽ" to 86,
        "ምስጋና" to 88, "ይቕሬታ" to 91, "ጸገም" to 87, "የለን" to 93, "ኣሎ" to 96,
    )

    val afaanOromoWords: List<Pair<String, Int>> = listOf(
        "akkam" to 120, "nagaadha" to 116, "galatoomi" to 118, "Oromiyaa" to 110, "Finfinnee" to 108,
        "har'a" to 115, "ta'e" to 112, "fedhii" to 106, "buna" to 105, "gaarii" to 114,
        "tole" to 113, "maaloo" to 109, "maaliif" to 101, "yoom" to 100, "eessa" to 102,
        "hojiin" to 99, "barnoota" to 97, "jaalala" to 104, "bishaan" to 98, "nyaata" to 96,
        "guyyaa" to 103, "halkan" to 94, "obboleessa" to 92, "obboleettii" to 92, "hiriyyaa" to 95,
        "dhugaa" to 103, "nyachuu" to 91, "phaphaasii" to 82, "shanan" to 86, "Waaqayyo" to 100,
        "akka" to 110, "kan" to 111, "fi" to 115, "yoo" to 104, "garuu" to 103,
        "kun" to 102, "sun" to 99, "ana" to 101, "ati" to 103, "inni" to 100,
        "isheen" to 98, "nuyi" to 95, "isini" to 94, "isaan" to 96, "keenya" to 97,
        "keessan" to 94, "keessa" to 98, "irra" to 99, "jala" to 93, "bira" to 95,
        "dura" to 96, "booda" to 97, "ammayyuu" to 89, "amma" to 105, "boru" to 102,
        "kaleessa" to 97, "ganama" to 98, "galgala" to 96, "waaree" to 90, "torban" to 91,
        "ji'a" to 93, "waggaa" to 95, "bara" to 96, "sa'aatii" to 94, "lakkoofsa" to 91,
        "bilbila" to 95, "ergaa" to 94, "maqaa" to 97, "iddoo" to 93, "magaalaa" to 94,
        "biyya" to 98, "ummata" to 92, "haadha" to 93, "abbaa" to 94, "maatii" to 95,
        "ilma" to 89, "intala" to 89, "mana" to 98, "karaa" to 94, "konkolaataa" to 87,
        "kitaaba" to 91, "qubee" to 101, "afaan" to 105, "Oromoo" to 107, "aadaa" to 94,
        "seenaa" to 93, "abdii" to 95, "gammachuu" to 94, "nagaa" to 104, "milkii" to 92,
        "carraa" to 90, "jabaa" to 91, "cimaa" to 90, "bareedaa" to 92, "mi'aawaa" to 88,
        "du'a" to 85, "jireenya" to 93, "qalbii" to 90, "yaada" to 94, "murtoo" to 89,
        "danda'a" to 96, "barbaada" to 97, "beekaa" to 92, "hubannoo" to 90, "walitti" to 91,
        "fakkaata" to 88, "baay'ee" to 104, "guddaa" to 96, "xiqqaa" to 91, "haaraa" to 94,
        "moofaa" to 85, "dhufuu" to 91, "deemuu" to 92, "hojjechuu" to 93, "dubbisuu" to 90,
        "barreessuu" to 92, "dubbachuu" to 91, "dhaga'uu" to 90, "arguuf" to 88, "kennuu" to 89,
        "fudhachuu" to 88, "ta'uu" to 95, "jira" to 104, "hin" to 112, "miti" to 99,
        "eeyyee" to 103, "lakki" to 98, "dhiifama" to 96, "rakkoo" to 94, "furmaata" to 91,
    )

    val englishWords: List<Pair<String, Int>> = listOf(
        "hello" to 120, "thanks" to 118, "thank" to 116, "please" to 115, "morning" to 110,
        "good" to 117, "great" to 112, "okay" to 119, "yes" to 115, "no" to 114,
        "tomorrow" to 106, "today" to 109, "yesterday" to 101, "now" to 110, "later" to 104,
        "meeting" to 102, "message" to 105, "call" to 106, "phone" to 107, "email" to 108,
        "address" to 101, "account" to 100, "bank" to 98, "telebirr" to 99, "cbe" to 99,
        "ethiopia" to 108, "addis" to 104, "ababa" to 103, "coffee" to 105, "buna" to 100,
        "injera" to 95, "keyboard" to 104, "fynlegacy" to 102, "fyngeez" to 100, "amharic" to 102,
        "tigrigna" to 98, "oromo" to 99, "offline" to 97, "privacy" to 98, "friend" to 101,
        "family" to 103, "home" to 105, "work" to 104, "school" to 99, "time" to 108,
        "love" to 107, "peace" to 103, "happy" to 102, "welcome" to 101, "sorry" to 104,
        "where" to 103, "when" to 103, "what" to 108, "which" to 99, "who" to 100,
        "how" to 107, "why" to 102, "because" to 100, "about" to 101, "after" to 98,
        "before" to 97, "always" to 96, "never" to 95, "maybe" to 98, "sure" to 103,
        "really" to 100, "very" to 102, "much" to 99, "more" to 100, "most" to 94,
        "some" to 97, "many" to 96, "other" to 95, "another" to 93, "every" to 94,
        "people" to 97, "person" to 92, "place" to 95, "thing" to 96, "world" to 94,
        "android" to 98, "app" to 100, "send" to 101, "check" to 99, "wait" to 98,
        "come" to 100, "going" to 102, "coming" to 99, "done" to 101, "ready" to 99,
        "nice" to 100, "awesome" to 96, "amazing" to 95, "bless" to 93, "congratulations" to 91,
        "birthday" to 93, "weekend" to 92, "project" to 95, "update" to 96, "number" to 97,
    )
}
