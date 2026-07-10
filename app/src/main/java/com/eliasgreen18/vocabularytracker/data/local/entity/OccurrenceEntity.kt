package com.eliasgreen18.vocabularytracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eliasgreen18.vocabularytracker.domain.model.Occurrence
import java.time.Instant

@Entity(tableName = "occurrences")
data class OccurrenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Long,
    val sessionId: Long,
    val createdAt: Instant,
    val snippet: String? = null
)

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
