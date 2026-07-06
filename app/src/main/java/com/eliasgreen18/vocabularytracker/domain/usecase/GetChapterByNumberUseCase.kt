package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.repository.ChapterRepository
import javax.inject.Inject

class GetChapterByNumberUseCase @Inject constructor(
    private val repository: ChapterRepository
) {
    suspend operator fun invoke(bookId: Long, number: Int): Chapter? {
        return repository.getChapterByNumber(bookId, number)
    }
}
