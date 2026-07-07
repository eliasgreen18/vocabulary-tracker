package com.eliasgreen18.vocabularytracker.data.local.dictionary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary")
data class DictionaryWord(
    @PrimaryKey val text: String,
    val translation: String
)
