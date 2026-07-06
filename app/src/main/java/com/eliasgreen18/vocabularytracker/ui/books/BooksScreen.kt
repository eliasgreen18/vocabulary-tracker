package com.eliasgreen18.vocabularytracker.ui.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    viewModel: BookViewModel = hiltViewModel()
) {
    val books by viewModel.booksState.collectAsState()
    var showAddBookDialog by remember { mutableStateOf(false) }
    
    // State for Session Start Flow
    var bookForSession by remember { mutableStateOf<Book?>(null) }
    var chapterNumberToAskTitle by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Books") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddBookDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
            }
        }
    ) { innerPadding ->
        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No books added yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(books) { book ->
                    BookItem(
                        book = book,
                        onClick = {
                            viewModel.onBookClicked(
                                book = book,
                                onSessionReady = onNavigateToSession,
                                onAskChapterNumber = { bookForSession = it },
                                onNeedChapterInfo = { _, _ -> /* Not used anymore */ }
                            )
                        }
                    )
                }
            }
        }

        if (showAddBookDialog) {
            AddBookDialog(
                onDismiss = { showAddBookDialog = false },
                onConfirm = { title, author, language ->
                    viewModel.addBook(title, author, language)
                    showAddBookDialog = false
                }
            )
        }

        // Phase 1: Ask Chapter Number
        bookForSession?.let { book ->
            AskChapterNumberDialog(
                bookTitle = book.title,
                onDismiss = { bookForSession = null },
                onConfirm = { number ->
                    viewModel.onChapterNumberEntered(
                        bookId = book.id,
                        number = number,
                        onExists = { sessionId ->
                            bookForSession = null
                            onNavigateToSession(sessionId)
                        },
                        onNew = { confirmedNumber ->
                            chapterNumberToAskTitle = confirmedNumber
                            // Don't null bookForSession yet, we need it for Phase 2
                        }
                    )
                }
            )
        }

        // Phase 2: Ask Chapter Title (only if number was new)
        chapterNumberToAskTitle?.let { number ->
            val book = bookForSession ?: return@let 
            AskChapterTitleDialog(
                bookTitle = book.title,
                chapterNumber = number,
                onDismiss = { 
                    chapterNumberToAskTitle = null
                    bookForSession = null 
                },
                onConfirm = { title ->
                    viewModel.startSessionWithNewChapter(book.id, number, title) { sessionId ->
                        chapterNumberToAskTitle = null
                        bookForSession = null
                        onNavigateToSession(sessionId)
                    }
                }
            )
        }
    }
}

@Composable
fun BookItem(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = book.title, style = MaterialTheme.typography.titleMedium)
            if (book.author.isNotBlank()) {
                Text(text = "by ${book.author}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Language: ${book.language}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskChapterNumberDialog(
    bookTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var number by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Session: $bookTitle") },
        text = {
            Column {
                Text("Which chapter number are you reading?")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = number,
                    onValueChange = { if (it.all { char -> char.isDigit() }) number = it },
                    label = { Text("Chapter Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { number.toIntOrNull()?.let(onConfirm) },
                enabled = number.isNotBlank()
            ) {
                Text("Next")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskChapterTitleDialog(
    bookTitle: String,
    chapterNumber: Int,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Chapter for $bookTitle") },
        text = {
            Column {
                Text("Chapter $chapterNumber is new. Want to add a title?")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Chapter Title (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title.ifBlank { null }) }) {
                Text("Start Reading")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Book") },
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
                    label = { Text("Author (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, author, language) },
                enabled = title.isNotBlank() && language.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
