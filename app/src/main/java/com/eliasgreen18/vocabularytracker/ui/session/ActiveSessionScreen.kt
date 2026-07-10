package com.eliasgreen18.vocabularytracker.ui.session

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val sessionInfo by viewModel.sessionInfo.collectAsState()
    val sessionWords by viewModel.sessionWords.collectAsState()
    val autoScrollEnabled by viewModel.autoScrollEnabled.collectAsState()
    val listState = rememberLazyListState()

    var showEditDialog by remember { mutableStateOf(false) }
    var wordText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Auto-scroll logic: pin to top instantly if enabled
    LaunchedEffect(sessionWords.size) {
        if (sessionWords.isNotEmpty() && autoScrollEnabled) {
            listState.scrollToItem(0)
        }
    }

    // Word Editing Dialog state
    var wordToEdit by remember { mutableStateOf<WordWithCount?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    sessionInfo?.let { info ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = info.book?.title ?: "Reading", 
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = info.chapter.number,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Chapter", modifier = Modifier.size(20.dp))
                    }
                    TextButton(onClick = { viewModel.endSession(onNavigateBack) }) {
                        Text("Finish", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
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
            // Focus Input Field
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = wordText,
                    onValueChange = { wordText = it },
                    placeholder = { Text("Enter word...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (wordText.isNotBlank()) {
                                val word = wordText.trim()
                                viewModel.recordWord(word)
                                wordText = ""
                                focusRequester.requestFocus()
                            }
                        }
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (wordText.isNotBlank()) {
                                    val word = wordText.trim()
                                    viewModel.recordWord(word)
                                    wordText = ""
                                    focusRequester.requestFocus()
                                }
                            },
                            enabled = wordText.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Add")
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Minimalist Chronological Feed
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(sessionWords, key = { it.wordId }) { wordWithCount ->
                    FocusWordItem(
                        word = wordWithCount,
                        onToggleFocus = { isFocus -> viewModel.toggleFocus(wordWithCount.wordId, isFocus) },
                        onDeleteFromSession = { viewModel.deleteFromSession(wordWithCount.wordId) },
                        onEditWord = { wordToEdit = wordWithCount },
                        onPlusClick = { 
                            viewModel.recordWord(wordWithCount.wordText)
                        },
                        onClick = { onNavigateToWordDetail(wordWithCount.wordId) }
                    )
                }
            }
        }

        if (showEditDialog) {
            sessionInfo?.let { info ->
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
        }

        wordToEdit?.let { word ->
            EditWordDialog(
                initialText = word.wordText,
                onDismiss = { wordToEdit = null },
                onConfirm = { newText ->
                    viewModel.renameWord(word.wordId, newText)
                    wordToEdit = null
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun FocusWordItem(
    word: WordWithCount, 
    onToggleFocus: (Boolean) -> Unit, 
    onDeleteFromSession: () -> Unit,
    onEditWord: () -> Unit,
    onPlusClick: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { 
            Text(
                text = word.wordText, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = {
            if (word.translation != null && word.translationStatus == TranslationStatus.DONE) {
                Text(
                    text = word.translation!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Compact Metrics
                MetricChip(value = word.sessionCount, isPrimary = true)
                Spacer(modifier = Modifier.width(4.dp))
                MetricChip(value = word.globalCount, isPrimary = false)
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Growth Action: Plus
                IconButton(onClick = onPlusClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Hit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Maintenance Hub: Overflow Menu
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.MoreVert, 
                            contentDescription = "More", 
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit word text") },
                            onClick = { 
                                showMenu = false
                                onEditWord() 
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (word.isFocusWord) "Remove from Focus" else "Add to Focus") },
                            onClick = { 
                                showMenu = false
                                onToggleFocus(!word.isFocusWord) 
                            },
                            leadingIcon = { 
                                Icon(
                                    if (word.isFocusWord) Icons.Default.Star else Icons.Default.StarBorder, 
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                ) 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove from this session", color = MaterialTheme.colorScheme.error) },
                            onClick = { 
                                showMenu = false
                                onDeleteFromSession() 
                            },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun MetricChip(value: Int, isPrimary: Boolean) {
    Surface(
        color = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape,
        modifier = Modifier.size(22.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EditWordDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Word") },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Word text") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChapterDialog(
    initialNumber: String,
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var chapterNumber by remember { mutableStateOf(initialNumber) }
    var chapterTitle by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Chapter Info") },
        text = {
            Column {
                TextField(
                    value = chapterNumber,
                    onValueChange = { chapterNumber = it },
                    label = { Text("Chapter/Section *") },
                    modifier = Modifier.fillMaxWidth()
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
                    onConfirm(chapterNumber, chapterTitle.ifBlank { null })
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
