package com.eliasgreen18.vocabularytracker.ui.scanner

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.remote.ocr.TextRecognitionService
import com.google.mlkit.vision.text.Text
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraScannerViewModel @Inject constructor(
    private val ocrService: TextRecognitionService
) : ViewModel() {

    private val _scannedText = MutableStateFlow<Text?>(null)
    val scannedText = _scannedText.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    fun onImageCaptured(imageProxy: ImageProxy) {
        if (_isProcessing.value) return
        
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = ocrService.analyzeImage(imageProxy)
                _scannedText.value = result
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    fun clearResults() {
        _scannedText.value = null
    }
}
