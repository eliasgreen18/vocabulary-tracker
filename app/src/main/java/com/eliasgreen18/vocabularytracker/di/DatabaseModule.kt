package com.eliasgreen18.vocabularytracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eliasgreen18.vocabularytracker.data.local.db.VocabularyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE reading_sessions_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    bookId INTEGER NOT NULL,
                    chapterNumber INTEGER NOT NULL,
                    chapterTitle TEXT,
                    startedAt INTEGER NOT NULL,
                    endedAt INTEGER
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO reading_sessions_new (id, bookId, chapterNumber, chapterTitle, startedAt, endedAt)
                SELECT id, bookId, CAST(chapter AS INTEGER), chapter, startedAt, endedAt
                FROM reading_sessions
                """.trimIndent()
            )
            db.execSQL("DROP TABLE reading_sessions")
            db.execSQL("ALTER TABLE reading_sessions_new RENAME TO reading_sessions")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE books ADD COLUMN lastOpenedAt INTEGER")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Create chapters table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chapters` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `bookId` INTEGER NOT NULL, 
                    `number` INTEGER NOT NULL, 
                    `title` TEXT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_chapters_bookId_number` ON `chapters` (`bookId`, `number`)")

            // 2. Migrate unique chapters from reading_sessions to chapters
            // Use INSERT OR IGNORE to handle cases where DISTINCT might still fail due to data inconsistencies
            db.execSQL(
                """
                INSERT OR IGNORE INTO chapters (bookId, number, title)
                SELECT bookId, chapterNumber, MAX(chapterTitle)
                FROM reading_sessions
                GROUP BY bookId, chapterNumber
                """.trimIndent()
            )

            // 3. Create new reading_sessions table with chapterId
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reading_sessions_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `chapterId` INTEGER NOT NULL, 
                    `startedAt` INTEGER NOT NULL, 
                    `endedAt` INTEGER,
                    FOREIGN KEY(`chapterId`) REFERENCES `chapters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )

            // 4. Populate new reading_sessions table by joining with the newly created chapters
            db.execSQL(
                """
                INSERT INTO reading_sessions_new (id, chapterId, startedAt, endedAt)
                SELECT rs.id, c.id, rs.startedAt, rs.endedAt
                FROM reading_sessions rs
                JOIN chapters c ON rs.bookId = c.bookId AND rs.chapterNumber = c.number
                """.trimIndent()
            )

            // 5. Drop old table and rename new one
            db.execSQL("DROP TABLE reading_sessions")
            db.execSQL("ALTER TABLE reading_sessions_new RENAME TO reading_sessions")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE words ADD COLUMN isFocusWord INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE words ADD COLUMN lastReviewedAt INTEGER")
            db.execSQL("ALTER TABLE words ADD COLUMN reviewPriority INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE words ADD COLUMN translationStatus TEXT NOT NULL DEFAULT 'NOT_REQUESTED'")
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE words ADD COLUMN nextReviewAt INTEGER")
            db.execSQL("ALTER TABLE words ADD COLUMN lastSrsReviewAt INTEGER")
            db.execSQL("ALTER TABLE words ADD COLUMN reviewCount INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE words ADD COLUMN successfulReviews INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE words ADD COLUMN currentIntervalDays INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Add IPA column to words
            db.execSQL("ALTER TABLE words ADD COLUMN ipa TEXT")
            
            // 2. Refactor chapters table number column from Int to String
            // Create a temporary table with the new schema
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chapters_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `bookId` INTEGER NOT NULL, 
                    `number` TEXT NOT NULL, 
                    `title` TEXT
                )
                """.trimIndent()
            )
            // Copy data, casting number to String
            db.execSQL(
                """
                INSERT INTO chapters_new (id, bookId, number, title)
                SELECT id, bookId, CAST(number AS TEXT), title FROM chapters
                """.trimIndent()
            )
            
            // Swap tables
            db.execSQL("DROP TABLE chapters")
            db.execSQL("ALTER TABLE chapters_new RENAME TO chapters")

            // Re-create the unique index ON THE FINAL TABLE NAME
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_chapters_bookId_number` ON `chapters` (`bookId`, `number`)")
        }
    }

    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE occurrences ADD COLUMN snippet TEXT")
        }
    }

    private val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE words ADD COLUMN notes TEXT")
        }
    }

    private val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `word_relationships` (
                    `wordId` INTEGER NOT NULL, 
                    `relatedWordId` INTEGER NOT NULL, 
                    `type` TEXT NOT NULL, 
                    PRIMARY KEY(`wordId`, `relatedWordId`, `type`), 
                    FOREIGN KEY(`wordId`) REFERENCES `words`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                    FOREIGN KEY(`relatedWordId`) REFERENCES `words`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_word_relationships_wordId` ON `word_relationships` (`wordId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_word_relationships_relatedWordId` ON `word_relationships` (`relatedWordId`)")
        }
    }

    private val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE words ADD COLUMN aiExplanation TEXT")
            db.execSQL("ALTER TABLE words ADD COLUMN aiExamples TEXT")
        }
    }

    private val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE books ADD COLUMN genre TEXT")
        }
    }

    private val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE books ADD COLUMN coverPath TEXT")
            db.execSQL("ALTER TABLE books ADD COLUMN status TEXT NOT NULL DEFAULT 'READING'")
        }
    }

    private val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE reading_sessions ADD COLUMN activeDurationSeconds INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE books ADD COLUMN filePath TEXT")
        }
    }

    private val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `highlights` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `bookId` INTEGER NOT NULL, 
                    `chapterIndex` INTEGER NOT NULL, 
                    `startOffset` INTEGER NOT NULL, 
                    `endOffset` INTEGER NOT NULL, 
                    `colorHex` TEXT NOT NULL, 
                    `text` TEXT NOT NULL, 
                    `createdAt` INTEGER NOT NULL, 
                    FOREIGN KEY(`bookId`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_highlights_bookId` ON `highlights` (`bookId`)")
        }
    }

    private val MIGRATION_18_19 = object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE books ADD COLUMN lastChapterIndex INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE books ADD COLUMN lastScrollOffset INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_19_20 = object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Words indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_words_text` ON `words` (`text`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_words_translation` ON `words` (`translation`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_words_isFocusWord` ON `words` (`isFocusWord`)")
            
            // Occurrences indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_occurrences_wordId` ON `occurrences` (`wordId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_occurrences_sessionId` ON `occurrences` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_occurrences_createdAt` ON `occurrences` (`createdAt`)")
            
            // Reading sessions indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reading_sessions_chapterId` ON `reading_sessions` (`chapterId`)")
            
            // Books indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_books_author` ON `books` (`author`)")
        }
    }

    private val MIGRATION_20_21 = object : Migration(20, 21) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Add globalCount column
            db.execSQL("ALTER TABLE words ADD COLUMN globalCount INTEGER NOT NULL DEFAULT 0")
            
            // 2. Populate globalCount with existing data
            db.execSQL("""
                UPDATE words 
                SET globalCount = (
                    SELECT COUNT(*) 
                    FROM occurrences 
                    WHERE occurrences.wordId = words.id
                )
            """)
            
            // 3. Index the new column for sorting
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_words_globalCount` ON `words` (`globalCount`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VocabularyDatabase {
        return Room.databaseBuilder(
            context,
            VocabularyDatabase::class.java,
            "vocabulary_db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideBookDao(database: VocabularyDatabase) = database.bookDao()

    @Provides
    fun provideChapterDao(database: VocabularyDatabase) = database.chapterDao()

    @Provides
    fun provideReadingSessionDao(database: VocabularyDatabase) = database.readingSessionDao()

    @Provides
    fun provideWordDao(database: VocabularyDatabase) = database.wordDao()

    @Provides
    fun provideOccurrenceDao(database: VocabularyDatabase) = database.occurrenceDao()

    @Provides
    fun provideRelationshipDao(database: VocabularyDatabase) = database.relationshipDao()

    @Provides
    fun provideHighlightDao(database: VocabularyDatabase) = database.highlightDao()
}
