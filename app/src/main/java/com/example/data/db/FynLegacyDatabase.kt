package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ClipboardEntry::class, LearnedWord::class],
    version = 1,
    exportSchema = false,
)
abstract class FynLegacyDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
    abstract fun learnedWordDao(): LearnedWordDao

    companion object {
        @Volatile
        private var instance: FynLegacyDatabase? = null

        fun getInstance(context: Context): FynLegacyDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FynLegacyDatabase::class.java,
                    "fynlegacy_offline.db",
                ).build().also { instance = it }
            }
    }
}
