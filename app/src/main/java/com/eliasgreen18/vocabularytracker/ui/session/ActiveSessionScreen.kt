package com.eliasgreen18.vocabularytracker.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.ui.platform.LocalContext
import com.eliasgreen18.vocabularytracker.ui.util.ExternalTranslationHelper
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val sessionInfo by viewModel.sessionInfo.collectAsState()
    val sessionWords by viewModel.sessionWords.collectAsState()
    val highlightedWords by viewModel.highlightedWords.collectAsState()
    val sessionSummary by viewModel.sessionSummary.collectAsState()
    val currentFilter by viewModel.masteryFilter.collectAsState()
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(true) }
    var wordText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading Session") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.endSession(onNavigateBack) }) {
                        Text(
                            "End",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            sessionInfo?.let { info ->
                // Book & Chapter Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    onClick = { showEditDialog = true }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                info.book?.let { book ->
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "by ${book.author}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = info.chapter.displayTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Session Summary
                sessionSummary?.let { summary ->
                    AnimatedVisibility(
                        visible = showSummary && summary.totalWords > 0,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Session Progress",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    IconButton(onClick = { showSummary = false }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Hide Summary", modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    SummaryStat(label = "New", value = summary.newWords)
                                    SummaryStat(label = "Learning", value = summary.learningWords)
                                    SummaryStat(label = "Learned", value = summary.learnedWords)
                                }
                            }
                        }
                    }
                }

                // Highlights Section
                if (highlightedWords.isNotEmpty()) {
                    Text(
                        text = "Highlights",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(highlightedWords) { word ->
                            SuggestionChip(
                                onClick = { onNavigateToWordDetail(word.wordId) },
                                label = { Text(word.wordText) }
                            )
                        }
                    }
                }

                // Word Input
                OutlinedTextField(
                    value = wordText,
                    onValueChange = { wordText = it },
                    label = { Text("New Word") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (wordText.isNotBlank()) {
                                viewModel.recordWord(wordText)
                                wordText = ""
                            }
                        }
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (wordText.isNotBlank()) {
                                    viewModel.recordWord(wordText)
                                    wordText = ""
                                }
                            },
                            enabled = wordText.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Add Word")
                        }
                    }
                )

                // Mastery Filters
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = currentFilter == null,
                            onClick = { viewModel.onFilterChanged(null) },
                            label = { Text("All") }
                        )
                    }
                    items(WordMastery.values()) { mastery ->
                        FilterChip(
                            selected = currentFilter == mastery,
                            onClick = { viewModel.onFilterChanged(mastery) },
                            label = { Text(mastery.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                // Words List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(sessionWords) { wordWithCount ->
                        SessionWordItem(
                            word = wordWithCount,
                            onToggleFocus = { isFocus -> viewModel.toggleFocus(wordWithCount.wordId, isFocus) },
                            onTranslateExternal = { ExternalTranslationHelper.openGoogleTranslate(context, wordWithCount.wordText) },
                            onClick = { onNavigateToWordDetail(wordWithCount.wordId) }
                        )
                    }
                }

                if (showEditDialog) {
                    EditChapterDialog(
                        initialNumber = info.chapter.number,
                        initialTitle = info.chapter.title ?: "",
                        onDismiss = { showEditDialog = false },
                        onConfirm = { number, title ->
                            viewModel.updateChapterInfo(number, title)
                            showEditDialog = false
                        }
                    )
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Auto focus the input field
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun SummaryStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SessionWordItem(
    word: WordWithCount, 
    onToggleFocus: (Boolean) -> Unit, 
    onTranslateExternal: () -> Unit,
    onClick: () -> Unit
) {
    val (icon, color) = when (word.mastery) {
        WordMastery.NEW -> Icons.Default.FiberNew to MaterialTheme.colorScheme.outline
        WordMastery.LEARNING -> Icons.AutoMirrored.Filled.MenuBook to MaterialTheme.colorScheme.primary
        WordMastery.LEARNED -> Icons.Default.Check to MaterialTheme.colorScheme.tertiary
    }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(word.wordText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (word.translation != null && word.translationStatus == TranslationStatus.DONE) {
                    Text(
                        text = " → ${word.translation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else if (word.translationStatus == TranslationStatus.LOADING || word.translationStatus == TranslationStatus.PENDING) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "traduciendo...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else if (word.translationStatus == TranslationStatus.ERROR) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(!)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        supportingContent = {
            Text(
                text = "Session: ${word.sessionCount} • Total: ${word.globalCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = word.mastery.name,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onTranslateExternal) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Translate External",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { onToggleFocus(!word.isFocusWord) }) {
                    Icon(
                        imageVector = if (word.isFocusWord) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Focus",
                        tint = if (word.isFocusWord) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChapterDialog(
    initialNumber: Int,
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String?) -> Unit
) {
    var chapterNumber by remember { mutableStateOf(initialNumber.toString()) }
    var chapterTitle by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Chapter Info") },
        text = {
            Column {
                TextField(
                    value = chapterNumber,
                    onValueChange = { if (it.all { char -> char.isDigit() }) chapterNumber = it },
                    label = { Text("Chapter Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = chapterTitle,
                    onValueChange = { chapterTitle = it },
                    label = { Text("Chapter Title (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    chapterNumber.toIntOrNull()?.let { 
                        onConfirm(it, chapterTitle.ifBlank { null }) 
                    }
                },
                enabled = chapterNumber.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
