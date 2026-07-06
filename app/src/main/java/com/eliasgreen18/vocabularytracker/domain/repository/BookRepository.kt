package com.eliasgreen18.vocabularytracker.domain.repository

import com.eliasgreen18.vocabularytracker.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    fun getBookById(id: Long): Flow<Book?>
    suspend fun insertBook(book: Book): Long
    suspend fun updateLastOpened(bookId: Long)
}
