package com.eliasgreen18.vocabularytracker.domain.model

data class RelatedWord(
    val wordId: Long,
    val wordText: String,
    val globalCount: Int,
    val isFocusWord: Boolean,
    val translation: String?,
    val relationshipType: RelationshipType
)
