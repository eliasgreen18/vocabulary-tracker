package com.eliasgreen18.vocabularytracker.ui.words

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.R
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount

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
    viewModel: WordsViewModel = hiltViewModel(),
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val availableBooks by viewModel.availableBooks.collectAsState()
    
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedWordIds.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Integrated Header
        if (isSelectionMode) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                        Text("${selectedIds.size} selected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row {
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
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.words_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_notifications)) },
                            onClick = { showMenu = false; onNavigateToNotifications() },
                            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_settings)) },
                            onClick = { showMenu = false; onNavigateToSettings() },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_sync_drive)) },
                            onClick = { showMenu = false; onSyncClick() },
                            leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_export_csv)) },
                            onClick = { showMenu = false; onExportCsvClick() },
                            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_export_json)) },
                            onClick = { showMenu = false; onExportJsonClick() },
                            leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_db_backup)) },
                            onClick = { showMenu = false; onBackupClick() },
                            leadingIcon = { Icon(Icons.Default.Backup, contentDescription = null) }
                        )
                    }
                }
            }
        }

        if (!isSelectionMode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text(stringResource(R.string.search_hint), fontSize = 14.sp) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
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
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.words_found_count, searchResults.size),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
            if (filters != FilterState()) {
                TextButton(onClick = { viewModel.clearFilters() }, contentPadding = PaddingValues(0.dp)) {
                    Text(stringResource(R.string.reset_filters), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
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
                HorizontalDivider(
                    modifier = Modifier.padding(start = 24.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_selected_title)) },
            text = { Text(stringResource(R.string.delete_selected_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelectedWords()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            currentFilters = filters,
            availableBooks = availableBooks.map { it.id to it.title },
            onDismiss = { showFilterDialog = false },
            onApply = { 
                viewModel.updateFilters(it)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun FilterDialog(
    currentFilters: FilterState,
    availableBooks: List<Pair<Long, String>>,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    var tempFilters by remember { mutableStateOf(currentFilters) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_refine_title), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Favorites
                Surface(
                    onClick = { tempFilters = tempFilters.copy(isFavoriteOnly = !tempFilters.isFavoriteOnly) },
                    color = if (tempFilters.isFavoriteOnly) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    shape = MaterialTheme.shapes.medium,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (tempFilters.isFavoriteOnly) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (tempFilters.isFavoriteOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.filter_only_favorites), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Mastery
                Column {
                    Text(stringResource(R.string.filter_by_knowledge), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WordMastery.entries.forEach { mastery ->
                            FilterChip(
                                selected = tempFilters.mastery == mastery,
                                onClick = { 
                                    tempFilters = tempFilters.copy(mastery = if (tempFilters.mastery == mastery) null else mastery)
                                },
                                label = { Text(mastery.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }

                // Books
                if (availableBooks.isNotEmpty()) {
                    Column {
                        Text(stringResource(R.string.filter_by_book), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.heightIn(max = 140.dp).fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                            LazyColumn(modifier = Modifier.padding(4.dp)) {
                                items(availableBooks) { (id, title) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { 
                                            tempFilters = tempFilters.copy(bookId = if (tempFilters.bookId == id) null else id)
                                        }.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = tempFilters.bookId == id, onClick = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(title, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }

                // Encounters
                Column {
                    Text(stringResource(R.string.filter_by_encounters), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = tempFilters.minHits?.toString() ?: "",
                            onValueChange = { tempFilters = tempFilters.copy(minHits = it.toIntOrNull()) },
                            placeholder = { Text(stringResource(R.string.filter_min), fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        Text(stringResource(R.string.filter_to), style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = tempFilters.maxHits?.toString() ?: "",
                            onValueChange = { tempFilters = tempFilters.copy(maxHits = it.toIntOrNull()) },
                            placeholder = { Text(stringResource(R.string.filter_max), fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(tempFilters) }, shape = MaterialTheme.shapes.medium) {
                Text(stringResource(R.string.filter_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = { 
                tempFilters = FilterState()
                onApply(tempFilters)
            }) {
                Text(stringResource(R.string.clear_filters))
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minimal Mastery Indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (word.mastery) {
                            WordMastery.NEW -> MaterialTheme.colorScheme.outlineVariant
                            WordMastery.LEARNING -> MaterialTheme.colorScheme.primary
                            WordMastery.LEARNED -> Color(0xFF4CAF50)
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))

            // Word and Translation (High Density)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.wordText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Serif
                    )
                    word.translation?.let {
                        Text(
                            text = " • $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Compact Stats and Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.globalCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(8.dp))

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
                            contentDescription = null,
                            tint = if (word.isFocusWord) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
