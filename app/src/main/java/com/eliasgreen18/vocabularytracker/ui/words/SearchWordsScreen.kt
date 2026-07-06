package com.eliasgreen18.vocabularytracker.ui.words

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWordsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val currentFilter by viewModel.masteryFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Vocabulary") },
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
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search words") },
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(searchResults) { wordWithCount ->
                    SearchWordItem(
                        word = wordWithCount,
                        onToggleFocus = { isFocus -> viewModel.onToggleFocus(wordWithCount.wordId, isFocus) },
                        onClick = { onNavigateToWordDetail(wordWithCount.wordId) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchWordItem(word: WordWithCount, onToggleFocus: (Boolean) -> Unit, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(word.wordText) },
        supportingContent = {
             Text(
                text = "${word.mastery.name.lowercase().replaceFirstChar { it.uppercase() }} • ${word.globalCount} times",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = { 
            IconButton(onClick = { onToggleFocus(!word.isFocusWord) }) {
                Icon(
                    imageVector = if (word.isFocusWord) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Focus",
                    tint = if (word.isFocusWord) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
