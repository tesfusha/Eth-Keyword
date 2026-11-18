package com.example

import com.example.data.KeyboardLanguage
import com.example.engine.AfaanOromoLanguageModule
import com.example.engine.AmharicLanguageModule
import com.example.engine.EmojiCatalog
import com.example.engine.EnglishLanguageModule
import com.example.engine.EthiopianDate
import com.example.engine.GeezFidelData
import com.example.engine.LanguageModuleRegistry
import com.example.engine.PhoneticTransliterator
import com.example.engine.TigrignaLanguageModule
import com.example.engine.TriePredictionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FynLegacyEngineTest {

    @Test
    fun `phonetic transliterator converts Amharic words accurately`() {
        val transliterator = PhoneticTransliterator(isTigrigna = false)
        assertEquals("ሰላም", transliterator.transliterate("selam"))
        assertEquals("ሸ", transliterator.transliterate("she"))
        assertEquals("ች", transliterator.transliterate("ch"))
        assertEquals("ኛ", transliterator.transliterate("gna"))
        assertEquals("ጽ", transliterator.transliterate("ts"))
    }

    @Test
    fun `phonetic transliterator converts Tigrigna qh series`() {
        val transliterator = PhoneticTransliterator(isTigrigna = true)
        assertEquals("ቐ", transliterator.transliterate("qhe"))
        assertEquals("ቕ", transliterator.transliterate("qh"))
    }

    @Test
    fun `ethiopian calendar converts Gregorian 2026-11-18 to Hidar 9 2019`() {
        val date = EthiopianDate.fromGregorian(LocalDate.of(2026, 11, 18))
        assertEquals(2019, date.year)
        assertEquals(3, date.month)
        assertEquals(9, date.day)
        assertEquals("ሕዳር 9, 2019", date.formatEthiopic())
        assertEquals("Hidar 9, 2019 E.C.", date.formatLatin())
    }

    @Test
    fun `ethiopian calendar handles Ethiopian New Year Meskerem 1`() {
        val newYear = EthiopianDate.fromGregorian(LocalDate.of(2026, 9, 11))
        assertEquals(2019, newYear.year)
        assertEquals(1, newYear.month)
        assertEquals(1, newYear.day)
        assertEquals("መስከረም 1, 2019", newYear.formatEthiopic())
    }

    @Test
    fun `trie prediction engine suggests words for all 4 languages`() {
        val engine = TriePredictionEngine()
        assertTrue(engine.getSyncSuggestions("ሰላ", KeyboardLanguage.AMHARIC).contains("ሰላም"))
        assertTrue(engine.getSyncSuggestions("የቐ", KeyboardLanguage.TIGRIGNA).contains("የቐንየለይ"))
        assertTrue(engine.getSyncSuggestions("gal", KeyboardLanguage.AFAAN_OROMO).contains("galatoomi"))
        assertTrue(engine.getSyncSuggestions("har", KeyboardLanguage.AFAAN_OROMO).contains("har'a"))
        assertTrue(engine.getSyncSuggestions("hel", KeyboardLanguage.ENGLISH).contains("hello"))
    }

    @Test
    fun `bilingual emoji catalog matches buna and Amharic queries`() {
        assertTrue(EmojiCatalog.search("ቡና").any { it.emoji == "☕" })
        assertTrue(EmojiCatalog.search("buna").any { it.emoji == "☕" })
        assertTrue(EmojiCatalog.search("jaalala").any { it.emoji == "❤️" })
    }

    @Test
    fun `language modules expose valid layouts and hudhaa key for Oromo`() {
        assertEquals(AmharicLanguageModule, LanguageModuleRegistry.getModule(KeyboardLanguage.AMHARIC))
        assertEquals(TigrignaLanguageModule, LanguageModuleRegistry.getModule(KeyboardLanguage.TIGRIGNA))
        assertEquals(AfaanOromoLanguageModule, LanguageModuleRegistry.getModule(KeyboardLanguage.AFAAN_OROMO))
        assertEquals(EnglishLanguageModule, LanguageModuleRegistry.getModule(KeyboardLanguage.ENGLISH))

        assertTrue(AfaanOromoLanguageModule.hasDedicatedHudhaaKey)
        assertTrue(AfaanOromoLanguageModule.baseRows()[1].contains("'"))
        assertEquals(11, GeezFidelData.simpleRow1Amharic.size)
        assertEquals(11, GeezFidelData.simpleRow2.size)
    }
}
