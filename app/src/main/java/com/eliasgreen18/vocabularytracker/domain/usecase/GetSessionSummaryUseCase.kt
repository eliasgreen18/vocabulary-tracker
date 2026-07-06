package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.SessionSummary
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import javax.inject.Inject

class GetSessionSummaryUseCase @Inject constructor() {
    operator fun invoke(words: List<WordWithCount>): SessionSummary {
        val groups = words.groupBy { it.mastery }
        return SessionSummary(
            totalWords = words.size,
            newWords = groups[WordMastery.NEW]?.size ?: 0,
            learningWords = groups[WordMastery.LEARNING]?.size ?: 0,
            learnedWords = groups[WordMastery.LEARNED]?.size ?: 0
        )
    }
}
