package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.repository.ChapterRepository
import javax.inject.Inject

class UpsertChapterUseCase @Inject constructor(
    private val repository: ChapterRepository
) {
    suspend operator fun invoke(chapter: Chapter): Long {
        return if (chapter.id == 0L) {
            repository.insertChapter(chapter)
        } else {
            repository.updateChapter(chapter)
            chapter.id
        }
    }
}
