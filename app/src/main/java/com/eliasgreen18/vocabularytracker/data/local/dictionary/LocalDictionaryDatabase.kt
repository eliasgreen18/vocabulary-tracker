package com.eliasgreen18.vocabularytracker.data.local.dictionary

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DictionaryWord::class], version = 1, exportSchema = false)
abstract class LocalDictionaryDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao
}
