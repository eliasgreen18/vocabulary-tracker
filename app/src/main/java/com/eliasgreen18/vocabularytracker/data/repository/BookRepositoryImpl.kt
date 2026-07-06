package com.eliasgreen18.vocabularytracker.data.repository

import com.eliasgreen18.vocabularytracker.data.local.dao.BookDao
import com.eliasgreen18.vocabularytracker.data.local.entity.toDomain
import com.eliasgreen18.vocabularytracker.data.local.entity.toEntity
import com.eliasgreen18.vocabularytracker.domain.model.Book
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {
    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBookById(id: Long): Flow<Book?> {
        return bookDao.getBookById(id).map { it?.toDomain() }
    }

    override suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book.toEntity())
    }

    override suspend fun updateLastOpened(bookId: Long) {
        bookDao.updateLastOpened(bookId, Instant.now())
    }
}
