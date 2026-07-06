package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import javax.inject.Inject

class GetWordsByMasteryUseCase @Inject constructor() {
    operator fun invoke(words: List<WordWithCount>, mastery: WordMastery?): List<WordWithCount> {
        return if (mastery == null) {
            words
        } else {
            words.filter { it.mastery == mastery }
        }
    }
}
