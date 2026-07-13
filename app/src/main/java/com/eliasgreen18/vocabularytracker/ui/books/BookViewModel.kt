package com.eliasgreen18.vocabularytracker.ui.books

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.util.FileStorageService
import com.eliasgreen18.vocabularytracker.domain.model.Book
import com.eliasgreen18.vocabularytracker.domain.model.BookWithStats
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    getBooksUseCase: GetBooksUseCase,
    private val addBookUseCase: AddBookUseCase,
    private val fileStorageService: FileStorageService,
) : ViewModel() {

    val booksState: StateFlow<List<BookWithStats>> = getBooksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBook(title: String, author: String, language: String, genre: String?, coverUri: Uri?) {
        viewModelScope.launch {
            var coverPath: String? = null
            coverUri?.let {
                coverPath = fileStorageService.saveBookCover(it).getOrNull()
            }
            
            addBookUseCase(
                Book(
                    title = title,
                    author = author,
                    language = language,
                    genre = genre,
                    coverPath = coverPath
                )
            )
        }
    }
}
