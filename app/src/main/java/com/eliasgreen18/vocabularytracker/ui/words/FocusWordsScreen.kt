package com.eliasgreen18.vocabularytracker.ui.words

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusWordsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: FocusWordsViewModel = hiltViewModel()
) {
    val focusWords by viewModel.focusWords.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Words") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (focusWords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No words marked for focus yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(focusWords) { word ->
                    FocusWordItem(
                        word = word,
                        onClick = { onNavigateToWordDetail(word.wordId) },
                        onRemove = { viewModel.removeFocus(word.wordId) }
                    )
                }
            }
        }
    }
}

@Composable
fun FocusWordItem(word: WordWithCount, onClick: () -> Unit, onRemove: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(word.wordText) },
        supportingContent = { Text("Seen ${word.globalCount} times") },
        trailingContent = {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Star, contentDescription = "Remove Focus", tint = MaterialTheme.colorScheme.primary)
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
