package com.eliasgreen18.vocabularytracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eliasgreen18.vocabularytracker.data.local.dao.*
import com.eliasgreen18.vocabularytracker.data.local.entity.*

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        ReadingSessionEntity::class,
        WordEntity::class,
        OccurrenceEntity::class,
        WordRelationshipEntity::class,
        HighlightEntity::class
    ],
    version = 21,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VocabularyDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun wordDao(): WordDao
    abstract fun occurrenceDao(): OccurrenceDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun highlightDao(): HighlightDao
}
