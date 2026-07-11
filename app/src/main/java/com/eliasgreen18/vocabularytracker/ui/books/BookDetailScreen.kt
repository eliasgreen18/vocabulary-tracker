package com.eliasgreen18.vocabularytracker.ui.books

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.eliasgreen18.vocabularytracker.domain.model.BookStats
import com.eliasgreen18.vocabularytracker.domain.model.BookStatus
import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.model.ChapterMastery
import com.eliasgreen18.vocabularytracker.domain.model.ReadingSession
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    onNavigateToChapterDetail: (Long) -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    onNavigateToCompletion: (Long) -> Unit,
    onNavigateToPdfReader: (Long) -> Unit,
    onNavigateToEpubReader: (Long) -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showEditBookDialog by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showStats) (uiState.stats?.bookTitle ?: "Details") else "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.stats?.bookStatus != BookStatus.FINISHED) {
                        IconButton(onClick = { 
                            viewModel.finishBook()
                            onNavigateToCompletion(viewModel.bookId)
                        }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Finish", tint = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        IconButton(onClick = { viewModel.resumeBook() }) {
                            Icon(Icons.Default.Replay, contentDescription = "Resume", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    IconButton(onClick = { showEditBookDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Book")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Book Header
            uiState.stats?.let { s ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (s.bookCoverPath != null) {
                        AsyncImage(
                            model = s.bookCoverPath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MenuBook, 
                                contentDescription = null, 
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = s.bookTitle,
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.weight(1f)
                            )
                            if (s.bookStatus == BookStatus.FINISHED) {
                                Surface(
                                    color = Color(0xFFFFD700),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "FINISHED",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (s.bookAuthor.isNullOrBlank()) "Unknown Author" else "by ${s.bookAuthor}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Action Hub Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showAddChapterDialog = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chapter", maxLines = 1)
                    }

                    // Digital Reader Action
                    if (uiState.stats?.bookFilePath != null) {
                        val isEpub = uiState.stats?.bookFilePath?.lowercase()?.endsWith(".epub") == true
                        Button(
                            onClick = { 
                                if (isEpub) onNavigateToEpubReader(viewModel.bookId)
                                else onNavigateToPdfReader(viewModel.bookId)
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                if (isEpub) Icons.Default.Book else Icons.Default.PictureAsPdf, 
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEpub) "Read EPUB" else "Read PDF", maxLines = 1)
                        }
                    }

                    OutlinedIconToggleButton(
                        checked = showStats,
                        onCheckedChange = { showStats = it },
                        modifier = if (uiState.stats?.bookFilePath != null) Modifier.size(48.dp) else Modifier.weight(0.5f)
                    ) {
                        Icon(Icons.Default.Leaderboard, contentDescription = "Stats")
                    }
                }

                // Expandable Stats Section
                AnimatedVisibility(visible = showStats) {
                    uiState.stats?.let { s ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            BookStatsCard(stats = s)
                            if (s.topWords.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Top Vocabulary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(s.topWords) { word ->
                                        TopWordChip(word = word, onClick = { onNavigateToWordDetail(word.wordId) })
                                    }
                                }
                            }
                        }
                    }
                }

                // Chapters History Search
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search chapters...") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.chapters.sortedByDescending { it.number }) { chapter ->
                        val mastery = uiState.mastery[chapter.id]
                        val isActive = uiState.activeSession?.chapterId == chapter.id
                        ChapterItem(
                            chapter = chapter,
                            mastery = mastery,
                            isActive = isActive,
                            onStartSession = { viewModel.startSessionForChapter(chapter.id, onNavigateToSession) },
                            onViewDetails = { onNavigateToChapterDetail(chapter.id) },
                            onDelete = { viewModel.deleteChapter(chapter.id) }
                        )
                    }
                }
            }
        }

        if (showAddChapterDialog) {
            AddChapterDialog(
                onDismiss = { showAddChapterDialog = false },
                onConfirm = { number, title ->
                    viewModel.startNewChapter(number, title, onNavigateToSession)
                    showAddChapterDialog = false
                }
            )
        }

        if (showEditBookDialog && uiState.stats != null) {
            EditBookMetadataDialog(
                stats = uiState.stats!!,
                onDismiss = { showEditBookDialog = false },
                onConfirm = { title, author, language, genre, coverUri, fileUri ->
                    viewModel.updateBookMetadata(title, author, language, genre, coverUri, fileUri)
                    showEditBookDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookMetadataDialog(
    stats: BookStats,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String?, Uri?, Uri?) -> Unit
) {
    var title by remember { mutableStateOf(stats.bookTitle) }
    var author by remember { mutableStateOf(stats.bookAuthor ?: "") }
    var language by remember { mutableStateOf(stats.bookLanguage) }
    var genre by remember { mutableStateOf(stats.bookGenre ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Book Details") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedImageUri == null) "Cover" else "Cover ✓", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") }, // Allow all and filter in result or use more specific mime
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        val text = when {
                            selectedFileUri?.toString()?.contains(".pdf") == true -> "PDF ✓"
                            selectedFileUri?.toString()?.contains(".epub") == true -> "EPUB ✓"
                            selectedFileUri != null -> "File ✓"
                            else -> "Attach Book"
                        }
                        Text(text, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(title, author, language, genre.ifBlank { null }, selectedImageUri, selectedFileUri) 
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BookStatsCard(stats: BookStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Unique", value = stats.uniqueWordsCount)
            StatItem(label = "Hits", value = stats.totalOccurrencesCount)
            StatItem(label = "Learned", value = stats.learnedWordsCount)
        }
    }
}

@Composable
fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun TopWordChip(word: WordWithCount, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text("${word.wordText} (${word.globalCount})") }
    )
}

@Composable
fun ChapterItem(
    chapter: Chapter, 
    mastery: ChapterMastery?, 
    isActive: Boolean,
    onStartSession: () -> Unit, 
    onViewDetails: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Status Icon
            Icon(
                if (isActive) Icons.Default.History else Icons.Default.CheckCircle, 
                contentDescription = null, 
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))

            // Main Text Content (Flexible)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chapter.displayTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (mastery?.isMastered == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Verified, 
                            contentDescription = "Mastered", 
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                if (mastery != null) {
                    Text(
                        text = "${mastery.learnedWordsCount}/${mastery.uniqueWordsCount} words mastered",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Text(
                        text = "Click to view words",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Grouped Actions at far right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete", 
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = onStartSession,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isActive) Icons.Default.PauseCircle else Icons.Default.PlayArrow, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Chapter?") },
            text = { Text("This will remove the chapter history. Words registered will remain in your collection but won't be linked to this chapter anymore.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AddChapterDialog(onDismiss: () -> Unit, onConfirm: (String, String?) -> Unit) {
    var number by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Chapter Session") },
        text = {
            Column {
                TextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Chapter/Section Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Chapter Title (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(number, title.ifBlank { null }) }, enabled = number.isNotBlank()) {
                Text("Start Reading")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
