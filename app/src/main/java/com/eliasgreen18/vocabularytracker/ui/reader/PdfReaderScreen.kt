package com.eliasgreen18.vocabularytracker.ui.reader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    onNavigateBack: () -> Unit,
    viewModel: PdfReaderViewModel = hiltViewModel()
) {
    val book by viewModel.bookState.collectAsState()
    val extractedText by viewModel.extractedText.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    
    val context = LocalContext.current
    
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(book) {
        book?.filePath?.let { path ->
            withContext(Dispatchers.IO) {
                val file = File(path)
                if (file.exists()) {
                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    pdfRenderer = renderer
                    pageCount = renderer.pageCount
                    
                    val bitmap = renderPageToBitmap(0, renderer)
                    withContext(Dispatchers.Main) {
                        currentBitmap = bitmap
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: stringResource(R.string.reader_loading), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (pageCount > 0) {
                        Text("${currentPage + 1} / $pageCount", modifier = Modifier.padding(end = 16.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { 
                        if (currentPage > 0) {
                            currentPage--
                            pdfRenderer?.let { r -> 
                                val b = renderPageToBitmap(currentPage, r)
                                currentBitmap = b
                            }
                        }
                    }) { Icon(Icons.Default.ChevronLeft, contentDescription = "Prev") }
                    
                    IconButton(onClick = { 
                        if (currentPage < pageCount - 1) {
                            currentPage++
                            pdfRenderer?.let { r -> 
                                val b = renderPageToBitmap(currentPage, r)
                                currentBitmap = b
                            }
                        }
                    }) { Icon(Icons.Default.ChevronRight, contentDescription = "Next") }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            currentBitmap?.let { viewModel.extractTextFromBitmap(it) }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        else Icon(Icons.Default.Translate, contentDescription = "Extract Text")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color.Gray)) {
            currentBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (extractedText != null) {
            ModalBottomSheet(onDismissRequest = { viewModel.clearExtraction() }) {
                Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.6f)) {
                    Text(stringResource(R.string.reader_detected_text), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.reader_tap_to_save), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val sentences = extractedText!!.text.split("\n").filter { it.isNotBlank() }
                        items(sentences) { sentence ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { 
                                    viewModel.addWordToVocabulary(sentence, sentence)
                                    Toast.makeText(context, context.getString(R.string.added_snackbar, sentence.take(20)), Toast.LENGTH_SHORT).show()
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Text(sentence, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun renderPageToBitmap(pageIndex: Int, renderer: PdfRenderer): Bitmap {
    val page = renderer.openPage(pageIndex)
    // Scale for better OCR
    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    page.close()
    return bitmap
}
