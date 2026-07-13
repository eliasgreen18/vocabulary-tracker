package com.eliasgreen18.vocabularytracker.data.repository

import com.eliasgreen18.vocabularytracker.data.local.dao.BookDao
import com.eliasgreen18.vocabularytracker.data.mapper.toDomain
import com.eliasgreen18.vocabularytracker.data.mapper.toEntity
import com.eliasgreen18.vocabularytracker.data.util.FileStorageService
import com.eliasgreen18.vocabularytracker.domain.model.Book
import com.eliasgreen18.vocabularytracker.domain.model.BookStatus
import com.eliasgreen18.vocabularytracker.domain.model.BookWithStats
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val fileStorageService: FileStorageService
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllBooksWithStats(): Flow<List<BookWithStats>> {
        return bookDao.getAllBooksWithStats().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBookById(id: Long): Flow<Book?> {
        return bookDao.getBookById(id).map { it?.toDomain() }
    }

    override suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book.toEntity())
    }

    override suspend fun updateBook(book: Book) {
        bookDao.updateBook(book.toEntity())
    }

    override suspend fun updateLastOpened(bookId: Long) {
        bookDao.updateLastOpened(bookId, Instant.now())
    }

    override suspend fun updateBookStatus(bookId: Long, status: BookStatus) {
        bookDao.updateBookStatus(bookId, status.name)
    }

    override suspend fun deleteBook(bookId: Long) {
        val book = bookDao.getBookById(bookId).first()?.toDomain()
        book?.let {
            it.coverPath?.let { path -> fileStorageService.deleteFile(path) }
            it.filePath?.let { path -> fileStorageService.deleteFile(path) }
            bookDao.deleteBook(it.toEntity())
        }
    }
}
