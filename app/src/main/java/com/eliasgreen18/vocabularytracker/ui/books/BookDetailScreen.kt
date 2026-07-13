package com.eliasgreen18.vocabularytracker.ui.books

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.eliasgreen18.vocabularytracker.R
import com.eliasgreen18.vocabularytracker.domain.model.Book
import com.eliasgreen18.vocabularytracker.domain.model.BookStatus
import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.model.ChapterMastery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    onNavigateToChapterDetail: (Long) -> Unit,
    onNavigateToPdfReader: (Long) -> Unit,
    onNavigateToEpubReader: (Long) -> Unit,
    onNavigateToStats: (Long) -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }
    var chapterToEdit by remember { mutableStateOf<Chapter?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateBookMetadata(uiState.book?.title ?: "", uiState.book?.author ?: "", uiState.book?.language ?: "", uiState.book?.genre, null, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToStats(uiState.book?.id ?: 0) }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Stats", tint = Color.White)
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Book", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        uiState.book?.let { currentBook ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    BookHeaderSection(
                        book = currentBook,
                        onImportFile = { filePickerLauncher.launch("*/*") },
                        onReadPdf = { onNavigateToPdfReader(currentBook.id) },
                        onReadEpub = { onNavigateToEpubReader(currentBook.id) },
                        onFinishBook = { viewModel.finishBook() },
                        onReopenBook = { viewModel.resumeBook() }
                    )
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.onContinueReading(onNavigateToSession) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Continue")
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            OutlinedButton(
                                onClick = { showNewSessionDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Session")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text(stringResource(R.string.search_chapters_hint), fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }
                }

                items(uiState.chapters) { chapter ->
                    val mastery = uiState.mastery[chapter.id]
                    ChapterItem(
                        chapter = chapter,
                        mastery = mastery,
                        onClick = { onNavigateToChapterDetail(chapter.id) },
                        onStartSession = { viewModel.startSessionForChapter(chapter.id, onNavigateToSession) },
                        onEdit = { chapterToEdit = chapter },
                        onDelete = { viewModel.deleteChapter(chapter.id) }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        uiState.book?.let { currentBook ->
            EditBookDialog(
                book = currentBook,
                onDismiss = { showEditDialog = false },
                onConfirm = { title, author, language, genre ->
                    viewModel.updateBookMetadata(title, author, language, genre, null)
                    showEditDialog = false
                },
                onDelete = {
                    viewModel.deleteBook()
                    showEditDialog = false
                    onNavigateBack()
                }
            )
        }
    }

    if (chapterToEdit != null) {
        EditChapterDialog(
            chapter = chapterToEdit!!,
            onDismiss = { chapterToEdit = null },
            onConfirm = { number, title ->
                viewModel.updateChapterInfo(chapterToEdit!!.id, number, title)
                chapterToEdit = null
            }
        )
    }

    if (showNewSessionDialog) {
        var chapterNumber by remember { mutableStateOf("") }
        var chapterTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewSessionDialog = false },
            title = { Text("Start Reading") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = chapterNumber,
                        onValueChange = { chapterNumber = it },
                        label = { Text("Chapter Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = chapterTitle,
                        onValueChange = { chapterTitle = it },
                        label = { Text("Chapter Title (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.startNewChapter(chapterNumber, chapterTitle.ifBlank { null }, onNavigateToSession)
                    showNewSessionDialog = false 
                }) { Text(stringResource(R.string.btn_start)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewSessionDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
fun BookHeaderSection(
    book: Book,
    onImportFile: () -> Unit,
    onReadPdf: () -> Unit,
    onReadEpub: () -> Unit,
    onFinishBook: () -> Unit,
    onReopenBook: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        if (book.coverPath != null) {
            AsyncImage(
                model = book.coverPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.75f), 
                            Color.Transparent, 
                            Color.Black.copy(alpha = 0.98f)
                        ),
                        startY = 0f,
                        endY = 1100f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "by ${book.author}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            val isEpub = book.filePath?.endsWith(".epub", ignoreCase = true) == true
            val isPdf = book.filePath?.endsWith(".pdf", ignoreCase = true) == true
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isEpub || isPdf) {
                    Button(
                        onClick = { if (isEpub) onReadEpub() else onReadPdf() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(if (isPdf) Icons.Default.PictureAsPdf else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.read))
                    }
                }
                
                IconButton(
                    onClick = onImportFile,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = "Upload", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                if (book.status == BookStatus.FINISHED) {
                    FilledTonalButton(
                        onClick = onReopenBook,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reopen")
                    }
                } else {
                    Button(
                        onClick = onFinishBook,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finish")
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterItem(
    chapter: Chapter,
    mastery: ChapterMastery?,
    onClick: () -> Unit,
    onStartSession: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val displayValue = if (chapter.number.equals("Prologue", ignoreCase = true) || 
                        chapter.number == "0") "P" else chapter.number
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title ?: (stringResource(R.string.btn_chapter) + " ${chapter.number}"),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.registered_words_count, mastery?.uniqueWordsCount ?: 0),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onStartSession) {
                Icon(Icons.Default.PlayCircleOutline, contentDescription = "Start Session", tint = MaterialTheme.colorScheme.primary)
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.EditNote, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            }

            IconButton(onClick = { showConfirmDelete = true }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
            
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text(stringResource(R.string.delete_chapter_title)) },
            text = { Text(stringResource(R.string.delete_chapter_desc)) },
            confirmButton = {
                Button(onClick = { onDelete(); showConfirmDelete = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun EditChapterDialog(
    chapter: Chapter,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var number by remember { mutableStateOf(chapter.number) }
    var title by remember { mutableStateOf(chapter.title ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_chapter_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Chapter Number") })
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Chapter Title (Optional)") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(number, title.ifBlank { null }) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun EditBookDialog(
    book: Book,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String?) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var language by remember { mutableStateOf(book.language) }
    var genre by remember { mutableStateOf(book.genre ?: "") }
    var showConfirmDelete by remember { mutableStateOf(false) }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text(stringResource(R.string.delete_entire_book_confirm_title)) },
            text = { Text(stringResource(R.string.delete_entire_book_confirm_desc)) },
            confirmButton = {
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.edit_book_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.field_title_hint)) })
                    OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text(stringResource(R.string.field_author_hint)) })
                    OutlinedTextField(value = language, onValueChange = { language = it }, label = { Text(stringResource(R.string.field_language_hint)) })
                    OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text(stringResource(R.string.field_genre_hint)) })
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = { showConfirmDelete = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.delete_book_btn))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onConfirm(title, author, language, genre.ifBlank { null }) }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
