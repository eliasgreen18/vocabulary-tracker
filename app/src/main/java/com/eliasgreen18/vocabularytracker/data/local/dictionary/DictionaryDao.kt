package com.eliasgreen18.vocabularytracker.data.local.dictionary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DictionaryDao {
    @Query("SELECT translation FROM dictionary WHERE text = :text LIMIT 1")
    suspend fun findTranslation(text: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<DictionaryWord>)
    
    @Query("SELECT COUNT(*) FROM dictionary")
    suspend fun getCount(): Int
}
