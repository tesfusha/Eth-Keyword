package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LearnedWordDao {

    @Query("SELECT * FROM learned_words ORDER BY frequency DESC, lastUsedAt DESC")
    fun observeAllLearnedWords(): Flow<List<LearnedWord>>

    @Query("SELECT COUNT(*) FROM learned_words WHERE languageCode = :languageCode")
    fun observeCountForLanguage(languageCode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM learned_words")
    fun observeTotalCount(): Flow<Int>

    @Query(
        "SELECT * FROM learned_words " +
            "WHERE word LIKE :prefix || '%' " +
            "ORDER BY CASE WHEN languageCode = :languageCode THEN 0 ELSE 1 END, frequency DESC, lastUsedAt DESC " +
            "LIMIT :limit"
    )
    suspend fun findSuggestions(prefix: String, languageCode: String, limit: Int = 8): List<LearnedWord>

    @Query("SELECT * FROM learned_words WHERE word = :word AND languageCode = :languageCode LIMIT 1")
    suspend fun findExact(word: String, languageCode: String): LearnedWord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: LearnedWord)

    @Query("DELETE FROM learned_words")
    suspend fun clearAll()
}
