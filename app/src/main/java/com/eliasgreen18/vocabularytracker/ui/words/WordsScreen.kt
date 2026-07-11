package com.eliasgreen18.vocabularytracker.ui.words

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onExportAnkiClick: (List<WordWithCount>) -> Unit,
    onExportQuizletClick: (List<WordWithCount>) -> Unit,
    viewModel: WordsViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val availableBooks by viewModel.availableBooks.collectAsState()
    val availableAuthors by viewModel.availableAuthors.collectAsState()
    
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedWordIds.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

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
                        IconButton(onClick = { 
                            val selectedWords = searchResults.filter { selectedIds.contains(it.wordId) }
                            onExportAnkiClick(selectedWords) 
                        }) {
                            Icon(Icons.Default.UploadFile, contentDescription = "Export Anki")
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraLarge,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    
                    FilledTonalIconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (filters != FilterState()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        BadgedBox(badge = {
                            if (filters != FilterState()) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary)
                            }
                        }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${searchResults.size} words found",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (filters != FilterState()) {
                    TextButton(onClick = { viewModel.clearFilters() }, contentPadding = PaddingValues(0.dp)) {
                        Text("Clear Filters", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
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
                text = { Text("This will permanently remove ${selectedIds.size} words and all their history.") },
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

        if (showFilterDialog) {
            FilterDialog(
                currentFilters = filters,
                availableBooks = availableBooks.map { it.id to it.title },
                availableAuthors = availableAuthors,
                onDismiss = { showFilterDialog = false },
                onApply = { 
                    viewModel.updateFilters(it)
                    showFilterDialog = false
                }
            )
        }
    }
}

@Composable
fun FilterDialog(
    currentFilters: FilterState,
    availableBooks: List<Pair<Long, String>>,
    availableAuthors: List<String>,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    var tempFilters by remember { mutableStateOf(currentFilters) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Filters") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Favorites
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = tempFilters.isFavoriteOnly, onCheckedChange = { tempFilters = tempFilters.copy(isFavoriteOnly = it) })
                    Text("Only Favorites (Star)")
                }

                // Mastery
                Text("By Status", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WordMastery.values().forEach { mastery ->
                        FilterChip(
                            selected = tempFilters.mastery == mastery,
                            onClick = { 
                                tempFilters = tempFilters.copy(mastery = if (tempFilters.mastery == mastery) null else mastery)
                            },
                            label = { Text(mastery.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                // Books
                if (availableBooks.isNotEmpty()) {
                    Text("By Book", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.heightIn(max = 120.dp)) {
                        LazyColumn {
                            items(availableBooks) { (id, title) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { 
                                        tempFilters = tempFilters.copy(bookId = if (tempFilters.bookId == id) null else id)
                                    }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = tempFilters.bookId == id, onClick = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                // Authors
                if (availableAuthors.isNotEmpty()) {
                    Text("By Author", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.heightIn(max = 120.dp)) {
                        LazyColumn {
                            items(availableAuthors) { author ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { 
                                        tempFilters = tempFilters.copy(author = if (tempFilters.author == author) null else author)
                                    }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = tempFilters.author == author, onClick = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(author)
                                }
                            }
                        }
                    }
                }

                // Hits
                Text("By Encounters", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempFilters.minHits?.toString() ?: "",
                        onValueChange = { tempFilters = tempFilters.copy(minHits = it.toIntOrNull()) },
                        label = { Text("Min") },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                    Text("-")
                    OutlinedTextField(
                        value = tempFilters.maxHits?.toString() ?: "",
                        onValueChange = { tempFilters = tempFilters.copy(maxHits = it.toIntOrNull()) },
                        label = { Text("Max") },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(tempFilters) }) {
                Text("Apply Filters")
            }
        },
        dismissButton = {
            TextButton(onClick = { 
                tempFilters = FilterState()
                onApply(tempFilters)
            }) {
                Text("Clear All")
            }
        }
    )
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mastery Dot
            if (!selectionMode) {
                Surface(
                    color = when (word.mastery) {
                        WordMastery.NEW -> MaterialTheme.colorScheme.outlineVariant
                        WordMastery.LEARNING -> MaterialTheme.colorScheme.primaryContainer
                        WordMastery.LEARNED -> Color(0xFFC8E6C9)
                    },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(8.dp)
                ) {}
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Word and Translation
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.wordText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (word.translation != null) {
                        Text(
                            text = " • ${word.translation}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Text(
                    text = "${word.globalCount} encounters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Actions
            if (selectionMode) {
                Checkbox(
                    checked = isSelected, 
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(24.dp)
                )
            } else {
                IconButton(
                    onClick = { onToggleFocus(!word.isFocusWord) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (word.isFocusWord) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Focus",
                        tint = if (word.isFocusWord) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
