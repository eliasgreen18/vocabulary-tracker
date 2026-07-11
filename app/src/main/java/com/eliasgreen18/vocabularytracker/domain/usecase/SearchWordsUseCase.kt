package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchWordsUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(
        query: String, 
        bookId: Long? = null,
        author: String? = null,
        isFavorite: Boolean? = null,
        minHits: Int? = null,
        maxHits: Int? = null
    ): Flow<List<WordWithCount>> {
        return repository.searchWords(query, bookId, author, isFavorite, minHits, maxHits)
    }
}
