package com.eliasgreen18.vocabularytracker.ui.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.BookStats
import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    onNavigateToChapterDetail: (Long) -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val stats by viewModel.bookStats.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    var showAddChapterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stats?.bookTitle ?: "Book Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onContinueReading(onNavigateToSession) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Continue Reading")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Book Stats Summary
            item {
                stats?.let { s ->
                    BookStatsCard(stats = s)
                }
            }

            // Top Words Horizontal List
            item {
                stats?.let { s ->
                    if (s.topWords.isNotEmpty()) {
                        Column {
                            Text(text = "Top Vocabulary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

            // Chapters List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Chapters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showAddChapterDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Chapter")
                    }
                }
            }

            items(chapters.sortedByDescending { it.number }) { chapter ->
                ChapterItem(
                    chapter = chapter,
                    onClick = { onNavigateToChapterDetail(chapter.id) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
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
    }
}

@Composable
fun BookStatsCard(stats: BookStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Unique Words", value = stats.uniqueWordsCount)
            StatItem(label = "Total Hits", value = stats.totalOccurrencesCount)
            StatItem(label = "Learned", value = stats.learnedWordsCount)
        }
    }
}

@Composable
fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
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
fun ChapterItem(chapter: Chapter, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(chapter.displayTitle) },
        supportingContent = { Text("Click for details") },
        trailingContent = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.outline) }
    )
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun AddChapterDialog(onDismiss: () -> Unit, onConfirm: (Int, String?) -> Unit) {
    var number by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start New Chapter") },
        text = {
            Column {
                TextField(
                    value = number,
                    onValueChange = { if (it.all { c -> c.isDigit() }) number = it },
                    label = { Text("Chapter Number") },
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
            Button(
                onClick = { number.toIntOrNull()?.let { onConfirm(it, title.ifBlank { null }) } },
                enabled = number.isNotBlank()
            ) {
                Text("Start Reading")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
