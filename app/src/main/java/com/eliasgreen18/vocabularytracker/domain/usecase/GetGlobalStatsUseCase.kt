package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.GlobalStats
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetGlobalStatsUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(): Flow<GlobalStats> {
        return combine(
            repository.searchWords(""),
            repository.getTotalOccurrencesCount(),
            repository.getTranslatedWordsCount()
        ) { allWords, totalOccurrences, translatedCount ->
            val groups = allWords.groupBy { it.mastery }
            GlobalStats(
                uniqueWordsCount = allWords.size,
                totalOccurrencesCount = totalOccurrences,
                newWordsCount = groups[WordMastery.NEW]?.size ?: 0,
                learningWordsCount = groups[WordMastery.LEARNING]?.size ?: 0,
                learnedWordsCount = groups[WordMastery.LEARNED]?.size ?: 0,
                translatedWordsCount = translatedCount
            )
        }
    }
}
