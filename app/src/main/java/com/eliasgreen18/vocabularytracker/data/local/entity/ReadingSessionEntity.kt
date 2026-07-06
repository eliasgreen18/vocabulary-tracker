package com.eliasgreen18.vocabularytracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.eliasgreen18.vocabularytracker.domain.model.ReadingSession
import java.time.Instant

@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val startedAt: Instant,
    val endedAt: Instant?
)

fun ReadingSessionEntity.toDomain() = ReadingSession(
    id = id,
    chapterId = chapterId,
    startedAt = startedAt,
    endedAt = endedAt
)

fun ReadingSession.toEntity() = ReadingSessionEntity(
    id = id,
    chapterId = chapterId,
    startedAt = startedAt,
    endedAt = endedAt
)
