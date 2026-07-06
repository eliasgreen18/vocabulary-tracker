package com.eliasgreen18.vocabularytracker.ui.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: ChapterDetailViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val words by viewModel.words.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chapter Insights") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                stats?.let { s ->
                    ChapterStatsSummaryCard(
                        uniqueCount = s.uniqueWordsCount,
                        newCount = s.newWordsCount
                    )
                }
            }

            item {
                Text(text = "Vocabulary in this chapter", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(words) { word ->
                ChapterWordItem(word = word, onClick = { onNavigateToWordDetail(word.wordId) })
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ChapterStatsSummaryCard(uniqueCount: Int, newCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = uniqueCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "Unique Words", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = newCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "New Discovered", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ChapterWordItem(word: WordWithCount, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(word.wordText) },
        supportingContent = { Text("Total: ${word.globalCount} appearances") },
        trailingContent = {
            if (word.mastery == WordMastery.NEW) {
                Icon(Icons.Default.FiberNew, contentDescription = "New Word", tint = MaterialTheme.colorScheme.primary)
            }
        }
    )
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
