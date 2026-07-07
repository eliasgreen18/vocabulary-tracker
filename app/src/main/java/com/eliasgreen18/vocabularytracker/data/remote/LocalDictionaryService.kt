package com.eliasgreen18.vocabularytracker.data.remote

import com.eliasgreen18.vocabularytracker.data.local.dictionary.DictionaryDao
import com.eliasgreen18.vocabularytracker.domain.repository.TranslationService
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class LocalDictionaryService @Inject constructor(
    private val dictionaryDao: DictionaryDao
) : TranslationService {
    override suspend fun translate(text: String, sourceLang: String, targetLang: String): Result<String> {
        // Simulate minor overhead for UI consistency (optional, but makes it feel "searched")
        delay(50.milliseconds) 
        
        val translation = dictionaryDao.findTranslation(text.trim().lowercase())
        
        return if (translation != null) {
            Result.success(translation)
        } else {
            Result.failure(Exception("Not found in local dictionary"))
        }
    }
}
