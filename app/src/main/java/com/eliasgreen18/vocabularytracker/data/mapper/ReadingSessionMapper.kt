package com.eliasgreen18.vocabularytracker.data.mapper

import com.eliasgreen18.vocabularytracker.data.local.dao.ActiveSessionWithDetails
import com.eliasgreen18.vocabularytracker.data.local.entity.ReadingSessionEntity
import com.eliasgreen18.vocabularytracker.domain.model.ActiveSessionInfo
import com.eliasgreen18.vocabularytracker.domain.model.Book
import com.eliasgreen18.vocabularytracker.domain.model.ReadingSession

fun ReadingSessionEntity.toDomain() = ReadingSession(
    id = id,
    chapterId = chapterId,
    startedAt = startedAt,
    endedAt = endedAt,
    activeDurationSeconds = activeDurationSeconds
)

fun ReadingSession.toEntity() = ReadingSessionEntity(
    id = id,
    chapterId = chapterId,
    startedAt = startedAt,
    endedAt = endedAt,
    activeDurationSeconds = activeDurationSeconds
)

fun ActiveSessionWithDetails.toDomain(book: Book?) = ActiveSessionInfo(
    session = session.toDomain(),
    chapter = chapter.toDomain(),
    book = book
)
