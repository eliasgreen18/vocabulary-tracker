package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.LearningProgress
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetLearningProgressUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(): Flow<LearningProgress> {
        val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return combine(
            repository.searchWords(""), // Get all words with counts
            repository.getUniqueWordsCountSince(startOfToday)
        ) { allWords, countToday ->
            val groups = allWords.groupBy { it.mastery }
            LearningProgress(
                totalWords = allWords.size,
                newCount = groups[WordMastery.NEW]?.size ?: 0,
                learningCount = groups[WordMastery.LEARNING]?.size ?: 0,
                learnedCount = groups[WordMastery.LEARNED]?.size ?: 0,
                wordsAddedToday = countToday
            )
        }
    }
}
