package com.eliasgreen18.vocabularytracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.model.Word
import java.time.Instant

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val translation: String?,
    val isFocusWord: Boolean = false,
    val lastReviewedAt: Long? = null,
    val reviewPriority: Int = 0,
    val translationStatus: String = TranslationStatus.NOT_REQUESTED.name,
    // SRS Columns
    val nextReviewAt: Long? = null,
    val lastSrsReviewAt: Long? = null,
    val reviewCount: Int = 0,
    val successfulReviews: Int = 0,
    val currentIntervalDays: Int = 0
)

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
    currentIntervalDays = currentIntervalDays
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
    currentIntervalDays = currentIntervalDays
)
