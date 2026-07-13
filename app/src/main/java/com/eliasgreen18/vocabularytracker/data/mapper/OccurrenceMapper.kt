package com.eliasgreen18.vocabularytracker.data.mapper

import com.eliasgreen18.vocabularytracker.data.local.entity.OccurrenceEntity
import com.eliasgreen18.vocabularytracker.domain.model.Occurrence

fun OccurrenceEntity.toDomain() = Occurrence(
    id = id,
    wordId = wordId,
    sessionId = sessionId,
    createdAt = createdAt,
    snippet = snippet
)

fun Occurrence.toEntity() = OccurrenceEntity(
    id = id,
    wordId = wordId,
    sessionId = sessionId,
    createdAt = createdAt,
    snippet = snippet
)
