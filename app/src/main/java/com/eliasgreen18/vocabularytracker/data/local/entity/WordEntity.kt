package com.eliasgreen18.vocabularytracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus

@Entity(
    tableName = "words",
    indices = [
        Index("text"),
        Index("translation"),
        Index("isFocusWord"),
        Index("globalCount")
    ]
)
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
    val currentIntervalDays: Int = 0,
    val ipa: String? = null,
    val notes: String? = null,
    val aiExplanation: String? = null,
    val aiExamples: String? = null,
    val globalCount: Int = 0
)
