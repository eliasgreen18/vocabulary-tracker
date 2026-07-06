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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VocabularyDatabase {
        return Room.databaseBuilder(
            context,
            VocabularyDatabase::class.java,
            "vocabulary_db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
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
}
