package com.eliasgreen18.vocabularytracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "occurrences",
    indices = [
        Index("wordId"),
        Index("sessionId"),
        Index("createdAt")
    ]
)
data class OccurrenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Long,
    val sessionId: Long,
    val createdAt: Instant,
    val snippet: String? = null
)
