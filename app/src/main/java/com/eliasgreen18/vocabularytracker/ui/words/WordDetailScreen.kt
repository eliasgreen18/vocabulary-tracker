package com.eliasgreen18.vocabularytracker.ui.words

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.model.WordOccurrenceDetail
import com.eliasgreen18.vocabularytracker.ui.util.ExternalTranslationHelper
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel()
) {
    val word by viewModel.word.collectAsState()
    val history by viewModel.history.collectAsState()
    val context = LocalContext.current
    
    var showManualEdit by remember { mutableStateOf(false) }
    var manualTranslation by remember { mutableStateOf("") }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        .withZone(ZoneId.systemDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(word?.text ?: "Word Detail") },
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
                .padding(16.dp)
        ) {
            word?.let { w ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Translation", style = MaterialTheme.typography.labelMedium)
                            IconButton(onClick = { 
                                manualTranslation = w.translation ?: ""
                                showManualEdit = true 
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Manual", modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        when (w.translationStatus) {
                            TranslationStatus.DONE -> {
                                Text(
                                    text = w.translation ?: "No translation",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TranslationStatus.LOADING, TranslationStatus.PENDING -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Translating...", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            TranslationStatus.ERROR -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Error translating",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    IconButton(onClick = { viewModel.retryTranslation() }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Retry")
                                    }
                                }
                            }
                            TranslationStatus.NOT_REQUESTED -> {
                                Text(
                                    text = "Translation will trigger after 3 occurrences.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Quick Search", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { ExternalTranslationHelper.openGoogleTranslate(context, w.text) },
                                label = { Text("Google Translate") },
                                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            AssistChip(
                                onClick = { ExternalTranslationHelper.openReversoContext(context, w.text) },
                                label = { Text("Reverso") },
                                leadingIcon = { Icon(Icons.Default.ManageSearch, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { /* Placeholder for future mastery logic */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("I already know this word")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Occurrences (${history.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { detail ->
                    HistoryItem(detail, dateFormatter)
                }
            }
        }

        if (showManualEdit) {
            AlertDialog(
                onDismissRequest = { showManualEdit = false },
                title = { Text("Edit Translation") },
                text = {
                    TextField(
                        value = manualTranslation,
                        onValueChange = { manualTranslation = it },
                        label = { Text("Translation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.saveManualTranslation(manualTranslation)
                        showManualEdit = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualEdit = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryItem(detail: WordOccurrenceDetail, formatter: DateTimeFormatter) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = detail.bookTitle, style = MaterialTheme.typography.titleMedium)
            Text(text = detail.displayChapter, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Seen on: ${formatter.format(detail.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
