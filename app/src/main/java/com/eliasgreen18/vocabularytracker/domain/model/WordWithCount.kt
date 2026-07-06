package com.eliasgreen18.vocabularytracker.domain.model

data class WordWithCount(
    val wordId: Long,
    val wordText: String,
    val sessionCount: Int,
    val globalCount: Int,
    val isFocusWord: Boolean = false,
    val translation: String? = null,
    val translationStatus: TranslationStatus = TranslationStatus.NOT_REQUESTED
) {
    val mastery: WordMastery
        get() = WordMastery.fromCount(globalCount)
}
