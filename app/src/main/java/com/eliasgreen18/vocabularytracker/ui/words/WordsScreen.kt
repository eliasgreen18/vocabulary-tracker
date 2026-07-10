package com.eliasgreen18.vocabularytracker.ui.words

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.ui.util.MainTopBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WordsScreen(
    onNavigateToWordDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onBackupClick: () -> Unit,
    onSyncClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onExportJsonClick: () -> Unit,
    viewModel: WordsViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val currentFilter by viewModel.masteryFilter.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedWordIds.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFocusForSelected(true) }) {
                            Icon(Icons.Default.Star, contentDescription = "Star selected")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            } else {
                MainTopBar(
                    title = "My Vocabulary",
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onBackupClick = onBackupClick,
                    onSyncClick = onSyncClick,
                    onExportCsvClick = onExportCsvClick,
                    onExportJsonClick = onExportJsonClick
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (!isSelectionMode) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    label = { Text("Search words") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
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
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(searchResults, key = { it.wordId }) { wordWithCount ->
                    val isSelected = selectedIds.contains(wordWithCount.wordId)
                    WordItem(
                        word = wordWithCount,
                        isSelected = isSelected,
                        selectionMode = isSelectionMode,
                        onToggleFocus = { isFocus -> viewModel.onToggleFocus(wordWithCount.wordId, isFocus) },
                        onLongClick = { viewModel.toggleSelection(wordWithCount.wordId) },
                        onClick = { 
                            if (isSelectionMode) viewModel.toggleSelection(wordWithCount.wordId)
                            else onNavigateToWordDetail(wordWithCount.wordId) 
                        }
                    )
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete selected words?") },
                text = { Text("This will permanently remove ${selectedIds.size} words and all their history. This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSelectedWords()
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordItem(
    word: WordWithCount, 
    isSelected: Boolean,
    selectionMode: Boolean,
    onToggleFocus: (Boolean) -> Unit, 
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent),
        headlineContent = { 
            Text(
                text = word.wordText,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ) 
        },
        supportingContent = {
             Text(
                text = "${word.mastery.name.lowercase().replaceFirstChar { it.uppercase() }} • ${word.globalCount} times",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = { 
            if (selectionMode) {
                Checkbox(checked = isSelected, onCheckedChange = { onClick() })
            } else {
                IconButton(onClick = { onToggleFocus(!word.isFocusWord) }) {
                    Icon(
                        imageVector = if (word.isFocusWord) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Focus",
                        tint = if (word.isFocusWord) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        leadingContent = if (selectionMode) null else {
            {
                Surface(
                    color = when (word.mastery) {
                        WordMastery.NEW -> MaterialTheme.colorScheme.outlineVariant
                        WordMastery.LEARNING -> MaterialTheme.colorScheme.primaryContainer
                        WordMastery.LEARNED -> Color(0xFFC8E6C9)
                    },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(12.dp)
                ) {}
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
