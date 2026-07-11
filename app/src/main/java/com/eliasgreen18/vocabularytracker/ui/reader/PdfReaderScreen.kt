package com.eliasgreen18.vocabularytracker.ui.reader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    onNavigateBack: () -> Unit,
    viewModel: PdfReaderViewModel = hiltViewModel()
) {
    val pdfPath by viewModel.pdfPath.collectAsState()
    val extractedText by viewModel.extractedText.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Digital Reader") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            if (pdfPath == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val file = File(pdfPath!!)
                if (file.exists()) {
                    PdfView(
                        file = file, 
                        listState = listState,
                        onPageAction = { viewModel.extractTextFromBitmap(it) }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("File not found at: ${file.absolutePath}")
                    }
                }
            }

            if (isProcessing) {
                Surface(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Analyzing page text...", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Results Modal (Bottom Sheet style)
            if (extractedText != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { viewModel.clearExtraction() },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.6f)
                            .clickable(enabled = false) {}, 
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Detected Sentences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { viewModel.clearExtraction() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                            
                            HorizontalDivider(thickness = 0.5.dp)
                            
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(extractedText!!.textBlocks) { block ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                viewModel.addWordToVocabulary(block.text, block.text)
                                                viewModel.clearExtraction()
                                            },
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                        shape = MaterialTheme.shapes.medium,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = block.text,
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PdfView(file: File, listState: androidx.compose.foundation.lazy.LazyListState, onPageAction: (Bitmap) -> Unit) {
    val pfd = remember(file) { 
        try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: Exception) {
            null
        }
    }
    
    if (pfd == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error accessing file. Try attaching it again.")
        }
        return
    }

    val renderer = remember(pfd) { PdfRenderer(pfd) }
    val pageCount = renderer.pageCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(pageCount) { index ->
            PdfPageItem(renderer, index, onPageAction)
        }
    }
}

@Composable
fun PdfPageItem(renderer: PdfRenderer, index: Int, onAction: (Bitmap) -> Unit) {
    val bitmap = remember(index) {
        try {
            val page = renderer.openPage(index)
            // Display res (1.2x) balance between quality and memory
            val b = Bitmap.createBitmap((page.width * 1.2f).toInt(), (page.height * 1.2f).toInt(), Bitmap.Config.ARGB_8888)
            b.eraseColor(android.graphics.Color.WHITE)
            page.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            b
        } catch (e: Exception) {
            Log.e("PdfPageItem", "Render error", e)
            null
        }
    }

    if (bitmap != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Page ${index + 1}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                }
                
                // OCR Action (Triggering 3x render only for extraction)
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(48.dp)
                        .clickable { 
                            try {
                                val page = renderer.openPage(index)
                                val hiRes = Bitmap.createBitmap((page.width * 3.0f).toInt(), (page.height * 3.0f).toInt(), Bitmap.Config.ARGB_8888)
                                hiRes.eraseColor(android.graphics.Color.WHITE)
                                page.render(hiRes, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                page.close()
                                onAction(hiRes)
                            } catch (e: Exception) {
                                onAction(bitmap)
                            }
                        },
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.TextSnippet, 
                            contentDescription = "Extract Text",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Text(
                text = "Page ${index + 1}", 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
