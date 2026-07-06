package com.eliasgreen18.vocabularytracker.data.local.dao

import androidx.room.*
import com.eliasgreen18.vocabularytracker.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastOpenedAt DESC, title ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Long): Flow<BookEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Query("UPDATE books SET lastOpenedAt = :timestamp WHERE id = :bookId")
    suspend fun updateLastOpened(bookId: Long, timestamp: Instant)

    @Delete
    suspend fun deleteBook(book: BookEntity)
}
