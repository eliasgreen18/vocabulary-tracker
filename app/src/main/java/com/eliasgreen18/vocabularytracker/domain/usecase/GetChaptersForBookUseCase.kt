package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChaptersForBookUseCase @Inject constructor(
    private val repository: ChapterRepository
) {
    operator fun invoke(bookId: Long): Flow<List<Chapter>> {
        return repository.getChaptersForBook(bookId)
    }
}
