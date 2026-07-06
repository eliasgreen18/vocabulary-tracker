package com.eliasgreen18.vocabularytracker.ui.books

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
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
    var showStats by remember { mutableStateOf(false) }

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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Action Hub Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // New Chapter Button (Green)
                Button(
                    onClick = { showAddChapterDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Chapter", style = MaterialTheme.typography.labelLarge)
                }

                // Stats Toggle Button
                OutlinedIconToggleButton(
                    checked = showStats,
                    onCheckedChange = { showStats = it },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraLarge // Match button style
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (showStats) Icons.Default.BarChart else Icons.Default.Leaderboard,
                            contentDescription = "Toggle Stats",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stats", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Expandable Stats Section
            AnimatedVisibility(
                visible = showStats,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                stats?.let { s ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            // Chapters List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "Chapters History",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                items(chapters.sortedByDescending { it.number }) { chapter ->
                    ChapterItem(
                        chapter = chapter,
                        onStartSession = { viewModel.startSessionForChapter(chapter.id, onNavigateToSession) },
                        onViewDetails = { onNavigateToChapterDetail(chapter.id) }
                    )
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
fun ChapterItem(chapter: Chapter, onStartSession: () -> Unit, onViewDetails: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onViewDetails),
        headlineContent = { Text(chapter.displayTitle) },
        supportingContent = { Text("Stats & words", style = MaterialTheme.typography.bodySmall) },
        trailingContent = {
            IconButton(onClick = onStartSession) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, 
                    contentDescription = "Start Session", 
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        leadingContent = {
            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun AddChapterDialog(onDismiss: () -> Unit, onConfirm: (Int, String?) -> Unit) {
    var number by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Chapter Session") },
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
