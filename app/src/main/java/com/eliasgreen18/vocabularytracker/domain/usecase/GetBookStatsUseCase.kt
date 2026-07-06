package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Book
import com.eliasgreen18.vocabularytracker.domain.model.BookStats
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetBookStatsUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val bookRepository: BookRepository
) {
    operator fun invoke(bookId: Long): Flow<BookStats?> {
        return combine(
            bookRepository.getBookById(bookId),
            wordRepository.getWordsForBook(bookId),
            wordRepository.getTopWordsForBook(bookId, 5)
        ) { book: Book?, allWordsInBook: List<WordWithCount>, topWords: List<WordWithCount> ->
            if (book == null) return@combine null
            
            BookStats(
                bookId = bookId,
                bookTitle = book.title,
                uniqueWordsCount = allWordsInBook.size,
                totalOccurrencesCount = allWordsInBook.sumOf { it.globalCount },
                learnedWordsCount = allWordsInBook.count { it.mastery == WordMastery.LEARNED },
                topWords = topWords
            )
        }
    }
}
