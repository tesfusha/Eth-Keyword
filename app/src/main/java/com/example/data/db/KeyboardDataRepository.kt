package com.example.data.db

import android.content.Context
import kotlinx.coroutines.flow.Flow

class KeyboardDataRepository private constructor(
    private val clipboardDao: ClipboardDao,
    private val learnedWordDao: LearnedWordDao,
) {
    val allClipboardEntries: Flow<List<ClipboardEntry>> = clipboardDao.observeAllEntries()
    val pinnedSnippets: Flow<List<ClipboardEntry>> = clipboardDao.observePinnedSnippets()
    val totalLearnedWordCount: Flow<Int> = learnedWordDao.observeTotalCount()

    fun observeLearnedCount(languageCode: String): Flow<Int> =
        learnedWordDao.observeCountForLanguage(languageCode)

    suspend fun saveClipboardText(text: String, isPinned: Boolean = false, label: String? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val existing = clipboardDao.findByText(trimmed)
        if (existing != null) {
            clipboardDao.insert(
                existing.copy(
                    isPinned = existing.isPinned || isPinned,
                    label = label ?: existing.label,
                    createdAt = System.currentTimeMillis(),
                )
            )
        } else {
            clipboardDao.insert(
                ClipboardEntry(
                    text = trimmed,
                    isPinned = isPinned,
                    label = label,
                )
            )
        }
    }

    suspend fun togglePin(entry: ClipboardEntry, customLabel: String? = entry.label) {
        clipboardDao.updatePinState(
            id = entry.id,
            pinned = !entry.isPinned,
            label = if (!entry.isPinned) customLabel else entry.label,
        )
    }

    suspend fun deleteClipboardEntry(id: Long) = clipboardDao.deleteById(id)

    suspend fun clearUnpinnedClipboard() = clipboardDao.clearUnpinned()

    suspend fun recordLearnedWord(word: String, languageCode: String) {
        val cleaned = word.trim()
        if (cleaned.length < 2) return
        val existing = learnedWordDao.findExact(cleaned, languageCode)
        if (existing != null) {
            learnedWordDao.insert(
                existing.copy(
                    frequency = existing.frequency + 1,
                    lastUsedAt = System.currentTimeMillis(),
                )
            )
        } else {
            learnedWordDao.insert(
                LearnedWord(
                    word = cleaned,
                    languageCode = languageCode,
                    frequency = 1,
                )
            )
        }
    }

    suspend fun findLearnedSuggestions(prefix: String, languageCode: String): List<LearnedWord> {
        if (prefix.isBlank()) return emptyList()
        return learnedWordDao.findSuggestions(prefix, languageCode)
    }

    suspend fun clearLearnedWords() = learnedWordDao.clearAll()

    companion object {
        @Volatile
        private var instance: KeyboardDataRepository? = null

        fun getInstance(context: Context): KeyboardDataRepository =
            instance ?: synchronized(this) {
                val db = FynLegacyDatabase.getInstance(context)
                instance ?: KeyboardDataRepository(
                    clipboardDao = db.clipboardDao(),
                    learnedWordDao = db.learnedWordDao(),
                ).also { instance = it }
            }
    }
}
