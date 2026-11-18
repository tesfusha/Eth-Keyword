package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learned_words",
    indices = [Index(value = ["word", "languageCode"], unique = true)]
)
data class LearnedWord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val languageCode: String,
    val frequency: Int = 1,
    val lastUsedAt: Long = System.currentTimeMillis(),
)
