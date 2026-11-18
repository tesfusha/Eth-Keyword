package com.example.engine

data class EmojiEntry(
    val emoji: String,
    val keywords: List<String>,
    val category: String,
)

object EmojiCatalog {
    val entries: List<EmojiEntry> = listOf(
        // Ethiopian Culture & Food
        EmojiEntry("☕", listOf("coffee", "buna", "bun", "ቡና", "ቡን", "jabaanaa"), "Culture"),
        EmojiEntry("🇪🇹", listOf("ethiopia", "ኢትዮጵያ", "oromiyaa", "addis", "finfinnee", "flag"), "Culture"),
        EmojiEntry("🫓", listOf("injera", "enjera", "እንጀራ", "buddeena", "bread"), "Culture"),
        EmojiEntry("🍲", listOf("wot", "stew", "food", "ምግብ", "መግቢ", "nyaata", "doro"), "Culture"),
        EmojiEntry("🍯", listOf("tej", "honey", "ማር", "damma", "መዓር"), "Culture"),
        EmojiEntry("🌾", listOf("teff", "harvest", "ጤፍ", "xaafii", "grain"), "Culture"),
        EmojiEntry("🦁", listOf("lion", "anbessa", "አንበሳ", "leenca", "ambesa"), "Culture"),
        EmojiEntry("⛪", listOf("church", "lalibela", "ቤተክርስቲያን", "meskel"), "Culture"),
        EmojiEntry("🕌", listOf("mosque", "harar", "መስጊድ", "masjida"), "Culture"),
        EmojiEntry("🎶", listOf("music", "song", "ሙዚቃ", "ዘፈን", "muuziqaa", "kirar"), "Culture"),

        // Expressions & Greetings
        EmojiEntry("😊", listOf("smile", "happy", "ደስ", "ሰላም", "selam", "akkam", "gammachuu"), "Faces"),
        EmojiEntry("😂", listOf("laugh", "lol", "ሳቅ", "kolfuu", "funny"), "Faces"),
        EmojiEntry("❤️", listOf("love", "heart", "ፍቅር", "ፍቕሪ", "jaalala", "fikir"), "Faces"),
        EmojiEntry("🙏", listOf("please", "thanks", "thank", "እባክዎ", "አመሰግናለሁ", "የቐንየለይ", "galatoomi", "maaloo"), "Faces"),
        EmojiEntry("👍", listOf("ok", "good", "yes", "እሺ", "ጥሩ", "ጽቡቕ", "tole", "gaarii", "eeyyee"), "Faces"),
        EmojiEntry("👏", listOf("clap", "bravo", "ጭብጨባ", "congrats"), "Faces"),
        EmojiEntry("🎉", listOf("celebrate", "party", "በዓል", "ayyaana", "happy"), "Faces"),
        EmojiEntry("✨", listOf("sparkle", "star", "ብርሃን", "ifaa", "shine"), "Faces"),
        EmojiEntry("🔥", listOf("fire", "lit", "እሳት", "ibidda", "amazing"), "Faces"),
        EmojiEntry("🤝", listOf("handshake", "deal", "peace", "ሰላምታ", "nagaa", "agree"), "Faces"),
        EmojiEntry("🤗", listOf("hug", "warm", "እቅፍ", "hammachuu"), "Faces"),
        EmojiEntry("😍", listOf("heart eyes", "adore", "ውብ", "bareedaa"), "Faces"),
        EmojiEntry("🥳", listOf("celebration", "holiday", "ደስታ", "birthday"), "Faces"),
        EmojiEntry("😢", listOf("sad", "cry", "ለቅሶ", "boo'icha", "sorry"), "Faces"),

        // Everyday & Tech
        EmojiEntry("☀️", listOf("sun", "morning", "ፀሐይ", "ጸሓይ", "ጠዋት", "ganama", "aduu"), "Everyday"),
        EmojiEntry("🌙", listOf("moon", "night", "ጨረቃ", "ሌሊት", "halkan", "ji'a"), "Everyday"),
        EmojiEntry("🌧️", listOf("rain", "zemb", "ዝናብ", "rooba", "kiremt"), "Everyday"),
        EmojiEntry("🌹", listOf("flower", "rose", "አበባ", "ababa", "daraaraa"), "Everyday"),
        EmojiEntry("📱", listOf("phone", "call", "ስልክ", "ስልኪ", "bilbila"), "Everyday"),
        EmojiEntry("💬", listOf("chat", "message", "መልዕክት", "ergaa"), "Everyday"),
        EmojiEntry("💰", listOf("money", "birr", "ብር", "ገንዘብ", "telebirr", "cbe", "qarshii"), "Everyday"),
        EmojiEntry("📅", listOf("calendar", "date", "ቀን", "guyyaa", "ዛሬ", "har'a"), "Everyday"),
        EmojiEntry("⏰", listOf("time", "clock", "ሰዓት", "sa'aatii"), "Everyday"),
        EmojiEntry("📚", listOf("book", "study", "መጽሐፍ", "kitaaba", "ትምህርት"), "Everyday"),
        EmojiEntry("✈️", listOf("travel", "flight", "ጉዞ", "imala"), "Everyday"),
        EmojiEntry("🏠", listOf("home", "house", "ቤት", "ገዛ", "mana"), "Everyday"),
    )

    fun search(query: String): List<EmojiEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return entries
        return entries.filter { entry ->
            entry.keywords.any { it.lowercase().contains(q) } ||
                entry.category.lowercase().contains(q)
        }
    }
}
