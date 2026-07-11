package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.repository.ChapterRepository
import javax.inject.Inject

class DeleteChapterUseCase @Inject constructor(
    private val repository: ChapterRepository
) {
    suspend operator fun invoke(chapterId: Long) {
        repository.deleteChapter(chapterId)
    }
}
