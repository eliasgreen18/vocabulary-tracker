package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ChapterStats
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetChapterStatsUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(chapterId: Long): Flow<ChapterStats> {
        return combine(
            repository.getTopWordsForChapter(chapterId, 10),
            repository.getChapterWords(chapterId)
        ) { topWords, allWordsInChapter ->
            ChapterStats(
                chapterId = chapterId,
                uniqueWordsCount = allWordsInChapter.size,
                newWordsCount = allWordsInChapter.count { it.mastery == WordMastery.NEW },
                topWords = topWords
            )
        }
    }
}
