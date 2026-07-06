package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import javax.inject.Inject

class GetHighlightedWordsUseCase @Inject constructor() {
    operator fun invoke(sessionWords: List<WordWithCount>): List<WordWithCount> {
        return sessionWords.filter { word ->
            // Highlight rules:
            // 1. Suggest as Focus Word (>= 3 times in THIS session)
            val suggestedFocus = word.sessionCount >= 3
            
            // 2. Close to level change (e.g., 2 -> 3 is change to LEARNING)
            val nearLevelChange = word.globalCount == 2
            
            // 3. Frequent in current session (>= 2 times)
            val frequentInSession = word.sessionCount >= 2
            
            suggestedFocus || nearLevelChange || frequentInSession
        }.sortedByDescending { it.sessionCount }
    }
}
