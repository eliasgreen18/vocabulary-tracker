package com.eliasgreen18.vocabularytracker.ui.reader

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.remote.ocr.TextRecognitionService
import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.GetActiveSessionUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.RegisterWordUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.StartReadingSessionUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.UpsertChapterUseCase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PdfReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val registerWordUseCase: RegisterWordUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val startReadingSessionUseCase: StartReadingSessionUseCase,
    private val upsertChapterUseCase: UpsertChapterUseCase
) : ViewModel() {

    val bookId: Long = checkNotNull(savedStateHandle["bookId"])
    
    private val _pdfPath = MutableStateFlow<String?>(null)
    val pdfPath: StateFlow<String?> = _pdfPath.asStateFlow()
    
    private val _extractedText = MutableStateFlow<Text?>(null)
    val extractedText = _extractedText.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()
    
    private var activeSessionId: Long? = null

    init {
        viewModelScope.launch {
            val book = bookRepository.getBookById(bookId).first()
            _pdfPath.value = book?.filePath
            
            val active = getActiveSessionUseCase(bookId).first()
            if (active != null) {
                activeSessionId = active.id
            } else {
                val chapterId = upsertChapterUseCase(Chapter(bookId = bookId, number = "DIGITAL", title = "Active Reading"))
                activeSessionId = startReadingSessionUseCase(chapterId)
            }
        }
    }

    fun extractTextFromBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                // We could use TextRecognitionService but it takes ImageProxy. 
                // Let's call ML Kit directly here for Bitmap.
                val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                    com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
                )
                val result = recognizer.process(image).await()
                _extractedText.value = result
            } catch (e: Exception) {
                // Error
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun addWordToVocabulary(text: String, contextSnippet: String?) {
        val sessionId = activeSessionId ?: return
        viewModelScope.launch {
            registerWordUseCase(sessionId = sessionId, text = text, snippet = contextSnippet)
        }
    }
    
    fun clearExtraction() {
        _extractedText.value = null
    }
}
