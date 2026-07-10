package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class WordDetailUiState(
    val word: Word,
    val totalOccurrences: Int,
    val bookCount: Int,
    val chapterCount: Int,
    val firstSeen: Instant?,
    val lastSeen: Instant?,
    val history: List<WordOccurrenceDetail> = emptyList(),
    val journey: List<JourneyEvent> = emptyList(),
    val relatedWords: List<RelatedWord> = emptyList(),
    // SRS Stats
    val nextReviewDate: Instant? = null,
    val currentInterval: Int = 0,
    val successCount: Int = 0,
    val failCount: Int = 0,
    val recallAccuracy: Int = 0,
    val mainLanguage: String? = null
) {
    val mastery: WordMastery
        get() = WordMastery.fromCount(totalOccurrences)
}
