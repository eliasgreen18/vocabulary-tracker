package com.eliasgreen18.vocabularytracker.data.mapper

import com.eliasgreen18.vocabularytracker.data.local.entity.*
import com.eliasgreen18.vocabularytracker.domain.model.*
import java.time.Instant

fun WordEntity.toDomain() = Word(
    id = id,
    text = text,
    translation = translation,
    isFocusWord = isFocusWord,
    lastReviewedAt = lastReviewedAt,
    reviewPriority = reviewPriority,
    translationStatus = TranslationStatus.valueOf(translationStatus),
    nextReviewAt = nextReviewAt?.let { Instant.ofEpochMilli(it) },
    lastSrsReviewAt = lastSrsReviewAt?.let { Instant.ofEpochMilli(it) },
    reviewCount = reviewCount,
    successfulReviews = successfulReviews,
    currentIntervalDays = currentIntervalDays,
    ipa = ipa,
    notes = notes,
    aiExplanation = aiExplanation,
    aiExamples = aiExamples,
    globalCount = globalCount
)

fun Word.toEntity() = WordEntity(
    id = id,
    text = text,
    translation = translation,
    isFocusWord = isFocusWord,
    lastReviewedAt = lastReviewedAt,
    reviewPriority = reviewPriority,
    translationStatus = translationStatus.name,
    nextReviewAt = nextReviewAt?.toEpochMilli(),
    lastSrsReviewAt = lastSrsReviewAt?.toEpochMilli(),
    reviewCount = reviewCount,
    successfulReviews = successfulReviews,
    currentIntervalDays = currentIntervalDays,
    ipa = ipa,
    notes = notes,
    aiExplanation = aiExplanation,
    aiExamples = aiExamples,
    globalCount = globalCount
)

fun WordWithCountEntity.toDomain() = WordWithCount(
    wordId = wordId,
    wordText = wordText,
    sessionCount = sessionCount,
    globalCount = globalCount,
    isFocusWord = isFocusWord,
    translation = translation,
    ipa = ipa,
    notes = notes,
    translationStatus = TranslationStatus.valueOf(translationStatus)
)

fun ReviewWordEntity.toDomain() = ReviewWord(
    wordId = wordId,
    wordText = wordText,
    globalCount = globalCount,
    isFocusWord = isFocusWord,
    lastReviewedAt = lastReviewedAt,
    reviewPriority = reviewPriority,
    lastContext = formatContext(lastBookTitle, lastChapterNumber, lastChapterTitle),
    lastBookLanguage = lastBookLanguage,
    lastSnippet = snippet,
    translation = translation,
    ipa = ipa,
    notes = notes,
    currentIntervalDays = currentIntervalDays,
    nextReviewAt = nextReviewAt
)

fun AuthorStatsEntity.toDomain() = AuthorStats(
    author = author,
    uniqueWordsCount = uniqueWordsCount,
    totalOccurrencesCount = totalOccurrencesCount
)

fun WordDiscoveryEntity.toDomain() = WordDiscovery(
    wordText = wordText,
    firstSeenAt = Instant.ofEpochMilli(firstSeenAt)
)

fun WordHistoryEntity.toDomain() = WordOccurrenceDetail(
    createdAt = createdAt,
    bookTitle = bookTitle,
    bookLanguage = bookLanguage,
    chapterNumber = chapterNumber,
    chapterTitle = chapterTitle,
    sessionId = sessionId,
    snippet = snippet
)

private fun formatContext(book: String?, chapter: String?, title: String?): String {
    val b = book ?: "Unknown Book"
    val c = if (chapter != null) "Chapter $chapter" else "Unknown Chapter"
    val t = title?.let { ": $it" } ?: ""
    return "$b - $c$t"
}
