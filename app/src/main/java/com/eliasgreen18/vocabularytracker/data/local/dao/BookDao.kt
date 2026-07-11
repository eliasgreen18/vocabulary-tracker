package com.eliasgreen18.vocabularytracker.data.local.dao

import androidx.room.*
import com.eliasgreen18.vocabularytracker.data.local.entity.BookEntity
import com.eliasgreen18.vocabularytracker.data.local.entity.BookWithStatsEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastOpenedAt DESC, title ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("""
        SELECT 
            b.id as id,
            b.title as title,
            b.author as author,
            b.language as language,
            b.genre as genre,
            b.coverPath as coverPath,
            b.filePath as filePath,
            b.status as status,
            b.lastOpenedAt as lastOpenedAt,
            (SELECT COUNT(DISTINCT o.wordId) FROM occurrences o JOIN reading_sessions rs ON o.sessionId = rs.id JOIN chapters c ON rs.chapterId = c.id WHERE c.bookId = b.id) as wordCount,
            (SELECT COUNT(*) FROM chapters c WHERE c.bookId = b.id) as chapterCount
        FROM books b
        ORDER BY lastOpenedAt DESC, title ASC
    """)
    fun getAllBooksWithStats(): Flow<List<BookWithStatsEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Long): Flow<BookEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET lastOpenedAt = :timestamp WHERE id = :bookId")
    suspend fun updateLastOpened(bookId: Long, timestamp: Instant)

    @Query("UPDATE books SET status = :status WHERE id = :bookId")
    suspend fun updateBookStatus(bookId: Long, status: String)

    @Delete
    suspend fun deleteBook(book: BookEntity)
}
