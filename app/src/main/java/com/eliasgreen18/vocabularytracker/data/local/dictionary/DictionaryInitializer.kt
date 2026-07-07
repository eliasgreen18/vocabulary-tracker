package com.eliasgreen18.vocabularytracker.data.local.dictionary

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject

class DictionaryInitializer @Inject constructor(
    private val dictionaryDao: DictionaryDao
) {
    suspend fun initializeIfNeeded(context: Context) {
        val currentVersion = 4 // Increment this when updating dictionary_starter.json
        val prefs = context.getSharedPreferences("dictionary_prefs", Context.MODE_PRIVATE)
        val lastVersion = prefs.getInt("last_init_version", 0)

        withContext(Dispatchers.IO) {
            if (lastVersion < currentVersion || dictionaryDao.getCount() == 0) {
                try {
                    val inputStream = context.assets.open("dictionary_starter.json")
                    val reader = InputStreamReader(inputStream)
                    val type = object : TypeToken<List<DictionaryWord>>() {}.type
                    val words: List<DictionaryWord> = Gson().fromJson(reader, type)
                    
                    dictionaryDao.insertWords(words)
                    inputStream.close()
                    
                    prefs.edit().putInt("last_init_version", currentVersion).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
