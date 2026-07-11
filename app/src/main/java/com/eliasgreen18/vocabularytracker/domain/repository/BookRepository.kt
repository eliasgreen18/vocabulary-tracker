package com.eliasgreen18.vocabularytracker.domain.repository

import com.eliasgreen18.vocabularytracker.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    fun getAllBooksWithStats(): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.BookWithStats>>
    fun getBookById(id: Long): Flow<Book?>
    suspend fun insertBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun updateLastOpened(bookId: Long)
    suspend fun updateBookStatus(bookId: Long, status: com.eliasgreen18.vocabularytracker.domain.model.BookStatus)
}
